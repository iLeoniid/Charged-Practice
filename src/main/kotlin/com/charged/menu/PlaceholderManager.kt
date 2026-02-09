package com.charged.menu

import com.charged.Charged
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

class PlaceholderManager(private val plugin: Charged) {
    
    private val cache = ConcurrentHashMap<String, CachedValue>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy")
    
    /**
     * Replace all placeholders in text
     */
    fun replacePlaceholders(text: String, player: Player): String {
        var result = text
        
        // Find all placeholders
        val placeholderPattern = "\{([^}]+)}".toRegex()
        val matches = placeholderPattern.findAll(result)
        
        matches.forEach { match ->
            val placeholder = match.groupValues[1]
            val value = getPlaceholderValue(placeholder, player)
            result = result.replace("{$placeholder}", value)
        }
        
        // Color codes
        result = result.replace("&", "§")
        
        return result
    }
    
    private fun getPlaceholderValue(placeholder: String, player: Player): String {
        // Check cache first
        val cached = cache[placeholder]
        if (cached != null && !cached.isExpired()) {
            return cached.value
        }
        
        // Calculate value
        val value = when {
            // Player data
            placeholder.startsWith("player_") -> getPlayerPlaceholder(placeholder, player)
            
            // Queue data
            placeholder.startsWith("queue_") -> getQueuePlaceholder(placeholder)
            
            // Leaderboard data
            placeholder.startsWith("top_") -> getLeaderboardPlaceholder(placeholder)
            
            // Server data
            placeholder.startsWith("server_") -> getServerPlaceholder(placeholder)
            
            // Event data
            placeholder.startsWith("event_") -> getEventPlaceholder(placeholder)
            
            // Clan data
            placeholder.startsWith("clan_") -> getClanPlaceholder(placeholder, player)
            
            // Dynamic queries
            placeholder.startsWith("QUERY:") -> executeDynamicQuery(placeholder, player)
            
            // Date/Time
            "date" -> dateFormat.format(Date())
            "time" -> SimpleDateFormat("HH:mm").format(Date())
            
            else -> "{$placeholder}"
        }
        
        // Cache value if appropriate
        if (shouldCache(placeholder)) {
            cache[placeholder] = CachedValue(value, getCacheDuration(placeholder))
        }
        
        return value
    }
    
    private fun getPlayerPlaceholder(placeholder: String, player: Player): String {
        val stats = plugin.plugin.playerManager.getStats(player.uniqueId)
        
        return when (placeholder) {
            "player_name" -> player.name
            "player_level" -> (stats?.level ?: 1).toString()
            "player_elo" -> (stats?.elo ?: 1000).toString()
            "player_division" -> getDivisionName(stats?.elo ?: 1000)
            "player_division_colored" -> getDivisionColored(stats?.elo ?: 1000)
            "player_wins" -> (stats?.wins ?: 0).toString()
            "player_losses" -> (stats?.losses ?: 0).toString()
            "player_total_matches" -> ((stats?.wins ?: 0) + (stats?.losses ?: 0)).toString()
            "player_winrate" -> calculateWinrate(stats?.wins ?: 0, stats?.losses ?: 0)
            "player_kdr" -> calculateKDR(stats?.kills ?: 0, stats?.deaths ?: 0)
            "player_global_rank" -> getGlobalRank(player.uniqueId).toString()
            "player_division_rank" -> getDivisionRank(player.uniqueId).toString()
            "player_coins" -> (stats?.coins ?: 0).toString()
            "player_achievements" -> getAchievementCount(player.uniqueId).toString()
            "player_streak" -> (stats?.winstreak ?: 0).toString()
            "player_best_streak" -> (stats?.bestStreak ?: 0).toString()
            "level_progress" -> calculateLevelProgress(stats?.exp ?: 0)
            "exp_needed" -> calculateExpNeeded(stats?.level ?: 1)
            "elo_change" -> getRecentEloChange(player.uniqueId)
            else -> "?"
        }
    }
    
