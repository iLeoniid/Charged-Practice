package com.charged.language

import com.charged.Charged
import com.charged.config.ConfigManager
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class LanguageManager(
    private val plugin: Charged,
    private val config: ConfigManager
) {
    
    private val playerLanguages = ConcurrentHashMap<UUID, String>()
    
    fun autoDetect(player: Player) {
        // Default to Spanish
        playerLanguages[player.uniqueId] = "es_ES"
    }
    
    fun translate(key: String, placeholders: Map<String, String> = emptyMap()): String {
        var text = key // Fallback to key
        
        // Apply placeholders
        placeholders.forEach { (placeholder, value) ->
            text = text.replace(placeholder, value)
        }
        
        return text
    }
}