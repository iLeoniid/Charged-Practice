package com.charged.kit

import com.charged.Charged
import com.charged.util.PluginAccess
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

class KitSettingsManager(private val plugin: Charged) {
    
    data class KitSettings(
        // Basic settings
        val naturalRegeneration: Boolean = false,
        val hungerEnabled: Boolean = false,
        val fallDamage: Boolean = true,
        val voidDamage: Boolean = true,
        val pvpDamage: Boolean = true,
        
        // Display
        val showHealth: Boolean = false,
        val damageTicks: Int = 10,
        
        // Building
        val buildEnabled: Boolean = false,
        val blockPlace: Boolean = true,
        val blockBreak: Boolean = true,
        val buildLimit: Int = 256,
        
        // Combat
        val knockbackProfile: String = "vanilla",
        val knockbackMultiplier: Double = 1.0,
        val damageMultiplier: Double = 1.0,
        val hitDelay: Int = 20,
        
        // Special mechanics
        val soupHealing: Double = 0.0,
        val respawnOnDeath: Boolean = false,
        val keepInventory: Boolean = false,
        val spawnInvincibility: Int = 0,
        
        // Potion effects
        val effects: List<PotionEffect> = emptyList()
    )
    
    fun applySettings(player: Player, settings: KitSettings) {
        // Natural regeneration
        player.setHealth(player.maxHealth)
        
        // Food level
        if (!settings.hungerEnabled) {
            player.foodLevel = 20
            player.saturation = 20f
        }
        
        // Show health
        if (settings.showHealth) {
            player.setHealthScaled(false)
        }
        
        // Potion effects
        settings.effects.forEach { effect ->
            player.addPotionEffect(effect)
        }
    }
    
    fun parseSettings(kitName: String): KitSettings {
        val config = PluginAccess.plugin().configManager
        val path = "kits.$kitName.settings"
        
        return KitSettings(
            naturalRegeneration = config.getBoolean("$path.natural-regeneration", false),
            hungerEnabled = config.getBoolean("$path.hunger-enabled", false),
            fallDamage = config.getBoolean("$path.fall-damage", true),
            voidDamage = config.getBoolean("$path.void-damage", true),
            pvpDamage = config.getBoolean("$path.pvp-damage", true),
            showHealth = config.getBoolean("$path.show-health", false),
            damageTicks = config.getInt("$path.damage-ticks", 10),
            buildEnabled = config.getBoolean("$path.build", false),
            blockPlace = config.getBoolean("$path.block-place", true),
            blockBreak = config.getBoolean("$path.block-break", true),
            buildLimit = config.getInt("$path.build-limit", 256),
            knockbackProfile = config.getString("$path.combat.knockback-profile", "vanilla"),
            knockbackMultiplier = config.getDouble("$path.combat.knockback-multiplier", 1.0),
            damageMultiplier = config.getDouble("$path.combat.damage-multiplier", 1.0),
            soupHealing = config.getDouble("$path.soup-healing", 0.0),
            respawnOnDeath = config.getBoolean("$path.respawn-on-death", false),
            keepInventory = config.getBoolean("$path.keep-inventory", false)
        )
    }
}