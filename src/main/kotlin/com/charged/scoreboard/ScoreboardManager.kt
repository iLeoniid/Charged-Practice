package com.charged.scoreboard

import com.charged.Charged
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ScoreboardManager(private val plugin: Charged) {
    
    private val playerBoards = ConcurrentHashMap<UUID, Scoreboard>()
    
    fun createLobbyBoard(player: Player) {
        val board = Bukkit.getScoreboardManager().newScoreboard
        val objective = board.registerNewObjective("lobby", "dummy")
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName = "§6§lCHARGED"
        
        setLobbyScores(player, objective)
        
        player.scoreboard = board
        playerBoards[player.uniqueId] = board
    }
    
    fun createMatchBoard(player: Player, opponent: String) {
        val board = Bukkit.getScoreboardManager().newScoreboard
        val objective = board.registerNewObjective("match", "dummy")
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName = "§6§lIN MATCH"
        
        setMatchScores(player, opponent, objective)
        
        player.scoreboard = board
        playerBoards[player.uniqueId] = board
    }
    
    private fun setLobbyScores(player: Player, objective: Objective) {
        val stats = plugin.plugin.playerManager.getStats(player.uniqueId)
        
        var line = 10
        objective.getScore("§7").score = line--
        objective.getScore("§7Your Stats:").score = line--
        objective.getScore("§7ELO: §f${stats?.elo ?: 1000}").score = line--
        objective.getScore("§7Wins: §a${stats?.wins ?: 0}").score = line--
        objective.getScore("§7Losses: §c${stats?.losses ?: 0}").score = line--
        objective.getScore("§8").score = line--
        objective.getScore("§7Online: §f${Bukkit.getOnlinePlayers().size}").score = line--
        objective.getScore("§9").score = line--
        objective.getScore("§ewww.yourserver.com").score = line
    }
    
    private fun setMatchScores(player: Player, opponent: String, objective: Objective) {
        var line = 10
        objective.getScore("§7").score = line--
        objective.getScore("§7Opponent:").score = line--
        objective.getScore("§f$opponent").score = line--
        objective.getScore("§8").score = line--
        objective.getScore("§7Your Health:").score = line--
        objective.getScore("§c${player.health.toInt()}❤").score = line--
        objective.getScore("§9").score = line--
        objective.getScore("§7Duration: §f0:00").score = line
    }
    
    fun updateMatchBoard(player: Player, duration: Int) {
        val board = playerBoards[player.uniqueId] ?: return
        val objective = board.getObjective(DisplaySlot.SIDEBAR) ?: return
        
        // Update health
        objective.getScore("§c${player.health.toInt()}❤").score = 5
        
        // Update duration
        val minutes = duration / 60
        val seconds = duration % 60
        objective.getScore("§7Duration: §f$minutes:${seconds.toString().padStart(2, '0')}").score = 3
    }
    
    fun removeboard(player: Player) {
        playerBoards.remove(player.uniqueId)
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    }
}