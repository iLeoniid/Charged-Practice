package com.charged.config

import com.charged.Charged
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager(val plugin: Charged) {
    
    private lateinit var config: YamlConfiguration
    private var spawn: Location? = null
    
    fun loadAll() {
        val file = File(plugin.dataFolder, "config.yml")
        if (!file.exists()) {
            plugin.saveResource("config.yml", false)
        }
        config = YamlConfiguration.loadConfiguration(file)
        
        // Load spawn
        loadSpawn()
    }
    
    private fun loadSpawn() {
        val spawnSection = config.getConfigurationSection("spawn-location")
        if (spawnSection != null) {
            val world = plugin.server.getWorld(spawnSection.getString("world") ?: "world")
            if (world != null) {
                spawn = Location(
                    world,
                    spawnSection.getDouble("x"),
                    spawnSection.getDouble("y"),
                    spawnSection.getDouble("z"),
                    spawnSection.getDouble("yaw").toFloat(),
                    spawnSection.getDouble("pitch").toFloat()
                )
            }
        }
    }
    
    fun setSpawn(location: Location) {
        spawn = location
        config.set("spawn-location.world", location.world?.name)
        config.set("spawn-location.x", location.x)
        config.set("spawn-location.y", location.y)
        config.set("spawn-location.z", location.z)
        config.set("spawn-location.yaw", location.yaw.toDouble())
        config.set("spawn-location.pitch", location.pitch.toDouble())
        saveConfig()
    }
    
    fun getSpawn(): Location? = spawn
    
    fun getString(path: String, def: String = "") = config.getString(path, def) ?: def
    fun getInt(path: String, def: Int = 0) = config.getInt(path, def)
    fun getBoolean(path: String, def: Boolean = false) = config.getBoolean(path, def)
    fun getDouble(path: String, def: Double = 0.0) = config.getDouble(path, def)
    
    private fun saveConfig() {
        config.save(File(plugin.dataFolder, "config.yml"))
    }
}