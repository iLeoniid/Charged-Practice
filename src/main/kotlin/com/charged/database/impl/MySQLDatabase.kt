package com.charged.database.impl

import com.charged.database.Database
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class MySQLDatabase(
    host: String,
    port: Int,
    database: String,
    username: String,
    password: String
) : Database {
    
    private lateinit var hikari: HikariDataSource
    
    init {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://$host:$port/$database?useSSL=false"
            this.username = username
            this.password = password
            driverClassName = "com.mysql.jdbc.Driver"
            maximumPoolSize = 10
            minimumIdle = 2
        }
        hikari = HikariDataSource(config)
    }
    
    override fun connect() {}
    
    override fun disconnect() {
        if (::hikari.isInitialized) hikari.close()
    }
    
    override fun getConnection(): Connection = hikari.connection
    
    override fun setupTables() {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                // Players table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS charged_players (
                        uuid VARCHAR(36) PRIMARY KEY,
                        name VARCHAR(16) NOT NULL,
                        elo INT DEFAULT 1000,
                        wins INT DEFAULT 0,
                        losses INT DEFAULT 0,
                        INDEX idx_name (name)
                    ) ENGINE=InnoDB
                """)
                
                // Clans table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS charged_clans (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        tag VARCHAR(8) UNIQUE NOT NULL,
                        name VARCHAR(32) NOT NULL,
                        owner VARCHAR(36) NOT NULL,
                        created_at BIGINT NOT NULL,
                        level INT DEFAULT 1,
                        wins INT DEFAULT 0,
                        losses INT DEFAULT 0,
                        INDEX idx_tag (tag)
                    ) ENGINE=InnoDB
                """)
                
                // Replays table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS charged_replays (
                        id VARCHAR(36) PRIMARY KEY,
                        player1 VARCHAR(36) NOT NULL,
                        player2 VARCHAR(36) NOT NULL,
                        mode VARCHAR(32) NOT NULL,
                        arena VARCHAR(64) NOT NULL,
                        started_at BIGINT NOT NULL,
                        ended_at BIGINT NOT NULL,
                        winner VARCHAR(36),
                        INDEX idx_player1 (player1),
                        INDEX idx_player2 (player2)
                    ) ENGINE=InnoDB
                """)
                
                // Achievements table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS charged_achievements (
                        player_uuid VARCHAR(36) NOT NULL,
                        achievement_id VARCHAR(64) NOT NULL,
                        unlocked_at BIGINT NOT NULL,
                        PRIMARY KEY (player_uuid, achievement_id)
                    ) ENGINE=InnoDB
                """)
            }
        }
    }
    
    override fun isConnected() = ::hikari.isInitialized && !hikari.isClosed
}