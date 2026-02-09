package com.charged.cosmetics

import com.charged.Charged
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos
import kotlin.math.sin

class ParticleEffectManager(private val plugin: Charged) {
    
    private val playerEffects = ConcurrentHashMap<UUID, DeathEffect>()
    
    enum class DeathEffect {
        EXPLOSION,
        LIGHTNING,
        BLOOD,
        FLAMES,
        HEARTS,
        NOTES,
        SNOW,
        SPIRAL,
        FIREWORK,
        VOID,
        CUSTOM
    }
    
    fun setPlayerEffect(player: Player, effect: DeathEffect) {
        playerEffects[player.uniqueId] = effect
        player.sendMessage("§aParticle death effect set to: §f${effect.name}")
    }
    
    fun playDeathEffect(player: Player, killer: Player?) {
        val effect = playerEffects[player.uniqueId] ?: DeathEffect.EXPLOSION
        val location = player.location
        
        when (effect) {
            DeathEffect.EXPLOSION -> playExplosion(location)
            DeathEffect.LIGHTNING -> playLightning(location)
            DeathEffect.BLOOD -> playBlood(location)
            DeathEffect.FLAMES -> playFlames(location)
            DeathEffect.HEARTS -> playHearts(location)
            DeathEffect.NOTES -> playNotes(location)
            DeathEffect.SNOW -> playSnow(location)
            DeathEffect.SPIRAL -> playSpiral(location)
            DeathEffect.FIREWORK -> playFirework(location)
            DeathEffect.VOID -> playVoid(location)
            DeathEffect.CUSTOM -> playCustom(location, player, killer)
        }
    }
    
    private fun playExplosion(loc: Location) {
        loc.world?.playEffect(loc, Effect.EXPLOSION_HUGE, null)
        loc.world?.playSound(loc, Sound.EXPLODE, 1f, 1f)
        
        // Particle burst
        for (i in 0..20) {
            val offset = Vector(
                (Math.random() - 0.5) * 2,
                (Math.random() - 0.5) * 2,
                (Math.random() - 0.5) * 2
            )
            loc.world?.playEffect(loc.clone().add(offset), Effect.SMOKE, null)
        }
    }
    
    private fun playLightning(loc: Location) {
        loc.world?.strikeLightningEffect(loc)
        loc.world?.playSound(loc, Sound.AMBIENCE_THUNDER, 1f, 1f)
        
        // Particle ring
        for (i in 0..360 step 10) {
            val rad = Math.toRadians(i.toDouble())
            val x = cos(rad) * 2
            val z = sin(rad) * 2
            loc.world?.playEffect(
                loc.clone().add(x, 0.0, z),
                Effect.MAGIC_CRIT,
                null
            )
        }
    }
    
    private fun playBlood(loc: Location) {
        loc.world?.playSound(loc, Sound.HURT_FLESH, 1f, 0.8f)
        
        // Red particles (using redstone)
        for (i in 0..50) {
            val offset = Vector(
                (Math.random() - 0.5) * 1.5,
                Math.random() * 2,
                (Math.random() - 0.5) * 1.5
            )
            loc.world?.playEffect(
                loc.clone().add(offset),
                Effect.STEP_SOUND,
                Material.REDSTONE_BLOCK
            )
        }
    }
    
    private fun playFlames(loc: Location) {
        loc.world?.playSound(loc, Sound.FIRE_IGNITE, 1f, 1f)
        
        // Fire particles
        for (i in 0..30) {
            val offset = Vector(
                (Math.random() - 0.5),
                Math.random() * 2,
                (Math.random() - 0.5)
            )
            loc.world?.playEffect(loc.clone().add(offset), Effect.FLAME, null)
            loc.world?.playEffect(loc.clone().add(offset), Effect.LAVA_POP, null)
        }
    }
    
