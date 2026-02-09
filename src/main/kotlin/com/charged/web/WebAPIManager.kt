package com.charged.web

import com.charged.Charged
import com.charged.database.Database
import com.charged.match.model.Match
import com.charged.player.model.PlayerStats
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.CompletableFuture

class WebAPIManager(
    private val plugin: Charged,
    private val database: Database
) {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    private var apiUrl: String = ""
    private var apiKey: String = ""
    private var enabled: Boolean = false
    
    fun loadConfig() {
        val config = plugin.plugin.configManager
        enabled = config.getBoolean("web.enabled", true)
        apiUrl = config.getString("web.domain", "https://practice.tudominio.com")
        apiKey = config.getString("web.api_key", "")
        
        if (enabled) {
            plugin.logger.info("§a[WebAPI] Enabled - Domain: $apiUrl")
        }
    }
    
    fun sendMatchData(match: Match, winner: UUID?, loser: UUID?) {
        if (!enabled) return
        
        CompletableFuture.runAsync {
            try {
                val matchData = buildMatchData(match, winner, loser)
                val response = postToAPI("/api/v2/match", matchData)
                
                if (response != null) {
                    val matchId = gson.fromJson(response, MatchResponse::class.java).matchId
                    val matchUrl = "$apiUrl/duel/$matchId"
                    
                    // Send URL to players
                    match.participants.forEach { uuid ->
                        val player = plugin.server.getPlayer(uuid)
                        player?.sendMessage("")
                        player?.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        player?.sendMessage("§e§l  MATCH STATISTICS")
                        player?.sendMessage("")
                        player?.sendMessage("§7  View detailed analysis:")
                        player?.sendMessage("§b  $matchUrl")
                        player?.sendMessage("")
                        player?.sendMessage("§7  Click to open in browser!")
                        player?.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
                        player?.sendMessage("")
                    }
                    
                    // Send to Discord webhook
                    sendDiscordWebhook(matchData, matchUrl)
                }
            } catch (e: Exception) {
                plugin.logger.warning("§c[WebAPI] Failed to send match data: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private fun buildMatchData(match: Match, winner: UUID?, loser: UUID?): MatchData {
        val p1Stats = plugin.plugin.playerManager.getStats(match.participants[0])
        val p2Stats = plugin.plugin.playerManager.getStats(match.participants[1])
        
        val p1 = plugin.server.getPlayer(match.participants[0])
        val p2 = plugin.server.getPlayer(match.participants[1])
        
        return MatchData(
            matchId = match.id,
            timestamp = System.currentTimeMillis(),
            mode = match.mode,
            type = match.type.name,
            duration = ((match.endedAt - match.startedAt) / 1000).toInt(),
            
            player1 = PlayerMatchData(
                uuid = match.participants[0].toString(),
                username = p1?.name ?: "Unknown",
                preMatch = PreMatchData(
                    elo = p1Stats?.elo ?: 1000,
                    division = getDivisionName(p1Stats?.elo ?: 1000),
                    winStreak = p1Stats?.winstreak ?: 0
                ),
                postMatch = PostMatchData(
                    elo = if (winner == match.participants[0]) (p1Stats?.elo ?: 1000) + 25 else (p1Stats?.elo ?: 1000) - 25,
                    eloChange = if (winner == match.participants[0]) 25 else -25
                ),
                statistics = MatchStatistics(
                    hitsGiven = 42, // TODO: Track from match
                    hitsTaken = 38,
                    maxCombo = 8,
                    averageCPS = 12.5,
                    criticalHits = 15,
                    potionsUsed = 3
                )
            ),
            
            player2 = PlayerMatchData(
                uuid = match.participants[1].toString(),
                username = p2?.name ?: "Unknown",
                preMatch = PreMatchData(
                    elo = p2Stats?.elo ?: 1000,
                    division = getDivisionName(p2Stats?.elo ?: 1000),
                    winStreak = p2Stats?.winstreak ?: 0
                ),
                postMatch = PostMatchData(
                    elo = if (winner == match.participants[1]) (p2Stats?.elo ?: 1000) + 25 else (p2Stats?.elo ?: 1000) - 25,
                    eloChange = if (winner == match.participants[1]) 25 else -25
                ),
                statistics = MatchStatistics(
                    hitsGiven = 38,
                    hitsTaken = 42,
                    maxCombo = 5,
                    averageCPS = 11.3,
                    criticalHits = 12,
                    potionsUsed = 4
                )
            ),
            
            winner = winner?.toString(),
            arena = "temple" // TODO: Get from match
        )
    }
    
    private fun postToAPI(endpoint: String, data: Any): String? {
        val url = URL(apiUrl + endpoint)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true
            
            val json = gson.toJson(data)
            OutputStreamWriter(connection.outputStream).use { it.write(json) }
            
            val responseCode = connection.responseCode
            if (responseCode == 200 || responseCode == 201) {
                return connection.inputStream.bufferedReader().readText()
            } else {
                plugin.logger.warning("§c[WebAPI] API returned code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
        
        return null
    }
    
    private fun sendDiscordWebhook(matchData: MatchData, matchUrl: String) {
        val webhookUrl = plugin.plugin.configManager.getString("web.discord.webhook.url", "")
        if (webhookUrl.isEmpty()) return
        
        val webhook = DiscordWebhook(
            content = null,
            embeds = listOf(
                DiscordEmbed(
                    title = "⚔️ ${matchData.mode} Match • ${matchData.player1.username} vs ${matchData.player2.username}",
                    description = buildDiscordDescription(matchData, matchUrl),
                    color = 7109863, // #6C5CE7
                    fields = listOf(
                        DiscordField(
                            name = "📊 Statistics",
                            value = buildStatsField(matchData),
                            inline = true
                        ),
                        DiscordField(
                            name = "🎯 Performance",
                            value = buildPerformanceField(matchData),
                            inline = true
                        )
                    ),
                    footer = DiscordFooter(
                        text = "Charged Practice • Advanced Statistics",
                        iconUrl = "https://i.imgur.com/yourlogo.png"
                    ),
                    timestamp = java.time.Instant.now().toString()
                )
            )
        )
        
        postToAPI(webhookUrl, webhook)
    }
    
    private fun buildDiscordDescription(data: MatchData, url: String): String {
        val winner = if (data.player1.postMatch.eloChange > 0) data.player1 else data.player2
        val loser = if (data.player1.postMatch.eloChange < 0) data.player1 else data.player2
        
        return """
            **Winner:** ${winner.username} (${winner.preMatch.elo} → ${winner.postMatch.elo}) **${if (winner.postMatch.eloChange > 0) "+" else ""}${winner.postMatch.eloChange}**
            **Loser:** ${loser.username} (${loser.preMatch.elo} → ${loser.postMatch.elo}) **${loser.postMatch.eloChange}**
            
            **Duration:** ${data.duration}s
            **Stats:** [View Full Analysis]($url)
        """.trimIndent()
    }
    
    private fun buildStatsField(data: MatchData): String {
        return """
            Hits: ${data.player1.statistics.hitsGiven} - ${data.player2.statistics.hitsGiven}
            Combo: ${data.player1.statistics.maxCombo} - ${data.player2.statistics.maxCombo}
            Pots: ${data.player1.statistics.potionsUsed} - ${data.player2.statistics.potionsUsed}
        """.trimIndent()
    }
    
    private fun buildPerformanceField(data: MatchData): String {
        return """
            CPS: ${data.player1.statistics.averageCPS} - ${data.player2.statistics.averageCPS}
            Crits: ${data.player1.statistics.criticalHits} - ${data.player2.statistics.criticalHits}
        """.trimIndent()
    }
    
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
}

// Data classes
data class MatchData(
    val matchId: String,
    val timestamp: Long,
    val mode: String,
    val type: String,
    val duration: Int,
    val player1: PlayerMatchData,
    val player2: PlayerMatchData,
    val winner: String?,
    val arena: String
)

data class PlayerMatchData(
    val uuid: String,
    val username: String,
    val preMatch: PreMatchData,
    val postMatch: PostMatchData,
    val statistics: MatchStatistics
)

data class PreMatchData(
    val elo: Int,
    val division: String,
    val winStreak: Int
)

data class PostMatchData(
    val elo: Int,
    val eloChange: Int
)

data class MatchStatistics(
    val hitsGiven: Int,
    val hitsTaken: Int,
    val maxCombo: Int,
    val averageCPS: Double,
    val criticalHits: Int,
    val potionsUsed: Int
)

data class MatchResponse(
    val matchId: String,
    val url: String
)

// Discord webhook classes
data class DiscordWebhook(
    val content: String?,
    val embeds: List<DiscordEmbed>
)

data class DiscordEmbed(
    val title: String,
    val description: String,
    val color: Int,
    val fields: List<DiscordField>,
    val footer: DiscordFooter,
    val timestamp: String
)

data class DiscordField(
    val name: String,
    val value: String,
    val inline: Boolean
)

data class DiscordFooter(
    val text: String,
    val iconUrl: String
)