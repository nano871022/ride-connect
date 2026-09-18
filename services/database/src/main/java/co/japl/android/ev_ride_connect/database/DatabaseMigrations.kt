package co.japl.android.ev_ride_connect.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE ev_configs ADD COLUMN image_url TEXT NOT NULL DEFAULT ''")
        }
    }
}
