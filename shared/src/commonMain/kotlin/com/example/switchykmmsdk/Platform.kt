package com.example.switchykmmsdk
import com.squareup.sqldelight.db.SqlDriver

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect class DatabaseDriverFactory {
    val houseId: String
    fun createDriver(): SqlDriver
}