package com.charged.achievement.manager

import com.charged.Charged
import com.charged.database.Database
import com.charged.achievement.model.Achievement
import com.charged.achievement.model.AchievementCategory
import com.charged.achievement.model.AchievementReward
import com.charged.util.PluginAccess
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
        achievements.add(Achievement(
            "first_blood",
            "First Blood",
            "Get your first kill",
            AchievementCategory.KILLS,
            1,
            AchievementReward(50, 10)
        ))

        achievements.add(Achievement(
            "killer_10",
            "Killer",
            "Get 10 kills",
            AchievementCategory.KILLS,
            10,
            AchievementReward(100, 25)
        ))

        achievements.add(Achievement(
            "slayer_50",
            "Slayer",
            "Get 50 kills",
            AchievementCategory.KILLS,
            50,
            AchievementReward(250, 50)
        ))

        achievements.add(Achievement(
            "assassin_100",
            "Assassin",
            "Get 100 kills",
            AchievementCategory.KILLS,
            100,
            AchievementReward(500, 100)
        ))

        achievements.add(Achievement(
            "destroyer_500",
            "Destroyer",
            "Get 500 kills",
            AchievementCategory.KILLS,
            500,
            AchievementReward(1000, 250)
        ))

        achievements.add(Achievement(
            "legend_1000",
            "Legend",
            "Get 1000 kills",
            AchievementCategory.KILLS,
            1000,
            AchievementReward(2000, 500)
        ))

        // Wins
        achievements.add(Achievement(
            "first_win",
            "First Victory",
            "Win your first match",
            AchievementCategory.WINS,
            1,
            AchievementReward(50, 10)
        ))

        achievements.add(Achievement(
            "winner_10",
            "Winner",
            "Win 10 matches",
            AchievementCategory.WINS,
            10,
            AchievementReward(100, 25)
        ))

        achievements.add(Achievement(
            "champion_50",
            "Champion",
            "Win 50 matches",
            AchievementCategory.WINS,
            50,
            AchievementReward(250, 50)
        ))

        achievements.add(Achievement(
            "master_100",
            "Master",
            "Win 100 matches",
            AchievementCategory.WINS,
            100,
            AchievementReward(500, 100)
        ))

        // Streaks
        achievements.add(Achievement(
            "streak_3",
            "Hot Streak",
            "Win 3 in a row",
            AchievementCategory.STREAKS,
            3,
            AchievementReward(150, 30)
        ))

        achievements.add(Achievement(
            "streak_5",
            "On Fire",
            "Win 5 in a row",
            AchievementCategory.STREAKS,
            5,
            AchievementReward(300, 60)
        ))

        achievements.add(Achievement(
            "streak_10",
            "Unstoppable",
            "Win 10 in a row",
            AchievementCategory.STREAKS,
            10,
            AchievementReward(600, 120)
        ))

        // Modes
        achievements.add(Achievement(
            "nodebuff_master",
            "NoDebuff Master",
            "Win 25 NoDebuff matches",
            AchievementCategory.MODES,
            25,
            AchievementReward(300, 60)
        ))

        achievements.add(Achievement(
            "gapple_master",
            "Gapple Master",
            "Win 25 Gapple matches",
            AchievementCategory.MODES,
            25,
            AchievementReward(300, 60)
        ))

        achievements.add(Achievement(
            "sumo_master",
            "Sumo Master",
            "Win 25 Sumo matches",
            AchievementCategory.MODES,
            25,
            AchievementReward(300, 60)
        ))

        // Social
        achievements.add(Achievement(
            "clan_creator",
            "Clan Creator",
            "Create a clan",
            AchievementCategory.SOCIAL,
            1,
            AchievementReward(200, 40)
        ))

        achievements.add(Achievement(
            "friend_10",
            "Friendly",
            "Have 10 friends",
            AchievementCategory.SOCIAL,
            10,
            AchievementReward(150, 30)
        ))

        // Secret achievements (hidden until unlocked)
        achievements.add(Achievement(
            "secret_flawless",
            "Flawless Victory",
            "Win without taking damage",
            AchievementCategory.SECRET,
            1,
            AchievementReward(500, 100),
            true
        ))

        achievements.add(Achievement(
            "secret_comeback",
            "Epic Comeback",
            "Win from 1 HP",
            AchievementCategory.SECRET,
            1,
            AchievementReward(500, 100),
            true
        ))

        plugin.logger.info("§a[Achievements] ${achievements.size} achievements loaded")
    }

    fun getAchievement(id: String): Achievement? = achievements.firstOrNull { it.id == id }

    fun getAllAchievements(): List<Achievement> = achievements.toList()

    fun getAchievementsByCategory(category: AchievementCategory): List<Achievement> {
        return achievements.filter { it.category == category }
    }

    fun getPlayerAchievements(uuid: UUID): Set<String> {
        return playerAchievements.getOrPut(uuid) { mutableSetOf() }
    }

    fun getPlayerCompletedAchievements(uuid: UUID): List<Achievement> {
        val unlockedIds = getPlayerAchievements(uuid)
        return achievements.filter { unlockedIds.contains(it.id) }
    }

    fun getPlayerProgress(uuid: UUID, achievementId: String): Pair<Int, Int>? {
        val achievement = getAchievement(achievementId) ?: return null
        val stats = PluginAccess.plugin().playerManager.getStats(uuid) ?: return null

        val current = when (achievement.category) {
            AchievementCategory.KILLS -> stats.kills
            AchievementCategory.WINS -> stats.wins
            AchievementCategory.STREAKS -> stats.winstreak
            AchievementCategory.MODES -> getModeWins(uuid, achievementId)
            AchievementCategory.SOCIAL -> getSocialProgress(uuid, achievementId)
            AchievementCategory.SECRET -> 0 // Secret achievements are special
        }

        return Pair(current, achievement.requirement)
    }

    private fun getModeWins(uuid: UUID, achievementId: String): Int {
        // TODO: Implement mode-specific win tracking
        return 0
    }

    private fun getSocialProgress(uuid: UUID, achievementId: String): Int {
        // TODO: Implement social progress tracking
        return 0
    }

    fun hasAchievement(uuid: UUID, achievementId: String): Boolean {
        return getPlayerAchievements(uuid).contains(achievementId)
    }

    fun unlockAchievement(uuid: UUID, achievementId: String): Boolean {
        val achievement = getAchievement(achievementId) ?: return false

        if (hasAchievement(uuid, achievementId)) {
            return false // Already unlocked
        }

        val playerAchs = playerAchievements.getOrPut(uuid) { mutableSetOf() }
        playerAchs.add(achievementId)

        saveAchievement(uuid, achievementId)

        // Apply rewards
        applyRewards(uuid, achievement.reward)

        // Notify player
        notifyAchievementUnlock(uuid, achievement)

        // Broadcast if enabled
        if (PluginAccess.plugin().configManager.getBoolean("achievements.broadcast", true)) {
            broadcastAchievement(uuid, achievement)
        }

        return true
    }

    fun unlockAchievementSilently(uuid: UUID, achievementId: String): Boolean {
        val achievement = getAchievement(achievementId) ?: return false

        if (hasAchievement(uuid, achievementId)) {
            return false
        }

        val playerAchs = playerAchievements.getOrPut(uuid) { mutableSetOf() }
        playerAchs.add(achievementId)

        saveAchievement(uuid, achievementId)
        applyRewards(uuid, achievement.reward)

        return true
    }

    private fun applyRewards(uuid: UUID, reward: AchievementReward) {
        PluginAccess.getPlayerManager().rewardXp(uuid, reward.xp, "achievement")
        PluginAccess.getPlayerManager().rewardCoins(uuid, reward.coins, "achievement")
    }

    private fun notifyAchievementUnlock(uuid: UUID, achievement: Achievement) {
        val player = plugin.server.getPlayer(uuid) ?: return

        player.sendMessage("")
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        player.sendMessage("§e§l   ACHIEVEMENT UNLOCKED!")
        player.sendMessage("")
        player.sendMessage("§f§l${achievement.name}")
        player.sendMessage("§7${achievement.description}")
        player.sendMessage("")
        player.sendMessage("§6+${achievement.reward.xp} XP")
        player.sendMessage("§6+${achievement.reward.coins} Coins")
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
        player.sendMessage("")

        player.playSound(player.location, org.bukkit.Sound.LEVEL_UP, 1f, 1f)
        player.sendTitle("§e§lACHIEVEMENT!", "§f${achievement.name}", 10, 70, 20)
    }

    private fun broadcastAchievement(uuid: UUID, achievement: Achievement) {
        val player = plugin.server.getPlayer(uuid) ?: return

        if (achievement.secret) return // Don't broadcast secret achievements

        val message = """
            §6§l━━━━━━━━━━━━━━━━━━━━━━━━━━
            §a§l   ACHIEVEMENT UNLOCKED!
            §7${player.name} §fearned §e${achievement.name}
            §7${achievement.description}
            §6§l━━━━━━━━━━━━━━━━━━━━━━━━━━
        """.trimIndent()

        plugin.server.onlinePlayers.forEach {
            if (it.uniqueId != uuid) {
                it.sendMessage(message)
            }
        }
    }

    fun checkAchievements(uuid: UUID) {
        val stats = PluginAccess.plugin().playerManager.getStats(uuid) ?: return

        // Check kill achievements
        when {
            stats.kills >= 1 && !hasAchievement(uuid, "first_blood") ->
                unlockAchievement(uuid, "first_blood")
            stats.kills >= 10 && !hasAchievement(uuid, "killer_10") ->
                unlockAchievement(uuid, "killer_10")
            stats.kills >= 50 && !hasAchievement(uuid, "slayer_50") ->
                unlockAchievement(uuid, "slayer_50")
            stats.kills >= 100 && !hasAchievement(uuid, "assassin_100") ->
                unlockAchievement(uuid, "assassin_100")
            stats.kills >= 500 && !hasAchievement(uuid, "destroyer_500") ->
                unlockAchievement(uuid, "destroyer_500")
            stats.kills >= 1000 && !hasAchievement(uuid, "legend_1000") ->
                unlockAchievement(uuid, "legend_1000")
        }

        // Check win achievements
        when {
            stats.wins >= 1 && !hasAchievement(uuid, "first_win") ->
                unlockAchievement(uuid, "first_win")
            stats.wins >= 10 && !hasAchievement(uuid, "winner_10") ->
                unlockAchievement(uuid, "winner_10")
            stats.wins >= 50 && !hasAchievement(uuid, "champion_50") ->
                unlockAchievement(uuid, "champion_50")
            stats.wins >= 100 && !hasAchievement(uuid, "master_100") ->
                unlockAchievement(uuid, "master_100")
        }

        // Check streak achievements
        when {
            stats.winstreak >= 3 && !hasAchievement(uuid, "streak_3") ->
                unlockAchievement(uuid, "streak_3")
            stats.winstreak >= 5 && !hasAchievement(uuid, "streak_5") ->
                unlockAchievement(uuid, "streak_5")
            stats.winstreak >= 10 && !hasAchievement(uuid, "streak_10") ->
                unlockAchievement(uuid, "streak_10")
        }

        // Check if player created a clan
        val clan = PluginAccess.getClanManager().getPlayerClan(uuid)
        if (clan != null && clan.ownerUuid == uuid && !hasAchievement(uuid, "clan_creator")) {
            unlockAchievement(uuid, "clan_creator")
        }
    }

    fun checkSecretAchievements(uuid: UUID, matchId: String, stats: com.charged.match.manager.MatchStats?) {
        // Check for Flawless Victory (win without taking damage)
        if (stats != null && stats.winner == uuid) {
            val playerHealth = if (uuid == stats.player1) stats.player1Health else stats.player2Health
            val hitsTaken = if (uuid == stats.player1) stats.player1HitsTaken else stats.player2HitsTaken

            if (playerHealth >= 20.0 && hitsTaken == 0 && !hasAchievement(uuid, "secret_flawless")) {
                unlockAchievement(uuid, "secret_flawless")
            }

            // Check for Epic Comeback (win from 1 HP)
            if (playerHealth <= 1.0 && !hasAchievement(uuid, "secret_comeback")) {
                unlockAchievement(uuid, "secret_comeback")
            }
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

    fun loadPlayerAchievements(uuid: UUID) {
        database.getConnection().use { conn ->
            conn.prepareStatement("SELECT achievement_id FROM charged_achievements WHERE player_uuid = ?")
                .use { stmt ->
                    stmt.setString(1, uuid.toString())
                    val rs = stmt.executeQuery()
                    val playerAchs = playerAchievements.getOrPut(uuid) { mutableSetOf() }

                    while (rs.next()) {
                        playerAchs.add(rs.getString("achievement_id"))
                    }
                }
        }
    }

    fun getAchievementProgress(uuid: UUID): AchievementProgress {
        val total = achievements.size
        val completed = getPlayerAchievements(uuid).size
        val progress = if (total > 0) (completed.toDouble() / total * 100).toInt() else 0

        return AchievementProgress(
            completed = completed,
            total = total,
            progress = progress
        )
    }

    fun getPlayerAchievementPoints(uuid: UUID): Int {
        return getPlayerCompletedAchievements(uuid)
            .sumOf { it.reward.xp / 10 } // Convert XP to points
    }

    // Método para debug/administración
    fun getAchievementStats(): AchievementStats {
        val totalPlayers = PluginAccess.get().server.onlinePlayers.size
        val totalUnlocks = playerAchievements.values.sumOf { it.size }

        val mostCommon = playerAchievements.entries
            .flatMap { it.value }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }

        val mostCommonAchievement = mostCommon?.let {
            val achievement = getAchievement(it.key)
            Pair(achievement?.name ?: "Unknown", it.value)
        }

        return AchievementStats(
            totalAchievements = achievements.size,
            totalPlayers = totalPlayers,
            totalUnlocks = totalUnlocks,
            mostCommonAchievement = mostCommonAchievement
        )
    }
}

// Data classes adicionales
data class AchievementProgress(
    val completed: Int,
    val total: Int,
    val progress: Int
)

data class AchievementStats(
    val totalAchievements: Int,
    val totalPlayers: Int,
    val totalUnlocks: Int,
    val mostCommonAchievement: Pair<String, Int>?
)