package com.charged.command

import com.charged.Charged

class CommandManager(private val plugin: Charged) {
    
    fun registerAll() {
        plugin.getCommand("charged")?.setExecutor(ChargedCommand(plugin))
        plugin.getCommand("duel")?.setExecutor(DuelCommand(plugin))
        plugin.getCommand("queue")?.setExecutor(QueueCommand(plugin))
        plugin.getCommand("leavequeue")?.setExecutor(LeaveQueueCommand(plugin))
        plugin.getCommand("stats")?.setExecutor(StatsCommand(plugin))
        plugin.getCommand("arena")?.setExecutor(ArenaCommand(plugin))
        plugin.getCommand("spawn")?.setExecutor(SpawnCommand(plugin))
        plugin.getCommand("clan")?.setExecutor(ClanCommand(plugin))
    }
}