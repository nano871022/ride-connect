package co.japl.android.ev_ride_connect.database

class GoogleDriveBackupHelper {

    suspend fun backupDatabaseToDrive(database: AppDatabase): Boolean {
        // Stub implementation for phase 2 Google Drive App Space backups
        return database.isOpen
    }
}