    private fun getQueuePlaceholder(placeholder: String): String {
        return when (placeholder) {
            "queue_count" -> plugin.plugin.queueManager.getTotalInQueue().toString()
            "queue_nodebuff" -> plugin.plugin.queueManager.getQueueSize("nodebuff").toString()
            "queue_gapple" -> plugin.plugin.queueManager.getQueueSize("gapple").toString()
            "queue_sumo" -> plugin.plugin.queueManager.getQueueSize("sumo").toString()
            "queue_builduhc" -> plugin.plugin.queueManager.getQueueSize("builduhc").toString()
            "avg_wait_time" -> "30" // TODO: Calculate
            else -> "0"
        }
    }
    
    private fun getLeaderboardPlaceholder(placeholder: String): String {
        val parts = placeholder.split("_")
        if (parts.size < 3) return "?"
        
        val position = parts[1].toIntOrNull() ?: return "?"
        val dataType = parts[2]
        
        val topPlayers = plugin.plugin.leaderboardManager.getTopPlayers(position)
        val player = topPlayers.getOrNull(position - 1) ?: return "-"
        
        return when (dataType) {
            "name" -> player.name
            "elo" -> player.elo.toString()
            else -> "?"
        }
    }
    
    private fun getServerPlaceholder(placeholder: String): String {
        return when (placeholder) {
            "server_tps" -> String.format("%.1f", plugin.server.tps[0])
            "server_uptime" -> calculateUptime()
            "online_players" -> Bukkit.getOnlinePlayers().size.toString()
            "max_players" -> Bukkit.getMaxPlayers().toString()
            "match_count" -> plugin.plugin.matchManager.getActiveMatchCount().toString()
            else -> "?"
        }
    }
    
    private fun getEventPlaceholder(placeholder: String): String {
        return when (placeholder) {
            "event_list_active" -> getActiveEventsList()
            "event_list_upcoming" -> getUpcomingEventsList()
            "active_events" -> plugin.plugin.eventManager.getActiveEventCount().toString()
            else -> "?"
        }
    }
    
    private fun getClanPlaceholder(placeholder: String, player: Player): String {
        val clan = plugin.plugin.clanManager.getPlayerClan(player.uniqueId) ?: return "-"
        
        return when (placeholder) {
            "clan_name" -> clan.name
            "clan_tag" -> clan.tag
            "clan_members" -> clan.members.size.toString()
            "clan_max" -> "20"
            "clan_elo" -> clan.elo.toString()
            "clan_rank" -> getClanRank(clan.id).toString()
            else -> "?"
        }
    }
    
    private fun executeDynamicQuery(placeholder: String, player: Player): String {
        // Format: QUERY:type:param1:param2
        val parts = placeholder.split(":")
        if (parts.size < 2) return ""
        
        val queryType = parts[1]
        val limit = parts.getOrNull(2)?.toIntOrNull() ?: 5
        
        return when (queryType) {
            "active_events" -> buildEventList(limit, true)
            "upcoming_events" -> buildEventList(limit, false)
            "leaderboard" -> buildLeaderboardList(limit)
            else -> ""
        }
    }
    
    // Helper methods
    
    private fun getDivisionName(elo: Int): String {
        return when {
            elo < 1000 -> "Iron"
            elo < 1300 -> "Bronze"
            elo < 1600 -> "Silver"
            elo < 1900 -> "Gold"
            elo < 2200 -> "Platinum"
            elo < 2500 -> "Diamond"
            elo < 2900 -> "Master"
            elo < 3300 -> "Grandmaster"
            else -> "Champion"
        }
    }
    
    private fun getDivisionColored(elo: Int): String {
        val name = getDivisionName(elo)
        val color = when (name) {
            "Iron" -> "§7"
            "Bronze" -> "§6"
            "Silver" -> "§f"
            "Gold" -> "§e"
            "Platinum" -> "§b"
            "Diamond" -> "§9"
            "Master" -> "§d"
            "Grandmaster" -> "§c"
            "Champion" -> "§4§l"
            else -> "§7"
        }
        return "$color$name"
    }
    
