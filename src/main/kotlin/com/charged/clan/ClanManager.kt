package com.charged.clan

import com.charged.Charged
import com.charged.database.Database
import com.charged.clan.model.Clan
import com.charged.clan.model.ClanMember
import com.charged.clan.model.ClanRole
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ClanManager(
    private val plugin: Charged,
    private val database: Database
) {
    
    private val clans = ConcurrentHashMap<Int, Clan>()
    private val playerToClan = ConcurrentHashMap<UUID, Int>()
    
    fun createClan(owner: Player, tag: String, name: String): Clan? {
        // Validate
        if (tag.length < 2 || tag.length > 8) {
            owner.sendMessage("§cEl tag debe tener entre 2 y 8 caracteres.")
            return null
        }
        
        if (isInClan(owner.uniqueId)) {
            owner.sendMessage("§cYa estás en un clan.")
            return null
        }
        
        if (isTagTaken(tag)) {
            owner.sendMessage("§cEse tag ya está en uso.")
            return null
        }
        
        // Create clan
        val clan = Clan(
            id = clans.size + 1,
            tag = tag,
            name = name,
            ownerUuid = owner.uniqueId,
            createdAt = System.currentTimeMillis()
        )
        
        clan.members.add(ClanMember(
            uuid = owner.uniqueId,
            name = owner.name,
            role = ClanRole.OWNER,
            joinedAt = System.currentTimeMillis()
        ))
        
        clans[clan.id] = clan
        playerToClan[owner.uniqueId] = clan.id
        
        saveClan(clan)
        
        owner.sendMessage("§aClan §f[${clan.tag}] ${clan.name} §acreado!")
        return clan
    }
    
    fun disbandClan(player: Player): Boolean {
        val clan = getPlayerClan(player.uniqueId) ?: return false
        
        if (clan.ownerUuid != player.uniqueId) {
            player.sendMessage("§cSolo el líder puede disolver el clan.")
            return false
        }
        
        // Remove all members
        clan.members.forEach { playerToClan.remove(it.uuid) }
        clans.remove(clan.id)
        
        deleteClan(clan.id)
        
        player.sendMessage("§cClan disuelto.")
        return true
    }
    
    fun inviteToClan(player: Player, targetName: String): Boolean {
        val clan = getPlayerClan(player.uniqueId) ?: return false
        val member = clan.members.find { it.uuid == player.uniqueId } ?: return false
        
        if (member.role == ClanRole.MEMBER) {
            player.sendMessage("§cNo tienes permiso para invitar.")
            return false
        }
        
        val target = plugin.server.getPlayer(targetName)
        if (target == null) {
            player.sendMessage("§cJugador no encontrado.")
            return false
        }
        
        if (isInClan(target.uniqueId)) {
            player.sendMessage("§c${target.name} ya está en un clan.")
            return false
        }
        
        target.sendMessage("§aHas sido invitado a §f[${clan.tag}] ${clan.name}")
        target.sendMessage("§7Usa §f/clan accept §7para aceptar")
        
        return true
    }
    
    fun joinClan(player: Player, clanId: Int): Boolean {
        val clan = clans[clanId] ?: return false
        
        if (isInClan(player.uniqueId)) {
            player.sendMessage("§cYa estás en un clan.")
            return false
        }
        
        clan.members.add(ClanMember(
            uuid = player.uniqueId,
            name = player.name,
            role = ClanRole.MEMBER,
            joinedAt = System.currentTimeMillis()
        ))
        
        playerToClan[player.uniqueId] = clan.id
        saveClan(clan)
        
        player.sendMessage("§aTE UNISTE A §f[${clan.tag}] ${clan.name}")
        return true
    }
    
    fun leaveClan(player: Player): Boolean {
        val clan = getPlayerClan(player.uniqueId) ?: return false
        
        if (clan.ownerUuid == player.uniqueId) {
            player.sendMessage("§cNo puedes abandonar tu propio clan. Usa §f/clan disband")
            return false
        }
        
        clan.members.removeIf { it.uuid == player.uniqueId }
        playerToClan.remove(player.uniqueId)
        
        saveClan(clan)
        
        player.sendMessage("§cSALISTE DE §f[${clan.tag}]")
        return true
    }
    
    fun getPlayerClan(uuid: UUID): Clan? {
        val clanId = playerToClan[uuid] ?: return null
        return clans[clanId]
    }
    
    fun isInClan(uuid: UUID): Boolean = playerToClan.containsKey(uuid)
    
    private fun isTagTaken(tag: String): Boolean {
        return clans.values.any { it.tag.equals(tag, ignoreCase = true) }
    }
    
    fun saveAll() {
        clans.values.forEach { saveClan(it) }
    }
    
    private fun saveClan(clan: Clan) {
        database.getConnection().use { conn ->
            conn.prepareStatement("""
                INSERT INTO charged_clans (id, tag, name, owner, created_at, level, wins, losses)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    level = VALUES(level),
                    wins = VALUES(wins),
                    losses = VALUES(losses)
            """).use { stmt ->
                stmt.setInt(1, clan.id)
                stmt.setString(2, clan.tag)
                stmt.setString(3, clan.name)
                stmt.setString(4, clan.ownerUuid.toString())
                stmt.setLong(5, clan.createdAt)
                stmt.setInt(6, clan.level)
                stmt.setInt(7, clan.wins)
                stmt.setInt(8, clan.losses)
                stmt.executeUpdate()
            }
        }
    }
    
    private fun deleteClan(id: Int) {
        database.getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM charged_clans WHERE id = ?").use { stmt ->
                stmt.setInt(1, id)
                stmt.executeUpdate()
            }
        }
    }
}