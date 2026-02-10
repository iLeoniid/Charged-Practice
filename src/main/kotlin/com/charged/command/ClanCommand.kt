package com.charged.command

import com.charged.util.PluginAccess
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClanCommand : CommandExecutor {

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
            sendHelp(sender as Player)
            return true
        }

        val player = sender as Player
        val chargedPlugin = PluginAccess.plugin()

        when (args[0].lowercase()) {
            "create" -> {
                if (args.size < 3) {
                    player.sendMessage("§cUso: /clan create <tag> <nombre>")
                    return true
                }

                val tag = args[1]
                val name = args.drop(2).joinToString(" ")

                // chargedPlugin.clanManager.createClan(player, tag, name)
                player.sendMessage("§aCreando clan $tag ($name)...")
            }

            "disband" -> {
                // chargedPlugin.clanManager.disbandClan(player)
                player.sendMessage("§cDisolviendo clan...")
            }

            "invite" -> {
                if (args.size < 2) {
                    player.sendMessage("§cUso: /clan invite <jugador>")
                    return true
                }

                // chargedPlugin.clanManager.inviteToClan(player, args[1])
                player.sendMessage("§aInvitar a ${args[1]}...")
            }

            "leave" -> {
                // chargedPlugin.clanManager.leaveClan(player)
                player.sendMessage("§cSaliendo del clan...")
            }

            "info" -> {
                // val clan = chargedPlugin.clanManager.getPlayerClan(player.uniqueId)
                // if (clan == null) {
                player.sendMessage("§cNo estás en un clan.")
                //     return true
                // }

                // player.sendMessage("§6§l━━━ [${clan.tag}] ${clan.name} ━━━")
                player.sendMessage("§cSistema de clanes en desarrollo...")
            }

            "list" -> {
                player.sendMessage("§6§lSistema de clanes en desarrollo...")
            }

            else -> sendHelp(player)
        }

        return true
    }

    private fun sendHelp(player: Player) {
        player.sendMessage("§6§lComandos de Clan:")
        player.sendMessage("§7/clan create <tag> <nombre> §8- §fCrear clan")
        player.sendMessage("§7/clan disband §8- §fDisolver clan")
        player.sendMessage("§7/clan invite <jugador> §8- §fInvitar")
        player.sendMessage("§7/clan leave §8- §fSalir del clan")
        player.sendMessage("§7/clan info §8- §fInformación")
        player.sendMessage("§7/clan list §8- §fListar miembros")
    }
}