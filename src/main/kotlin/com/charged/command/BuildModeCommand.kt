package com.charged.command

import com.charged.Charged
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BuildModeCommand(plugin: Charged) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cThis command can only be used by players!")
            return true
        }

        // PluginAccess.plugin().buildModeManager.toggleBuildMode(sender)
        sender.sendMessage("§aBuild mode toggled (system in development)")
        return true
    }
}