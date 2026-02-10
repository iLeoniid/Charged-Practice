package com.charged.command

import com.charged.util.PluginAccess
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DuelCommand : CommandExecutor {

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

        if (args.isEmpty()) {
            sender.sendMessage("§cUso: /duel <jugador>")
            return true
        }

        val plugin = PluginAccess.get()
        val target = plugin.server.getPlayer(args[0])

        if (target == null) {
            sender.sendMessage("§cJugador no encontrado")
            return true
        }

        if (target.uniqueId == sender.uniqueId) {
            sender.sendMessage("§cNo puedes retarte a ti mismo.")
            return true
        }

        sender.sendMessage("§aSolicitud de duelo enviada a §f${target.name}")
        target.sendMessage("§f${sender.name} §7te ha retado a un duelo.")

        return true
    }
}