package com.dmribeiro.zondatuner

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform