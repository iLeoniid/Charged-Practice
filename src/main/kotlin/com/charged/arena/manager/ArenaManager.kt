package com.charged.arena.manager

import com.charged.Charged
import com.charged.config.ConfigManager
import com.charged.arena.model.Arena
import com.charged.arena.model.ArenaState
import java.util.concurrent.ConcurrentHashMap

open class ArenaManager(
    private val plugin: Charged,
    private val config: ConfigManager
) {
    
    private val arenas = ConcurrentHashMap<String, Arena>()
    
    open fun getArenaCount() = arenas.size
    
    fun createArena(arena: Arena) {
        arenas[arena.name] = arena
        saveArenas()
    }
    
    fun deleteArena(name: String) {
        arenas.remove(name)
        saveArenas()
    }
    
    fun getArena(name: String): Arena? = arenas[name]
    
    fun getAllArenas(): List<Arena> = arenas.values.toList()
    
    fun getAvailableArena(mode: String): Arena? {
        return arenas.values
            .filter { it.mode.equals(mode, ignoreCase = true) }
            .firstOrNull { it.state == ArenaState.AVAILABLE }
    }
    
    fun markInUse(arena: Arena) {
        arena.state = ArenaState.IN_USE
    }
    
    fun markAvailable(arena: Arena) {
        arena.state = ArenaState.AVAILABLE
    }
    
    private fun saveArenas() {
        // Save arenas to file
    }
}