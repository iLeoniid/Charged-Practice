package com.charged.achievement

import com.charged.Charged
import com.charged.database.Database
import com.charged.achievement.model.Achievement
import com.charged.achievement.model.AchievementCategory
import com.charged.achievement.model.AchievementReward
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class AchievementManager(
    private val plugin: Charged,
    private val database: Database
) {
    
    private val achievements = mutableListOf<Achievement>()
    private val playerAchievements = ConcurrentHashMap<UUID, MutableSet<String>>()
    
    init {
        registerAchievements()
    }
    
    private fun registerAchievements() {
        // Kill achievements
        achievements.add(Achievement(
            id = "first_kill",
            name = "First Blood",
            description = "Get your first kill",
            category = AchievementCategory.KILLS,
            requirement = 1,
            reward = AchievementReward(coins = 50)
        ))
        
        achievements.add(Achievement(
            id = "killer_10",
            name = "Killer",
            description = "Get 10 kills",
            category = AchievementCategory.KILLS,
            requirement = 10,
            reward = AchievementReward(coins = 100)
        ))
        
        achievements.add(Achievement(
            id = "killer_100",
            name = "Mass Murderer",
            description = "Get 100 kills",
            category = AchievementCategory.KILLS,
            requirement = 100,
            reward = AchievementReward(coins = 500)
        ))
        
        // Win achievements
        achievements.add(Achievement(
            id = "first_win",
            name = "Victory",
            description = "Win your first match",
            category = AchievementCategory.WINS,
            requirement = 1,
            reward = AchievementReward(coins = 100)
        ))
        
        achievements.add(Achievement(
            id = "winner_10",
            name = "Champion",
            description = "Win 10 matches",
            category = AchievementCategory.WINS,
            requirement = 10,
            reward = AchievementReward(coins = 200)
        ))
        
        // Streak achievements
        achievements.add(Achievement(
            id = "streak_5",
            name = "On Fire",
            description = "Get a 5 win streak",
            category = AchievementCategory.STREAKS,
            requirement = 5,
            reward = AchievementReward(coins = 250, title = "§c§lOn Fire")
        ))
        
        achievements.add(Achievement(
            id = "streak_10",
            name = "Unstoppable",
            description = "Get a 10 win streak",
            category = AchievementCategory.STREAKS,
            requirement = 10,
            reward = AchievementReward(coins = 500, title = "§4§lUnstoppable")
        ))
        
        // Secret achievements
        achievements.add(Achievement(
            id = "comeback",
            name = "Comeback King",
            description = "Win a match with less than 2 hearts",
            category = AchievementCategory.SECRET,
            requirement = 1,
            reward = AchievementReward(coins = 300),
            secret = true
        ))
    }
    
    fun checkAchievements(player: Player, category: AchievementCategory) {
        val stats = plugin.plugin.playerManager.getStats(player.uniqueId) ?: return
        val unlocked = playerAchievements.getOrPut(player.uniqueId) { mutableSetOf() }
        
        achievements
            .filter { it.category == category && it.id !in unlocked }
            .forEach { achievement ->
                val progress = when (category) {
                    AchievementCategory.KILLS -> stats.wins + stats.losses // Total matches as proxy for kills
                    AchievementCategory.WINS -> stats.wins
                    AchievementCategory.STREAKS -> stats.winstreak
                    else -> 0
                }
                
                if (progress >= achievement.requirement) {
                    unlockAchievement(player, achievement)
                }
            }
    }
    
    fun unlockAchievement(player: Player, achievement: Achievement) {
        val unlocked = playerAchievements.getOrPut(player.uniqueId) { mutableSetOf() }
        
        if (achievement.id in unlocked) return
        
        unlocked.add(achievement.id)
        saveAchievement(player.uniqueId, achievement.id)
        
        // Notify player
        player.sendMessage("")
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        player.sendMessage("§e§l  ACHIEVEMENT UNLOCKED!")
        player.sendMessage("")
        player.sendMessage("§f  ${achievement.name}")
        player.sendMessage("§7  ${achievement.description}")
        player.sendMessage("")
        player.sendMessage("§7  Reward: §6${achievement.reward.coins} coins")
        if (achievement.reward.title != null) {
            player.sendMessage("§7  Title: ${achievement.reward.title}")
        }
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        player.sendMessage("")
        
        player.playSound(player.location, org.bukkit.Sound.LEVEL_UP, 1f, 1f)
    }
    
    fun getPlayerAchievements(uuid: UUID): Set<String> {
        return playerAchievements[uuid] ?: emptySet()
    }
    
    fun getAchievementProgress(uuid: UUID, achievementId: String): Int {
        val stats = plugin.plugin.playerManager.getStats(uuid) ?: return 0
        val achievement = achievements.find { it.id == achievementId } ?: return 0
        
        return when (achievement.category) {
            AchievementCategory.KILLS -> stats.wins + stats.losses
            AchievementCategory.WINS -> stats.wins
            AchievementCategory.STREAKS -> stats.winstreak
            else -> 0
        }
    }
    
    private fun saveAchievement(uuid: UUID, achievementId: String) {
        database.getConnection().use { conn ->
            conn.prepareStatement("""
                INSERT INTO charged_achievements (player_uuid, achievement_id, unlocked_at)
                VALUES (?, ?, ?)
            """).use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.setString(2, achievementId)
                stmt.setLong(3, System.currentTimeMillis())
                stmt.executeUpdate()
            }
        }
    }
}