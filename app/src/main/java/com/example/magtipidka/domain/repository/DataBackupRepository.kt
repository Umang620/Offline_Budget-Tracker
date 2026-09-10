package com.example.magtipidka.domain.repository

import com.example.magtipidka.domain.model.BackupData

interface DataBackupRepository {
    suspend fun exportDataJson(): String
    suspend fun importDataJson(jsonString: String): Boolean
    suspend fun clearAllData(): Boolean
}