    private fun playHearts(loc: Location) {
        loc.world?.playSound(loc, Sound.LEVEL_UP, 1f, 2f)
        
        // Hearts floating up
        var taskId: Int? = null
        var ticks = 0
        
        taskId = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            if (ticks >= 20) {
                taskId?.let { plugin.server.scheduler.cancelTask(it) }
                return@scheduleSyncRepeatingTask
            }
            
            for (i in 0..5) {
                val offset = Vector(
                    (Math.random() - 0.5),
                    ticks * 0.1,
                    (Math.random() - 0.5)
                )
                loc.world?.playEffect(loc.clone().add(offset), Effect.HEART, null)
            }
            
            ticks++
        }, 0L, 1L)
    }
    
    private fun playNotes(loc: Location) {
        // Musical notes
        var note = 0
        var taskId: Int? = null
        
        taskId = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            if (note >= 24) {
                taskId?.let { plugin.server.scheduler.cancelTask(it) }
                return@scheduleSyncRepeatingTask
            }
            
            val pitch = 0.5f + (note * 0.05f)
            loc.world?.playSound(loc, Sound.NOTE_PIANO, 1f, pitch)
            
            val offset = Vector(
                cos(Math.toRadians(note * 15.0)) * 2,
                note * 0.1,
                sin(Math.toRadians(note * 15.0)) * 2
            )
            loc.world?.playEffect(loc.clone().add(offset), Effect.NOTE, null)
            
            note++
        }, 0L, 2L)
    }
    
    private fun playSnow(loc: Location) {
        loc.world?.playSound(loc, Sound.DIG_SNOW, 1f, 1f)
        
        // Snow particles falling
        for (i in 0..40) {
            val offset = Vector(
                (Math.random() - 0.5) * 3,
                Math.random() * 3,
                (Math.random() - 0.5) * 3
            )
            loc.world?.playEffect(loc.clone().add(offset), Effect.SNOWBALL_BREAK, null)
        }
    }
    
    private fun playSpiral(loc: Location) {
        loc.world?.playSound(loc, Sound.PORTAL_TRAVEL, 1f, 2f)
        
        // Spiral effect
        var angle = 0.0
        var height = 0.0
        var taskId: Int? = null
        
        taskId = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            if (height >= 3.0) {
                taskId?.let { plugin.server.scheduler.cancelTask(it) }
                return@scheduleSyncRepeatingTask
            }
            
            val x = cos(Math.toRadians(angle)) * 1.5
            val z = sin(Math.toRadians(angle)) * 1.5
            
            loc.world?.playEffect(
                loc.clone().add(x, height, z),
                Effect.ENDER_SIGNAL,
                null
            )
            
            angle += 30
            height += 0.1
        }, 0L, 1L)
    }
    
    private fun playFirework(loc: Location) {
        loc.world?.playSound(loc, Sound.FIREWORK_BLAST, 1f, 1f)
        loc.world?.playSound(loc, Sound.FIREWORK_TWINKLE, 1f, 1f)
        
        // Firework burst
        for (i in 0..360 step 20) {
            val rad = Math.toRadians(i.toDouble())
            
            for (r in 1..3) {
                val x = cos(rad) * r
                val z = sin(rad) * r
                
                loc.world?.playEffect(
                    loc.clone().add(x, r * 0.5, z),
                    Effect.FIREWORKS_SPARK,
                    null
                )
            }
        }
    }
    
    private fun playVoid(loc: Location) {
        loc.world?.playSound(loc, Sound.ENDERMAN_TELEPORT, 1f, 0.5f)
        
        // Void particles (portal + smoke)
        for (i in 0..60) {
            val offset = Vector(
                (Math.random() - 0.5) * 2,
                Math.random() * 3,
                (Math.random() - 0.5) * 2
            )
            loc.world?.playEffect(loc.clone().add(offset), Effect.PORTAL, null)
            loc.world?.playEffect(loc.clone().add(offset), Effect.ENDER_SIGNAL, null)
        }
    }
    
    private fun playCustom(loc: Location, player: Player, killer: Player?) {
        // Combination effect
        playExplosion(loc)
        
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            playSpiral(loc)
        }, 10L)
        
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            playFirework(loc)
        }, 20L)
    }
}