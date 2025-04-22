package com.dmribeiro.zondatuner.data.local

import androidx.room.RoomDatabaseConstructor

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}