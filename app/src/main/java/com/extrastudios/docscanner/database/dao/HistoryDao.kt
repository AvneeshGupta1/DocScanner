package com.extrastudios.docscanner.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.extrastudios.docscanner.database.entity.History

/**
 * Created by akumar29 on 6/21/2018.
 */
@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleHistoryRecords(vararg records: History): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHistoryRecord(record: History): Long

    @Query("delete from history")
    fun deleteHistory()

    @Query("select * from history where operationType IN(:types) order by id desc")
    fun getHistoryByOperationType(vararg types: Int): List<History>


    @Query("select * from history order by id desc")
    fun getHistory(): List<History>

}