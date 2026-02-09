package com.charged.database

import java.sql.Connection

interface Database {
    fun connect()
    fun disconnect()
    fun getConnection(): Connection
    fun setupTables()
    fun isConnected(): Boolean
}