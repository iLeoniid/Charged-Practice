package com.charged.command

import com.charged.util.PluginAccess
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ArenaCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("charged.admin")) {
            sender.sendMessage("§cNo tienes permiso.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("§cUso: /arena <create|delete|list|tp>")
            return true
        }

        when (args[0].lowercase()) {
            "create" -> {
                if (sender !is Player) {
                    sender.sendMessage("§cSolo jugadores pueden crear arenas.")
                    return true
                }

                if (args.size < 3) {
                    sender.sendMessage("§cUso: /arena create <nombre> <modo>")
                    return true
                }

                val name = args[1]
                val mode = args[2]

                // Create arena at player location
                val arena = com.charged.arena.model.Arena(
                    name = name,
                    spawn1 = sender.location.clone(),
                    spawn2 = sender.location.clone().add(10.0, 0.0, 0.0),
                    mode = mode
                )

                PluginAccess.plugin().arenaManager.createArena(arena)
                sender.sendMessage("§aArena §f$name §acreada para modo §f$mode")
            }

            "list" -> {
                val arenas = PluginAccess.plugin().arenaManager.getAllArenas()
                sender.sendMessage("§6§l━━━ Arenas (${arenas.size}) ━━━")
                arenas.forEach { arena ->
                    sender.sendMessage("§7${arena.name} §8- §f${arena.mode} §8- §a${arena.state}")
                }
            }

            "delete" -> {
                if (args.size < 2) {
                    sender.sendMessage("§cUso: /arena delete <nombre>")
                    return true
                }

                val name = args[1]
                PluginAccess.plugin().arenaManager.deleteArena(name)
                sender.sendMessage("§cArena §f$name §celiminada")
            }

            else -> {
                sender.sendMessage("§cSubcomando inválido.")
            }
        }

        return true
    }
}