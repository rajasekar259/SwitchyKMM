package com.example.switchykmmsdk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform