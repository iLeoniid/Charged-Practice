package com.charged.replay

import com.charged.Charged
import com.charged.database.Database
import com.charged.replay.model.Replay
import com.charged.replay.model.ReplayFrame
import com.charged.match.model.Match
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ReplayManager(
    private val plugin: Charged,
    private val database: Database
) {
    
    private val activeRecordings = ConcurrentHashMap<String, ReplayRecorder>()
    private val playerReplays = ConcurrentHashMap<UUID, MutableList<String>>()
    
    companion object {
        const val MAX_REPLAYS_PER_PLAYER = 50
    }
    
    fun startRecording(match: Match) {
        if (match.participants.size != 2) return
        
        val recorder = ReplayRecorder(match, plugin)
        activeRecordings[match.id] = recorder
        recorder.start()
    }
    
    fun stopRecording(matchId: String, winnerUuid: UUID?) {
        val recorder = activeRecordings.remove(matchId) ?: return
        recorder.stop()
        
        val replay = recorder.finalize(winnerUuid)
        saveReplay(replay)
        
        // Add to player histories
        replay.player1.let { addToPlayerHistory(it, replay.id) }
        replay.player2.let { addToPlayerHistory(it, replay.id) }
    }
    
    private fun saveReplay(replay: Replay) {
        database.getConnection().use { conn ->
            conn.prepareStatement("""
                INSERT INTO charged_replays 
                (id, player1, player2, mode, arena, started_at, ended_at, winner)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """).use { stmt ->
                stmt.setString(1, replay.id)
                stmt.setString(2, replay.player1.toString())
                stmt.setString(3, replay.player2.toString())
                stmt.setString(4, replay.mode)
                stmt.setString(5, replay.arena)
                stmt.setLong(6, replay.startedAt)
                stmt.setLong(7, replay.endedAt)
                stmt.setString(8, replay.winnerUuid?.toString())
                stmt.executeUpdate()
            }
        }
    }
    
    private fun addToPlayerHistory(uuid: UUID, replayId: String) {
        val history = playerReplays.getOrPut(uuid) { mutableListOf() }
        history.add(0, replayId)
        
        while (history.size > MAX_REPLAYS_PER_PLAYER) {
            val removedId = history.removeAt(history.size - 1)
            deleteReplay(removedId)
        }
    }
    
    fun getPlayerReplays(uuid: UUID): List<Replay> {
        val replayIds = playerReplays[uuid] ?: emptyList()
        return replayIds.mapNotNull { loadReplay(it) }
    }
    
    private fun loadReplay(id: String): Replay? {
        database.getConnection().use { conn ->
            conn.prepareStatement("SELECT * FROM charged_replays WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                
                if (rs.next()) {
                    return Replay(
                        id = rs.getString("id"),
                        player1 = UUID.fromString(rs.getString("player1")),
                        player2 = UUID.fromString(rs.getString("player2")),
                        player1Name = plugin.server.getOfflinePlayer(UUID.fromString(rs.getString("player1"))).name ?: "Unknown",
                        player2Name = plugin.server.getOfflinePlayer(UUID.fromString(rs.getString("player2"))).name ?: "Unknown",
                        mode = rs.getString("mode"),
                        arena = rs.getString("arena"),
                        startedAt = rs.getLong("started_at"),
                        endedAt = rs.getLong("ended_at"),
                        winnerUuid = rs.getString("winner")?.let { UUID.fromString(it) }
                    )
                }
            }
        }
        return null
    }
    
    private fun deleteReplay(id: String) {
        database.getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM charged_replays WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                stmt.executeUpdate()
            }
        }
    }
}

class ReplayRecorder(
    private val match: Match,
    private val plugin: Charged
) {
    private val frames = mutableListOf<ReplayFrame>()
    private var currentTick = 0L
    private var taskId: Int? = null
    
    fun start() {
        taskId = plugin.server.scheduler.runTaskRepeatAsynchronously(plugin, Runnable {
            captureFrame()
        }, 0L, 1L).taskId
    }
    
    fun stop() {
        taskId?.let { plugin.server.scheduler.cancelTask(it) }
    }
    
    private fun captureFrame() {
        val p1 = plugin.server.getPlayer(match.participants[0]) ?: return
        val p2 = plugin.server.getPlayer(match.participants[1]) ?: return
        
        frames.add(ReplayFrame(
            tick = currentTick++,
            player1Location = p1.location.clone(),
            player2Location = p2.location.clone(),
            player1Health = p1.health,
            player2Health = p2.health
        ))
    }
    
    fun finalize(winnerUuid: UUID?): Replay {
        return Replay(
            id = match.id,
            player1 = match.participants[0],
            player2 = match.participants[1],
            player1Name = plugin.server.getPlayer(match.participants[0])?.name ?: "Unknown",
            player2Name = plugin.server.getPlayer(match.participants[1])?.name ?: "Unknown",
            mode = match.mode,
            arena = "arena1", // TODO: Get from match
            startedAt = match.startedAt,
            endedAt = match.endedAt,
            winnerUuid = winnerUuid,
            frames = frames
        )
    }
}