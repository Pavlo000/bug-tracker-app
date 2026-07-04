package com.uopeople.bugtracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IssueDao {

    @Query("SELECT * FROM issues WHERE syncStatus != 'PENDING_DELETE' ORDER BY createdAt DESC")
    fun getAllIssues(): Flow<List<IssueEntity>>

    @Query("SELECT * FROM issues WHERE id = :id")
    suspend fun getIssueById(id: Long): IssueEntity?

    @Query("SELECT * FROM issues WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getIssueByRemoteId(remoteId: Long): IssueEntity?

    @Query(
        """
        SELECT * FROM issues
        WHERE syncStatus IN ('PENDING_CREATE', 'PENDING_UPDATE', 'PENDING_DELETE', 'SYNC_FAILED')
        ORDER BY createdAt ASC
        """
    )
    suspend fun getPendingSyncIssues(): List<IssueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(issue: IssueEntity): Long

    @Update
    suspend fun update(issue: IssueEntity)

    @Delete
    suspend fun delete(issue: IssueEntity)

    @Query("DELETE FROM issues WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM issues WHERE syncStatus = 'PENDING_DELETE' AND remoteId IS NULL")
    suspend fun purgeLocalOnlyDeletes()
}
