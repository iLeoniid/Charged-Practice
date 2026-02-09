package com.charged.knockback

import com.charged.Charged
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class KnockbackController(private val plugin: Charged) : Listener {
    
    private val profiles = ConcurrentHashMap<String, KnockbackProfile>()
    private val playerProfiles = ConcurrentHashMap<UUID, String>()
    
    init {
        loadProfiles()
    }
    
    private fun loadProfiles() {
        // Vanilla
        profiles["vanilla"] = KnockbackProfile(
            name = "Vanilla",
            horizontal = 0.4,
            vertical = 0.4,
            extraHorizontal = 0.5,
            extraVertical = 0.1,
            friction = 2.0
        )
        
        // Practice
        profiles["practice"] = KnockbackProfile(
            name = "Practice",
            horizontal = 0.38,
            vertical = 0.36,
            extraHorizontal = 0.45,
            extraVertical = 0.08,
            friction = 2.0
        )
        
        // Soup
        profiles["soup"] = KnockbackProfile(
            name = "Soup",
            horizontal = 0.35,
            vertical = 0.35,
            extraHorizontal = 0.4,
            extraVertical = 0.05,
            friction = 2.0
        )
        
        // Combo
        profiles["combo"] = KnockbackProfile(
            name = "Combo",
            horizontal = 0.42,
            vertical = 0.38,
            extraHorizontal = 0.6,
            extraVertical = 0.12,
            friction = 1.8
        )
        
        // NoDebuff
        profiles["nodebuff"] = KnockbackProfile(
            name = "NoDebuff",
            horizontal = 0.4,
            vertical = 0.385,
            extraHorizontal = 0.5,
            extraVertical = 0.085,
            friction = 2.0
        )
    }
    
    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val victim = event.entity as? Player ?: return
        
        val profileName = playerProfiles[victim.uniqueId] ?: "vanilla"
        val profile = profiles[profileName] ?: return
        
        // Cancel default knockback
        event.damage = 0.0
        
        // Apply custom knockback
        plugin.server.scheduler.runTask(plugin, Runnable {
            applyKnockback(victim, damager, profile)
        })
    }
    
    private fun applyKnockback(victim: Player, damager: Player, profile: KnockbackProfile) {
        val direction = victim.location.toVector()
            .subtract(damager.location.toVector())
            .normalize()
        
        // Calculate base knockback
        var knockback = Vector(
            direction.x * profile.horizontal,
            profile.vertical,
            direction.z * profile.horizontal
        )
        
        // Add sprint knockback
        if (damager.isSprinting) {
            knockback.add(Vector(
                direction.x * profile.extraHorizontal,
                profile.extraVertical,
                direction.z * profile.extraHorizontal
            ))
        }
        
        // Apply friction
        val currentVelocity = victim.velocity
        knockback.add(Vector(
            currentVelocity.x / profile.friction,
            0.0,
            currentVelocity.z / profile.friction
        ))
        
        victim.velocity = knockback
    }
    
    fun setProfile(player: UUID, profileName: String) {
        if (profiles.containsKey(profileName)) {
            playerProfiles[player] = profileName
        }
    }
    
    fun getProfile(profileName: String): KnockbackProfile? = profiles[profileName]
    
    fun getAllProfiles(): Map<String, KnockbackProfile> = profiles.toMap()
}

data class KnockbackProfile(
    val name: String,
    val horizontal: Double,
    val vertical: Double,
    val extraHorizontal: Double,
    val extraVertical: Double,
    val friction: Double
)