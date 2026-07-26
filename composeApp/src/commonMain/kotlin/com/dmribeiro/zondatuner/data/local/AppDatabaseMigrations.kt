package com.dmribeiro.zondatuner.data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE tunings ADD COLUMN string6OctaveShift INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE tunings ADD COLUMN string5OctaveShift INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE tunings ADD COLUMN string4OctaveShift INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE tunings ADD COLUMN string3OctaveShift INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE tunings ADD COLUMN string2OctaveShift INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE tunings ADD COLUMN string1OctaveShift INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE tunings ADD COLUMN lastUsedAt INTEGER NOT NULL DEFAULT 0")
    }
}

private fun SQLiteConnection.execSQL(sql: String) {
    prepare(sql).use { statement ->
        statement.step()
    }
}
