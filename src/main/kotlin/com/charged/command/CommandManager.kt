package com.charged.command

import com.charged.Charged

class CommandManager(private val plugin: Charged) {

    fun registerAll() {
        plugin.getCommand("charged")?.setExecutor(ChargedCommand())
        plugin.getCommand("duel")?.setExecutor(DuelCommand())
        plugin.getCommand("queue")?.setExecutor(QueueCommand())
        plugin.getCommand("leavequeue")?.setExecutor(LeaveQueueCommand())
        plugin.getCommand("stats")?.setExecutor(StatsCommand(
            plugin = TODO()
        ))
        plugin.getCommand("arena")?.setExecutor(ArenaCommand())
        plugin.getCommand("spawn")?.setExecutor(SpawnCommand())
        plugin.getCommand("clan")?.setExecutor(ClanCommand())
        plugin.getCommand("buildmode")?.setExecutor(BuildModeCommand(plugin))
        plugin.getCommand("menu")?.setExecutor(MenuCommand(plugin))  // <-- AÑADIR ESTO
        plugin.getCommand("staffmode")?.setExecutor(StaffModeCommand(
            plugin = TODO()
        ))
    }
}