    private fun calculateWinrate(wins: Int, losses: Int): String {
        val total = wins + losses
        if (total == 0) return "0"
        return ((wins.toDouble() / total) * 100).toInt().toString()
    }
    
    private fun calculateKDR(kills: Int, deaths: Int): String {
        if (deaths == 0) return kills.toString()
        return String.format("%.2f", kills.toDouble() / deaths)
    }
    
    private fun getGlobalRank(uuid: java.util.UUID): Int {
        // TODO: Query from leaderboard
        return 1
    }
    
    private fun getDivisionRank(uuid: java.util.UUID): Int {
        // TODO: Query from leaderboard
        return 1
    }
    
    private fun getClanRank(clanId: String): Int {
        // TODO: Query from clan leaderboard
        return 1
    }
    
    private fun getAchievementCount(uuid: java.util.UUID): Int {
        return plugin.plugin.achievementManager.getUnlockedCount(uuid)
    }
    
    private fun calculateLevelProgress(exp: Int): String {
        val expForLevel = 1000 // TODO: Calculate from formula
        val progress = (exp % expForLevel).toDouble() / expForLevel * 100
        return progress.toInt().toString()
    }
    
    private fun calculateExpNeeded(level: Int): String {
        val expForLevel = level * 1000 // TODO: Proper formula
        return expForLevel.toString()
    }
    
    private fun getRecentEloChange(uuid: java.util.UUID): String {
        // TODO: Get from recent match
        return "+0"
    }
    
    private fun calculateUptime(): String {
        val uptime = System.currentTimeMillis() - plugin.startTime
        val hours = uptime / 3600000
        val minutes = (uptime % 3600000) / 60000
        return "${hours}h ${minutes}m"
    }
    
    private fun getActiveEventsList(): String {
        val events = plugin.plugin.eventManager.getActiveEvents()
        return events.take(3).joinToString("
") { 
            " §8▸ §7${it.name} §8[§a${it.participants}/${it.maxPlayers}§8]"
        }
    }
    
    private fun getUpcomingEventsList(): String {
        val events = plugin.plugin.eventManager.getUpcomingEvents()
        return events.take(2).joinToString("
") { 
            " §8▸ §7${it.name} §8- §f${it.timeUntil}"
        }
    }
    
    private fun buildEventList(limit: Int, active: Boolean): String {
        val events = if (active) {
            plugin.plugin.eventManager.getActiveEvents()
        } else {
            plugin.plugin.eventManager.getUpcomingEvents()
        }
        
        return events.take(limit).joinToString("
") { event ->
            " §8▸ §7${event.name} §8[§e${event.participants}/${event.maxPlayers}§8]"
        }
    }
    
    private fun buildLeaderboardList(limit: Int): String {
        val players = plugin.plugin.leaderboardManager.getTopPlayers(limit)
        return players.mapIndexed { index, player ->
            val rank = index + 1
            val medal = when (rank) {
                1 -> "§6#$rank"
                2 -> "§7#$rank"
                3 -> "§c#$rank"
                else -> "§f#$rank"
            }
            " §8▸ $medal §7${player.name} §8- §e${player.elo}"
        }.joinToString("
")
    }
    
    private fun shouldCache(placeholder: String): Boolean {
        return placeholder.startsWith("top_") || 
               placeholder.startsWith("server_") ||
               placeholder.startsWith("queue_")
    }
    
    private fun getCacheDuration(placeholder: String): Long {
        return when {
            placeholder.startsWith("queue_") -> 5000 // 5 seconds
            placeholder.startsWith("top_") -> 60000 // 1 minute
            placeholder.startsWith("server_") -> 10000 // 10 seconds
            else -> 30000 // 30 seconds
        }
    }
    
    fun clearCache() {
        cache.clear()
    }
}

data class CachedValue(
    val value: String,
    private val duration: Long,
    private val timestamp: Long = System.currentTimeMillis()
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > duration
    }
}

// Data classes for queries
data class TopPlayer(val name: String, val elo: Int)
data class EventInfo(val name: String, val participants: Int, val maxPlayers: Int, val timeUntil: String)