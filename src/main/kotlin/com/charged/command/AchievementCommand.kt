package com.charged.command

import com.charged.util.PluginAccess
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AchievementsCommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cEste comando solo puede ser usado por jugadores.")
            return true
        }

        val targetName = if (args.isNotEmpty()) args[0] else sender.name
        val target = PluginAccess.get().server.getPlayer(targetName)

        if (target == null && args.isNotEmpty()) {
            sender.sendMessage("§cJugador no encontrado.")
            return true
        }

        val uuid = target?.uniqueId ?: sender.uniqueId
        val achievementManager = PluginAccess.plugin().achievementManager

        if (args.getOrNull(1) == "stats") {
            showAchievementStats(sender, uuid, achievementManager)
        } else {
            showAchievements(sender, uuid, achievementManager, args.getOrNull(0))
        }

        return true
    }

    private fun showAchievements(
        player: Player,
        targetUuid: java.util.UUID,
        achievementManager: com.charged.achievement.manager.AchievementManager,
        categoryName: String?
    ) {
        val targetName = PluginAccess.get().server.getPlayer(targetUuid)?.name ?: "Unknown"

        val categories = com.charged.achievement.model.AchievementCategory.values()
        val selectedCategory = if (categoryName != null) {
            categories.firstOrNull { it.name.equals(categoryName, ignoreCase = true) }
        } else {
            com.charged.achievement.model.AchievementCategory.WINS
        }

        player.sendMessage("")
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        player.sendMessage("§e§l  ACHIEVEMENTS: §f${targetName}")
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // Mostrar categorías
        player.sendMessage("§7Categorías:")
        categories.forEach { category ->
            val count = achievementManager.getAchievementsByCategory(category).size
            val completed = achievementManager.getPlayerCompletedAchievements(targetUuid)
                .count { it.category == category }

            val status = if (category == selectedCategory) "§6» " else "§7"
            player.sendMessage("$status${category.name} §8- §f$completed§7/$count")
        }
        player.sendMessage("")

        // Mostrar achievements de la categoría seleccionada
        if (selectedCategory != null) {
            player.sendMessage("§e${selectedCategory.name}:")

            achievementManager.getAchievementsByCategory(selectedCategory).forEach { achievement ->
                val unlocked = achievementManager.hasAchievement(targetUuid, achievement.id)
                val status = if (unlocked) "§a✓" else "§7○"
                val name = if (achievement.secret && !unlocked) "§8???" else achievement.name

                player.sendMessage("  $status §f$name")
                if (unlocked) {
                    player.sendMessage("    §7${achievement.description}")
                    player.sendMessage("    §6${achievement.reward.xp} XP §8| §6${achievement.reward.coins} Coins")
                } else {
                    player.sendMessage("    §8${achievement.description}")
                }
            }
        }

        // Mostrar progreso total
        val progress = achievementManager.getAchievementProgress(targetUuid)
        player.sendMessage("")
        player.sendMessage("§7Progreso total: §f${progress.completed}§7/${progress.total} §8(§7${progress.progress}%§8)")
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun showAchievementStats(
        player: Player,
        targetUuid: java.util.UUID,
        achievementManager: com.charged.achievement.manager.AchievementManager
    ) {
        val targetName = PluginAccess.get().server.getPlayer(targetUuid)?.name ?: "Unknown"
        val progress = achievementManager.getAchievementProgress(targetUuid)
        val points = achievementManager.getPlayerAchievementPoints(targetUuid)

        player.sendMessage("")
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        player.sendMessage("§e§l  ACHIEVEMENT STATS: §f${targetName}")
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        player.sendMessage("§7Progreso total: §f${progress.completed}§7/${progress.total} §8(§7${progress.progress}%§8)")
        player.sendMessage("§7Puntos de logro: §e$points")

        // Por categoría
        com.charged.achievement.model.AchievementCategory.values().forEach { category ->
            val total = achievementManager.getAchievementsByCategory(category).size
            val completed = achievementManager.getPlayerCompletedAchievements(targetUuid)
                .count { it.category == category }

            if (total > 0) {
                val catProgress = (completed.toDouble() / total * 100).toInt()
                val bar = buildProgressBar(completed, total)
                player.sendMessage("§7${category.name}: $bar §f$completed§7/$total §8($catProgress%)")
            }
        }

        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun buildProgressBar(current: Int, max: Int): String {
        val percentage = if (max > 0) (current.toDouble() / max * 100).toInt() else 0
        val filled = (percentage / 10)
        val empty = 10 - filled

        return "§a${"█".repeat(filled)}§7${"█".repeat(empty)}"
    }
}