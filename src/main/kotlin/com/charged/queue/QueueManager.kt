package com.charged.queue

import com.charged.Charged
import com.charged.match.model.MatchType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class QueueManager(private val plugin: Charged) {
    
    private val queues = ConcurrentHashMap<String, MutableSet<UUID>>()
    
    init {
        startMatchmaking()
    }
    
    fun joinQueue(uuid: UUID, mode: String) {
        queues.getOrPut(mode) { mutableSetOf() }.add(uuid)
        
        val player = plugin.server.getPlayer(uuid)
        player?.sendMessage("§eBuscando oponente...")
    }
    
    fun leaveQueue(uuid: UUID) {
        queues.values.forEach { it.remove(uuid) }
    }
    
    fun isInQueue(uuid: UUID): Boolean {
        return queues.values.any { it.contains(uuid) }
    }
    
    fun clearAll() {
        queues.clear()
    }
    
    private fun startMatchmaking() {
        plugin.server.scheduler.runTaskRepeatAsynchronously(plugin, Runnable {
            matchmakePlayers()
        }, 20L, 20L) // Every second
    }
    
    private fun matchmakePlayers() {
        queues.forEach { (mode, players) ->
            if (players.size < 2) return@forEach
            
            val p1 = players.firstOrNull() ?: return@forEach
            players.remove(p1)
            
            val p2 = players.firstOrNull() ?: run {
                players.add(p1) // Re-add
                return@forEach
            }
            players.remove(p2)
            
            // Create match
            plugin.server.scheduler.runTask(plugin, Runnable {
                plugin.plugin.matchManager.createMatch(
                    mode = mode,
                    type = MatchType.UNRANKED,
                    players = listOf(p1, p2)
                )
            })
        }
    }
}