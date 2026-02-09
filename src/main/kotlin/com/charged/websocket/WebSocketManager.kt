package com.charged.websocket

import com.charged.Charged
import com.charged.match.model.Match
import com.google.gson.Gson
import org.bukkit.entity.Player
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.CompletableFuture

class WebSocketManager(private val plugin: Charged) {
    
    private val gson = Gson()
    private var websocketUrl = ""
    private var enabled = false
    
    fun loadConfig() {
        val config = plugin.plugin.configManager
        enabled = config.getBoolean("websocket.enabled", true)
        websocketUrl = config.getString("websocket.url", "http://localhost:8080")
        
        if (enabled) {
            plugin.logger.info("§a[WebSocket] Enabled - URL: $websocketUrl")
        }
    }
    
    /**
     * Send match update to WebSocket server
     */
    fun sendMatchUpdate(matchId: String, updateData: Map<String, Any>) {
        if (!enabled) return
        
        CompletableFuture.runAsync {
            try {
                val data = mapOf(
                    "type" to "match_update",
                    "matchId" to matchId,
                    "update" to updateData
                )
                
                postToWebSocket(data)
            } catch (e: Exception) {
                plugin.logger.warning("§c[WebSocket] Failed to send update: ${e.message}")
            }
        }
    }
    
    /**
     * Send live statistics during match
     */
    fun sendLiveStats(match: Match, player1: Player, player2: Player) {
        val data = mapOf(
            "player1" to mapOf(
                "name" to player1.name,
                "health" to player1.health,
                "ping" to getPing(player1),
                "location" to mapOf(
                    "x" to player1.location.x,
                    "y" to player1.location.y,
                    "z" to player1.location.z
                )
            ),
            "player2" to mapOf(
                "name" to player2.name,
                "health" to player2.health,
                "ping" to getPing(player2),
                "location" to mapOf(
                    "x" to player2.location.x,
                    "y" to player2.location.y,
                    "z" to player2.location.z
                )
            ),
            "duration" to ((System.currentTimeMillis() - match.startedAt) / 1000).toInt()
        )
        
        sendMatchUpdate(match.id, data)
    }
    
    /**
     * Send hit event
     */
    fun sendHitEvent(matchId: String, attacker: String, victim: String, damage: Double) {
        val data = mapOf(
            "type" to "hit",
            "attacker" to attacker,
            "victim" to victim,
            "damage" to damage,
            "timestamp" to System.currentTimeMillis()
        )
        
        sendMatchUpdate(matchId, data)
    }
    
    /**
     * Send combo event
     */
    fun sendComboEvent(matchId: String, player: String, comboCount: Int) {
        val data = mapOf(
            "type" to "combo",
            "player" to player,
            "hits" to comboCount,
            "timestamp" to System.currentTimeMillis()
        )
        
        sendMatchUpdate(matchId, data)
    }
    
    /**
     * Send chat message
     */
    fun sendChatMessage(matchId: String, sender: String, message: String) {
        val data = mapOf(
            "type" to "chat",
            "sender" to sender,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )
        
        sendMatchUpdate(matchId, data)
    }
    
    /**
     * End match notification
     */
    fun endMatch(matchId: String, winner: String) {
        if (!enabled) return
        
        CompletableFuture.runAsync {
            try {
                val data = mapOf(
                    "type" to "match_ended",
                    "matchId" to matchId,
                    "winner" to winner
                )
                
                postToWebSocket(data)
            } catch (e: Exception) {
                plugin.logger.warning("§c[WebSocket] Failed to end match: ${e.message}")
            }
        }
    }
    
    private fun postToWebSocket(data: Map<String, Any>) {
        val url = URL("$websocketUrl/api/update")
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val json = gson.toJson(data)
            OutputStreamWriter(connection.outputStream).use { it.write(json) }
            
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                plugin.logger.warning("§c[WebSocket] HTTP $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
    
    private fun getPing(player: Player): Int {
        return try {
            val craftPlayer = player::class.java
            val getHandle = craftPlayer.getMethod("getHandle")
            val entityPlayer = getHandle.invoke(player)
            val pingField = entityPlayer::class.java.getDeclaredField("ping")
            pingField.get(entityPlayer) as Int
        } catch (e: Exception) {
            0
        }
    }
}