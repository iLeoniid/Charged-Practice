package com.charged.events

import com.charged.Charged
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class EventManager(private val plugin: Charged) {
    
    private val activeEvents = ConcurrentHashMap<String, GameEvent>()
    
    enum class EventType {
        LMS,           // Last Man Standing
        OITC,          // One in the Chamber
        SUMO,          // Sumo Event
        GULAG,         // 1v1 Tournament
        PARKOUR,       // Parkour Race
        TNT_TAG,       // TNT Tag
        THIMBLE,       // Thimble/Target
        BRACKETS,      // Tournament Brackets
        SKYWARS,       // SkyWars Event
        KNOCKOUT,      // Knockout
        STOP_LIGHT,    // Red Light Green Light
        FOUR_CORNERS,  // Four Corners
        FFA            // Free For All
    }
    
    fun createEvent(type: EventType, maxPlayers: Int): GameEvent {
        val event = when (type) {
            EventType.LMS -> LMSEvent(plugin, maxPlayers)
            EventType.OITC -> OITCEvent(plugin, maxPlayers)
            EventType.SUMO -> SumoEvent(plugin, maxPlayers)
            EventType.GULAG -> GulagEvent(plugin, maxPlayers)
            EventType.PARKOUR -> ParkourEvent(plugin, maxPlayers)
            EventType.TNT_TAG -> TNTTagEvent(plugin, maxPlayers)
            EventType.THIMBLE -> ThimbleEvent(plugin, maxPlayers)
            EventType.BRACKETS -> BracketsEvent(plugin, maxPlayers)
            EventType.SKYWARS -> SkyWarsEvent(plugin, maxPlayers)
            EventType.KNOCKOUT -> KnockoutEvent(plugin, maxPlayers)
            EventType.STOP_LIGHT -> StopLightEvent(plugin, maxPlayers)
            EventType.FOUR_CORNERS -> FourCornersEvent(plugin, maxPlayers)
            EventType.FFA -> FFAEvent(plugin, maxPlayers)
        }
        
        activeEvents[event.id] = event
        return event
    }
    
    fun getEvent(id: String): GameEvent? = activeEvents[id]
    
    fun endEvent(id: String) {
        activeEvents[id]?.end()
        activeEvents.remove(id)
    }
}

abstract class GameEvent(
    protected val plugin: Charged,
    val maxPlayers: Int
) {
    val id = UUID.randomUUID().toString()
    val participants = mutableListOf<UUID>()
    var state = EventState.WAITING
    
    enum class EventState {
        WAITING,
        STARTING,
        ACTIVE,
        ENDING,
        FINISHED
    }
    
    abstract fun start()
    abstract fun end()
    abstract fun onPlayerJoin(player: Player)
    abstract fun onPlayerLeave(player: Player)
    abstract fun onPlayerDeath(player: Player, killer: Player?)
}

// LMS Event
class LMSEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    private val alivePlayers = mutableSetOf<UUID>()
    
    override fun start() {
        state = EventState.ACTIVE
        alivePlayers.addAll(participants)
        
        participants.forEach { uuid ->
            val player = plugin.server.getPlayer(uuid)
            player?.sendMessage("§a§lLast Man Standing started!")
            player?.sendMessage("§7Survive to win!")
        }
    }
    
    override fun end() {
        state = EventState.FINISHED
        
        val winner = alivePlayers.firstOrNull()
        if (winner != null) {
            val player = plugin.server.getPlayer(winner)
            plugin.server.broadcastMessage("§6${player?.name} §awon Last Man Standing!")
        }
    }
    
    override fun onPlayerJoin(player: Player) {
        if (participants.size < maxPlayers) {
            participants.add(player.uniqueId)
            player.sendMessage("§aYou joined Last Man Standing!")
        }
    }
    
    override fun onPlayerLeave(player: Player) {
        participants.remove(player.uniqueId)
        alivePlayers.remove(player.uniqueId)
    }
    
    override fun onPlayerDeath(player: Player, killer: Player?) {
        alivePlayers.remove(player.uniqueId)
        player.sendMessage("§cYou have been eliminated!")
        
        if (alivePlayers.size == 1) {
            end()
        }
    }
}

// OITC Event
class OITCEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    private val scores = ConcurrentHashMap<UUID, Int>()
    private val winScore = 25
    
    override fun start() {
        state = EventState.ACTIVE
        participants.forEach { scores[it] = 0 }
        
        // Give bow + arrow
        participants.forEach { uuid ->
            val player = plugin.server.getPlayer(uuid)
            player?.sendMessage("§a§lOne in the Chamber started!")
            // TODO: Give kit (bow, 1 arrow, sword)
        }
    }
    
    override fun end() {
        state = EventState.FINISHED
        
        val winner = scores.maxByOrNull { it.value }
        if (winner != null) {
            val player = plugin.server.getPlayer(winner.key)
            plugin.server.broadcastMessage("§6${player?.name} §awon OITC with ${winner.value} kills!")
        }
    }
    
    override fun onPlayerJoin(player: Player) {
        if (participants.size < maxPlayers) {
            participants.add(player.uniqueId)
            scores[player.uniqueId] = 0
        }
    }
    
    override fun onPlayerLeave(player: Player) {
        participants.remove(player.uniqueId)
        scores.remove(player.uniqueId)
    }
    
    override fun onPlayerDeath(player: Player, killer: Player?) {
        if (killer != null) {
            val currentScore = scores[killer.uniqueId] ?: 0
            scores[killer.uniqueId] = currentScore + 1
            
            // Give arrow
            killer.inventory.addItem(org.bukkit.inventory.ItemStack(org.bukkit.Material.ARROW, 1))
            
            if (currentScore + 1 >= winScore) {
                end()
            }
        }
        
        // Respawn player
        player.spigot().respawn()
    }
}

// Placeholder implementations for other events
class SumoEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}

class GulagEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}

class ParkourEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}

class TNTTagEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}

class ThimbleEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}

class BracketsEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}

class SkyWarsEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}

class KnockoutEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}

class StopLightEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}

class FourCornersEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}

class FFAEvent(plugin: Charged, maxPlayers: Int) : GameEvent(plugin, maxPlayers) {
    override fun start() { state = EventState.ACTIVE }
    override fun end() { state = EventState.FINISHED }
    override fun onPlayerJoin(player: Player) { participants.add(player.uniqueId) }
    override fun onPlayerLeave(player: Player) { participants.remove(player.uniqueId) }
    override fun onPlayerDeath(player: Player, killer: Player?) {}
}