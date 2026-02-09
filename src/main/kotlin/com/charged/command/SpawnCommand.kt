package com.charged.command

import com.charged.Charged
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpawnCommand(private val plugin: Charged) : CommandExecutor {
    
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cSolo jugadores pueden usar este comando.")
            return true
        }
        
        val spawn = plugin.plugin.configManager.getSpawn()
        if (spawn == null) {
            sender.sendMessage("§cNo hay spawn configurado.")
            return true
        }
        
        sender.teleport(spawn)
        sender.sendMessage("§aTeletransportado al spawn.")
        
        return true
    }
}