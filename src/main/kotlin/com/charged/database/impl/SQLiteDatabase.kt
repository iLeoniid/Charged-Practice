package com.charged.database.impl

import com.charged.database.Database
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

class SQLiteDatabase(dataFolder: File) : Database {
    
    private val file = File(dataFolder, "charged.db")
    private lateinit var connection: Connection
    
    override fun connect() {
        file.parentFile.mkdirs()
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:${file.path}")
    }
    
    override fun disconnect() {
        if (::connection.isInitialized) connection.close()
    }
    
    override fun getConnection() = connection
    
    override fun setupTables() {
        connection.createStatement().use { stmt ->
            // Players table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS charged_players (
                    uuid TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    elo INTEGER DEFAULT 1000,
                    wins INTEGER DEFAULT 0,
                    losses INTEGER DEFAULT 0
                )
            """)
            
            // Clans table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS charged_clans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tag TEXT UNIQUE NOT NULL,
                    name TEXT NOT NULL,
                    owner TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    level INTEGER DEFAULT 1,
                    wins INTEGER DEFAULT 0,
                    losses INTEGER DEFAULT 0
                )
            """)
            
            // Replays table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS charged_replays (
                    id TEXT PRIMARY KEY,
                    player1 TEXT NOT NULL,
                    player2 TEXT NOT NULL,
                    mode TEXT NOT NULL,
                    arena TEXT NOT NULL,
                    started_at INTEGER NOT NULL,
                    ended_at INTEGER NOT NULL,
                    winner TEXT
                )
            """)
            
            // Achievements table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS charged_achievements (
                    player_uuid TEXT NOT NULL,
                    achievement_id TEXT NOT NULL,
                    unlocked_at INTEGER NOT NULL,
                    PRIMARY KEY (player_uuid, achievement_id)
                )
            """)
        }
    }
    
    override fun isConnected() = ::connection.isInitialized && !connection.isClosed
}