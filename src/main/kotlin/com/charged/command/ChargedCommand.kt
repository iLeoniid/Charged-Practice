package com.charged.command

import com.charged.util.PluginAccess
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ChargedCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val plugin = PluginAccess.get()

        if (args.isEmpty()) {
            sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
            sender.sendMessage("§e  CHARGED PvP")
            sender.sendMessage("§7  Version: §f${plugin.description.version}")
            sender.sendMessage("§7  Author: §fYourName")
            sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━")
            return true
        }

        when (args[0].lowercase()) {
            "reload" -> {
                if (!sender.hasPermission("charged.admin")) {
                    sender.sendMessage("§cNo tienes permiso.")
                    return true
                }

                PluginAccess.plugin().configManager.loadAll()
                sender.sendMessage("§aConfiguración recargada.")
            }

            "help" -> {
                sender.sendMessage("§6§lComandos de Charged:")
                sender.sendMessage("§7/queue <modo> §8- §fUnirse a cola")
                sender.sendMessage("§7/duel <jugador> §8- §fRetar a duelo")
                sender.sendMessage("§7/stats [jugador] §8- §fVer estadísticas")
                sender.sendMessage("§7/spawn §8- §fIr al spawn")
            }

            else -> {
                sender.sendMessage("§cSubcomando inválido. Usa §f/charged help")
            }
        }

        return true
    }
}