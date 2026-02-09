package com.charged.command

import com.charged.Charged
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClanCommand(private val plugin: Charged) : CommandExecutor {
    
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
            sendHelp(sender)
            return true
        }
        
        when (args[0].lowercase()) {
            "create" -> {
                if (args.size < 3) {
                    sender.sendMessage("§cUso: /clan create <tag> <nombre>")
                    return true
                }
                
                val tag = args[1]
                val name = args.drop(2).joinToString(" ")
                
                plugin.plugin.clanManager.createClan(sender, tag, name)
            }
            
            "disband" -> {
                plugin.plugin.clanManager.disbandClan(sender)
            }
            
            "invite" -> {
                if (args.size < 2) {
                    sender.sendMessage("§cUso: /clan invite <jugador>")
                    return true
                }
                
                plugin.plugin.clanManager.inviteToClan(sender, args[1])
            }
            
            "leave" -> {
                plugin.plugin.clanManager.leaveClan(sender)
            }
            
            "info" -> {
                val clan = plugin.plugin.clanManager.getPlayerClan(sender.uniqueId)
                if (clan == null) {
                    sender.sendMessage("§cNo estás en un clan.")
                    return true
                }
                
                sender.sendMessage("§6§l━━━ [${clan.tag}] ${clan.name} ━━━")
                sender.sendMessage("§7Líder: §f${plugin.server.getOfflinePlayer(clan.ownerUuid).name}")
                sender.sendMessage("§7Miembros: §f${clan.members.size}")
                sender.sendMessage("§7Nivel: §f${clan.level}")
                sender.sendMessage("§7Victorias: §a${clan.wins}")
                sender.sendMessage("§7Derrotas: §c${clan.losses}")
                sender.sendMessage("§7WLR: §f${String.format("%.2f", clan.wlr)}")
            }
            
            "list" -> {
                val clan = plugin.plugin.clanManager.getPlayerClan(sender.uniqueId)
                if (clan == null) {
                    sender.sendMessage("§cNo estás en un clan.")
                    return true
                }
                
                sender.sendMessage("§6§lMiembros de [${clan.tag}]:")
                clan.members.forEach { member ->
                    val roleColor = when (member.role) {
                        com.charged.clan.model.ClanRole.OWNER -> "§c★"
                        com.charged.clan.model.ClanRole.ADMIN -> "§6★"
                        com.charged.clan.model.ClanRole.MEMBER -> "§7"
                    }
                    sender.sendMessage("$roleColor ${member.name}")
                }
            }
            
            else -> sendHelp(sender)
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
