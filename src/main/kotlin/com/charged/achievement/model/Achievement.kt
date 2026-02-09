package com.charged.achievement.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val requirement: Int,
    val reward: AchievementReward,
    val secret: Boolean = false
)

enum class AchievementCategory {
    KILLS,
    WINS,
    STREAKS,
    MODES,
    SOCIAL,
    SECRET
}

data class AchievementReward(
    val coins: Int = 0,
    val title: String? = null
)