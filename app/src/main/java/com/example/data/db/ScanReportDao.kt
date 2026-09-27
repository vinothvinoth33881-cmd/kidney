package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanReportDao {
    @Query("SELECT * FROM scan_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ScanReportEntity>>

    @Query("SELECT * FROM scan_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): ScanReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ScanReportEntity): Long

    @Query("DELETE FROM scan_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("DELETE FROM scan_reports")
    suspend fun deleteAllReports()

    @Query("SELECT COUNT(*) FROM scan_reports")
    fun getReportsCount(): Flow<Int>
}
