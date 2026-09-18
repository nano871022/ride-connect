package co.japl.android.ev_ride_connect.database

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DatabaseMigrationsTest {

    @Test
    fun shouldHaveCorrectStartAndEndVersionsForMigration6To7() {
        val migration = DatabaseMigrations.MIGRATION_6_7

        assertThat(migration.startVersion).isEqualTo(6)
        assertThat(migration.endVersion).isEqualTo(7)
    }

    @Test
    fun shouldExecuteAlterTableForImageUrlColumnWhenMigrating6To7() {
        val migration = DatabaseMigrations.MIGRATION_6_7
        val fakeDb = FakeSupportSQLiteDatabase()

        migration.migrate(fakeDb)

        assertThat(fakeDb.executedSql)
            .contains("ALTER TABLE ev_configs ADD COLUMN image_url TEXT NOT NULL DEFAULT ''")
    }

    private class FakeSupportSQLiteDatabase : SupportSQLiteDatabase {
        val executedSql = mutableListOf<String>()

        override fun execSQL(sql: String) {
            executedSql.add(sql)
        }

        override fun execSQL(sql: String, bindArgs: Array<out Any?>) {
            executedSql.add(sql)
        }

        override fun close() {}
        override fun query(query: String): Cursor = throw UnsupportedOperationException()
        override fun query(query: String, bindArgs: Array<out Any?>): Cursor = throw UnsupportedOperationException()
        override fun query(query: SupportSQLiteQuery): Cursor = throw UnsupportedOperationException()
        override fun query(query: SupportSQLiteQuery, cancellationSignal: android.os.CancellationSignal?): Cursor = throw UnsupportedOperationException()
        override fun insert(table: String, conflictAlgorithm: Int, values: android.content.ContentValues): Long = throw UnsupportedOperationException()
        override fun delete(table: String, whereClause: String?, whereArgs: Array<out Any?>?): Int = throw UnsupportedOperationException()
        override fun update(table: String, conflictAlgorithm: Int, values: android.content.ContentValues, whereClause: String?, whereArgs: Array<out Any?>?): Int = throw UnsupportedOperationException()
        override fun compileStatement(sql: String): androidx.sqlite.db.SupportSQLiteStatement = throw UnsupportedOperationException()
        override fun beginTransaction() {}
        override fun beginTransactionNonExclusive() {}
        override fun beginTransactionWithListener(transactionListener: android.database.sqlite.SQLiteTransactionListener) {}
        override fun beginTransactionWithListenerNonExclusive(transactionListener: android.database.sqlite.SQLiteTransactionListener) {}
        override fun endTransaction() {}
        override fun setTransactionSuccessful() {}
        override fun inTransaction(): Boolean = false
        override val isDbLockedByCurrentThread: Boolean = false
        override fun yieldIfContendedSafely(): Boolean = false
        override fun yieldIfContendedSafely(sleepAfterYieldDelay: Long): Boolean = false
        override var version: Int = 6
        override var maximumSize: Long = 0
        override fun setMaximumSize(numBytes: Long): Long = 0L
        override var pageSize: Long = 0
        override val isOpen: Boolean = true
        override val isReadOnly: Boolean = false
        override val path: String? = ""
        override fun setLocale(locale: java.util.Locale) {}
        override fun setMaxSqlCacheSize(cacheSize: Int) {}
        override fun setForeignKeyConstraintsEnabled(enable: Boolean) {}
        override fun enableWriteAheadLogging(): Boolean = false
        override fun disableWriteAheadLogging() {}
        override val isWriteAheadLoggingEnabled: Boolean = false
        override val attachedDbs: List<android.util.Pair<String, String>>? = null
        override val isDatabaseIntegrityOk: Boolean = true
        override fun needUpgrade(newVersion: Int): Boolean = false
    }
}
