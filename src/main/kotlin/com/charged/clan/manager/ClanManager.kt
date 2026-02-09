package com.charged.clan.manager

import com.charged.Charged
import com.charged.database.Database
import com.charged.clan.model.Clan
import com.charged.clan.model.ClanMember
import com.charged.clan.model.ClanRole
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ClanManager(
    private val plugin: Charged,
    private val database: Database
) {
    
    private val clans = ConcurrentHashMap<String, Clan>() // tag -> clan
    private val playerClans = ConcurrentHashMap<UUID, String>() // uuid -> clan tag
    private var nextClanId = 1
    
    fun createClan(ownerUuid: UUID, tag: String, name: String): Clan? {
        // Validate tag
        if (tag.length < 2 || tag.length > 8) {
            return null
        }
        
        if (clans.containsKey(tag.uppercase())) {
            return null
        }
        
        // Check if player already in clan
        if (playerClans.containsKey(ownerUuid)) {
            return null
        }
        
        val ownerName = plugin.server.getPlayer(ownerUuid)?.name ?: "Unknown"
        val clan = Clan(
            id = nextClanId++,
            tag = tag.uppercase(),
            name = name,
            ownerUuid = ownerUuid
        )
        
        // Add owner as member
        clan.addMember(ClanMember(ownerUuid, ownerName, ClanRole.OWNER))
        
        clans[clan.tag] = clan
        playerClans[ownerUuid] = clan.tag
        
        saveClan(clan)
        
        return clan
    }
    
    fun deleteClan(tag: String) {
        val clan = clans.remove(tag) ?: return
        clan.members.forEach { playerClans.remove(it.uuid) }
        deleteClanFromDatabase(clan.id)
    }
    
    fun getClan(tag: String): Clan? = clans[tag.uppercase()]
    
    fun getPlayerClan(uuid: UUID): Clan? {
        val tag = playerClans[uuid] ?: return null
        return clans[tag]
    }
    
    fun invitePlayer(clan: Clan, playerUuid: UUID): Boolean {
        if (playerClans.containsKey(playerUuid)) {
            return false // Already in a clan
        }
        
        val playerName = plugin.server.getPlayer(playerUuid)?.name ?: return false
        val member = ClanMember(playerUuid, playerName, ClanRole.MEMBER)
        
        clan.addMember(member)
        playerClans[playerUuid] = clan.tag
        
        saveClan(clan)
        
        return true
    }
    
    fun kickPlayer(clan: Clan, playerUuid: UUID): Boolean {
        if (clan.ownerUuid == playerUuid) {
            return false // Can't kick owner
        }
        
        clan.removeMember(playerUuid)
        playerClans.remove(playerUuid)
        
        saveClan(clan)
        
        return true
    }
    
    fun leavePlayer(playerUuid: UUID): Boolean {
        val clan = getPlayerClan(playerUuid) ?: return false
        
        if (clan.ownerUuid == playerUuid) {
            return false // Owner can't leave, must disband
        }
        
        clan.removeMember(playerUuid)
        playerClans.remove(playerUuid)
        
        saveClan(clan)
        
        return true
    }
    
    fun promotePlayer(clan: Clan, playerUuid: UUID): Boolean {
        val member = clan.getMember(playerUuid) ?: return false
        
        if (member.role == ClanRole.MEMBER) {
            member.role = ClanRole.ADMIN
            saveClan(clan)
            return true
        }
        
        return false
    }
    
    fun demotePlayer(clan: Clan, playerUuid: UUID): Boolean {
        val member = clan.getMember(playerUuid) ?: return false
        
        if (member.role == ClanRole.ADMIN) {
            member.role = ClanRole.MEMBER
            saveClan(clan)
            return true
        }
        
        return false
    }
    
    private fun saveClan(clan: Clan) {
        database.getConnection().use { conn ->
            conn.prepareStatement("""
                INSERT INTO charged_clans (id, tag, name, owner_uuid, level, wins, losses, bank_balance)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    level = VALUES(level),
                    wins = VALUES(wins),
                    losses = VALUES(losses),
                    bank_balance = VALUES(bank_balance)
            """).use { stmt ->
                stmt.setInt(1, clan.id)
                stmt.setString(2, clan.tag)
                stmt.setString(3, clan.name)
                stmt.setString(4, clan.ownerUuid.toString())
                stmt.setInt(5, clan.level)
                stmt.setInt(6, clan.wins)
                stmt.setInt(7, clan.losses)
                stmt.setInt(8, clan.bankBalance)
                stmt.executeUpdate()
            }
        }
    }
    
    private fun deleteClanFromDatabase(clanId: Int) {
        database.getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM charged_clans WHERE id = ?").use { stmt ->
                stmt.setInt(1, clanId)
                stmt.executeUpdate()
            }
        }
    }
    
    fun saveAll() {
        clans.values.forEach { saveClan(it) }
    }
}