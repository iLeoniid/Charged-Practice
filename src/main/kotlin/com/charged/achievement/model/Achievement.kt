package com.charged.achievement.model

// Versión correcta que usa AchievementReward
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val requirement: Int,
    val reward: AchievementReward,  // ¡Importante! Es AchievementReward, no Int
    val secret: Boolean = false
)

data class AchievementReward(
    val xp: Int,
    val coins: Int
)

enum class AchievementCategory {
    KILLS, WINS, STREAKS, MODES, SOCIAL, SECRET
}