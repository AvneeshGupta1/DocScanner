package com.extrastudios.docscanner.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.extrastudios.docscanner.database.dao.HistoryDao
import com.extrastudios.docscanner.database.entity.History

/**
 * Created by akumar29 on 6/21/2018.
 */
@Database(entities = [(History::class)], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getHistoryDao(): HistoryDao

    companion object {
        private var mInstance: AppDatabase? = null
        private var DB_NAME = "database.db"

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (mInstance == null) {
                mInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration().build()
            }
            return mInstance!!
        }

    }
}