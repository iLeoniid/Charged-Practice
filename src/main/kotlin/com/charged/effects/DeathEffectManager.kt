package com.charged.effects

import com.charged.Charged
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DeathEffectManager(private val plugin: Charged) {
    
    private val playerEffects = ConcurrentHashMap<UUID, DeathEffect>()
    
    enum class DeathEffect {
        EXPLOSION,
        LIGHTNING,
        BLOOD,
        HEARTS,
        FLAMES,
        WATER,
        SMOKE,
        FIREWORK,
        CRITICAL,
        NONE
    }
    
    fun setEffect(player: UUID, effect: DeathEffect) {
        playerEffects[player] = effect
    }
    
    fun playDeathEffect(player: Player, killer: Player?) {
        val effect = playerEffects[player.uniqueId] ?: DeathEffect.BLOOD
        val location = player.location.clone().add(0.0, 1.0, 0.0)
        
        when (effect) {
            DeathEffect.EXPLOSION -> playExplosion(location)
            DeathEffect.LIGHTNING -> playLightning(location)
            DeathEffect.BLOOD -> playBlood(location)
            DeathEffect.HEARTS -> playHearts(location)
            DeathEffect.FLAMES -> playFlames(location)
            DeathEffect.WATER -> playWater(location)
            DeathEffect.SMOKE -> playSmoke(location)
            DeathEffect.FIREWORK -> playFirework(location)
            DeathEffect.CRITICAL -> playCritical(location)
            DeathEffect.NONE -> {}
        }
    }
    
    private fun playExplosion(loc: Location) {
        loc.world?.createExplosion(loc, 0f, false)
        loc.world?.playSound(loc, Sound.EXPLODE, 1f, 1f)
        
        // Particle circle
        for (i in 0..360 step 10) {
            val rad = Math.toRadians(i.toDouble())
            val x = Math.cos(rad) * 2.0
            val z = Math.sin(rad) * 2.0
            loc.world?.playEffect(
                loc.clone().add(x, 0.5, z),
                Effect.EXPLOSION_LARGE,
                null
            )
        }
    }
    
    private fun playLightning(loc: Location) {
        loc.world?.strikeLightningEffect(loc)
        loc.world?.playSound(loc, Sound.AMBIENCE_THUNDER, 1f, 1f)
        
        // Upward particles
        for (i in 0..20) {
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                loc.world?.playEffect(
                    loc.clone().add(0.0, i * 0.3, 0.0),
                    Effect.MAGIC_CRIT,
                    null
                )
            }, i.toLong())
        }
    }
    
    private fun playBlood(loc: Location) {
        loc.world?.playSound(loc, Sound.HURT_FLESH, 1f, 0.8f)
        
        // Blood splatter
        repeat(50) {
            val vector = Vector(
                (Math.random() - 0.5) * 2,
                Math.random(),
                (Math.random() - 0.5) * 2
            )
            
            loc.world?.playEffect(
                loc.clone().add(vector),
                Effect.STEP_SOUND,
                Material.REDSTONE_BLOCK
            )
        }
    }
    
    private fun playHearts(loc: Location) {
        loc.world?.playSound(loc, Sound.LEVEL_UP, 1f, 2f)
        
        // Heart spiral
        for (i in 0..30) {
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                val rad = Math.toRadians(i * 24.0)
                val x = Math.cos(rad) * (i * 0.05)
                val z = Math.sin(rad) * (i * 0.05)
                val y = i * 0.1
                
                loc.world?.playEffect(
                    loc.clone().add(x, y, z),
                    Effect.HEART,
                    null
                )
            }, i.toLong())
        }
    }
    
    private fun playFlames(loc: Location) {
        loc.world?.playSound(loc, Sound.FIRE, 1f, 1f)
        
        // Flame burst
        repeat(100) {
            val vector = Vector(
                (Math.random() - 0.5) * 3,
                Math.random() * 2,
                (Math.random() - 0.5) * 3
            )
            
            loc.world?.playEffect(
                loc.clone().add(vector),
                Effect.FLAME,
                null
            )
        }
    }
    
    private fun playWater(loc: Location) {
        loc.world?.playSound(loc, Sound.SPLASH, 1f, 1f)
        
        // Water splash
        repeat(80) {
            val vector = Vector(
                (Math.random() - 0.5) * 2.5,
                Math.random() * 1.5,
                (Math.random() - 0.5) * 2.5
            )
            
            loc.world?.playEffect(
                loc.clone().add(vector),
                Effect.WATERDRIP,
                null
            )
        }
    }
    
    private fun playSmoke(loc: Location) {
        loc.world?.playSound(loc, Sound.FIZZ, 1f, 0.5f)
        
        // Smoke cloud
        for (i in 0..40) {
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                repeat(5) {
                    val vector = Vector(
                        (Math.random() - 0.5) * 2,
                        Math.random() * 2,
                        (Math.random() - 0.5) * 2
                    )
                    
                    loc.world?.playEffect(
                        loc.clone().add(vector),
                        Effect.LARGE_SMOKE,
                        null
                    )
                }
            }, i.toLong())
        }
    }
    
    private fun playFirework(loc: Location) {
        loc.world?.playSound(loc, Sound.FIREWORK_BLAST, 1f, 1f)
        loc.world?.playSound(loc, Sound.FIREWORK_LARGE_BLAST, 1f, 1.5f)
        
        // Firework burst
        for (i in 0..360 step 15) {
            val rad = Math.toRadians(i.toDouble())
            for (r in 1..10) {
                plugin.server.scheduler.runTaskLater(plugin, Runnable {
                    val distance = r * 0.3
                    val x = Math.cos(rad) * distance
                    val z = Math.sin(rad) * distance
                    
                    loc.world?.playEffect(
                        loc.clone().add(x, r * 0.2, z),
                        Effect.FIREWORKS_SPARK,
                        null
                    )
                }, r.toLong())
            }
        }
    }
    
    private fun playCritical(loc: Location) {
        loc.world?.playSound(loc, Sound.SUCCESSFUL_HIT, 1f, 0.5f)
        
        // Critical hits burst
        repeat(60) {
            val vector = Vector(
                (Math.random() - 0.5) * 2,
                Math.random() * 2,
                (Math.random() - 0.5) * 2
            )
            
            loc.world?.playEffect(
                loc.clone().add(vector),
                Effect.CRIT,
                null
            )
        }
    }
}