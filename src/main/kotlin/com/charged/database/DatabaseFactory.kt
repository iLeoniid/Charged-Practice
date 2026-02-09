package com.charged.database

import com.charged.config.ConfigManager
import com.charged.database.impl.MySQLDatabase
import com.charged.database.impl.SQLiteDatabase

object DatabaseFactory {
    fun create(config: ConfigManager): Database {
        val type = config.getString("database.type", "sqlite")
        return when (type.lowercase()) {
            "mysql" -> MySQLDatabase(
                config.getString("database.mysql.host", "localhost"),
                config.getInt("database.mysql.port", 3306),
                config.getString("database.mysql.database", "charged"),
                config.getString("database.mysql.username", "root"),
                config.getString("database.mysql.password", "")
            )
            else -> SQLiteDatabase(config.plugin.dataFolder)
        }
    }
}