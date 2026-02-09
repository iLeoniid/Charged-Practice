package com.charged.achievement.manager

import com.charged.Charged
import com.charged.database.Database
import com.charged.achievement.model.Achievement
import com.charged.achievement.model.AchievementCategory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class AchievementManager(
    private val plugin: Charged,
    private val database: Database
) {
    
    private val achievements = mutableListOf<Achievement>()
    private val playerAchievements = ConcurrentHashMap<UUID, MutableSet<String>>()
    
    init {
        loadAchievements()
    }
    
    private fun loadAchievements() {
        // Kills
        achievements.add(Achievement("first_blood", "First Blood", "Get your first kill", AchievementCategory.KILLS, 1, 50, 10))
        achievements.add(Achievement("killer_10", "Killer", "Get 10 kills", AchievementCategory.KILLS, 10, 100, 25))
        achievements.add(Achievement("slayer_50", "Slayer", "Get 50 kills", AchievementCategory.KILLS, 50, 250, 50))
        achievements.add(Achievement("assassin_100", "Assassin", "Get 100 kills", AchievementCategory.KILLS, 100, 500, 100))
        achievements.add(Achievement("destroyer_500", "Destroyer", "Get 500 kills", AchievementCategory.KILLS, 500, 1000, 250))
        achievements.add(Achievement("legend_1000", "Legend", "Get 1000 kills", AchievementCategory.KILLS, 1000, 2000, 500))
        
        // Wins
        achievements.add(Achievement("first_win", "First Victory", "Win your first match", AchievementCategory.WINS, 1, 50, 10))
        achievements.add(Achievement("winner_10", "Winner", "Win 10 matches", AchievementCategory.WINS, 10, 100, 25))
        achievements.add(Achievement("champion_50", "Champion", "Win 50 matches", AchievementCategory.WINS, 50, 250, 50))
        achievements.add(Achievement("master_100", "Master", "Win 100 matches", AchievementCategory.WINS, 100, 500, 100))
        
        // Streaks
        achievements.add(Achievement("streak_3", "Hot Streak", "Win 3 in a row", AchievementCategory.STREAKS, 3, 150, 30))
        achievements.add(Achievement("streak_5", "On Fire", "Win 5 in a row", AchievementCategory.STREAKS, 5, 300, 60))
        achievements.add(Achievement("streak_10", "Unstoppable", "Win 10 in a row", AchievementCategory.STREAKS, 10, 600, 120))
        
        // Modes
        achievements.add(Achievement("nodebuff_master", "NoDebuff Master", "Win 25 NoDebuff matches", AchievementCategory.MODES, 25, 300, 60))
        achievements.add(Achievement("gapple_master", "Gapple Master", "Win 25 Gapple matches", AchievementCategory.MODES, 25, 300, 60))
        achievements.add(Achievement("sumo_master", "Sumo Master", "Win 25 Sumo matches", AchievementCategory.MODES, 25, 300, 60))
        
        // Social
        achievements.add(Achievement("clan_creator", "Clan Creator", "Create a clan", AchievementCategory.SOCIAL, 1, 200, 40))
        achievements.add(Achievement("friend_10", "Friendly", "Have 10 friends", AchievementCategory.SOCIAL, 10, 150, 30))
        
        // Secret
        achievements.add(Achievement("secret_flawless", "Flawless Victory", "Win without taking damage", AchievementCategory.SECRET, 1, 500, 100, true))
        achievements.add(Achievement("secret_comeback", "Epic Comeback", "Win from 1 HP", AchievementCategory.SECRET, 1, 500, 100, true))
    }
    
    fun getAchievement(id: String): Achievement? = achievements.firstOrNull { it.id == id }
    
    fun getPlayerAchievements(uuid: UUID): Set<String> {
        return playerAchievements.getOrPut(uuid) { mutableSetOf() }
    }
    
    fun hasAchievement(uuid: UUID, achievementId: String): Boolean {
        return getPlayerAchievements(uuid).contains(achievementId)
    }
    
    fun unlockAchievement(uuid: UUID, achievementId: String): Boolean {
        val achievement = getAchievement(achievementId) ?: return false
        val playerAchs = playerAchievements.getOrPut(uuid) { mutableSetOf() }
        
        if (playerAchs.contains(achievementId)) {
            return false // Already unlocked
        }
        
        playerAchs.add(achievementId)
        saveAchievement(uuid, achievementId)
        
        // Notify player
        val player = plugin.server.getPlayer(uuid)
        if (player != null) {
            player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━")
            player.sendMessage("§e§lACHIEVEMENT UNLOCKED!")
            player.sendMessage("§f${achievement.name}")
            player.sendMessage("§7${achievement.description}")
            player.sendMessage("§6+${achievement.rewardXp} XP §8| §6+${achievement.rewardCoins} Coins")
            player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━")
            player.playSound(player.location, org.bukkit.Sound.LEVEL_UP, 1f, 1f)
        }
        
        return true
    }
    
    fun checkAchievements(uuid: UUID) {
        val stats = plugin.plugin.playerManager.getStats(uuid) ?: return
        
        // Check kill achievements
        when (stats.wins) {
            1 -> unlockAchievement(uuid, "first_win")
            10 -> unlockAchievement(uuid, "winner_10")
            50 -> unlockAchievement(uuid, "champion_50")
            100 -> unlockAchievement(uuid, "master_100")
        }
        
        // Check streak achievements
        when (stats.winstreak) {
            3 -> unlockAchievement(uuid, "streak_3")
            5 -> unlockAchievement(uuid, "streak_5")
            10 -> unlockAchievement(uuid, "streak_10")
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