package com.extrastudios.docscanner.dagger

import android.app.Application
import com.extrastudios.docscanner.database.AppDatabase
import dagger.Module
import dagger.Provides

@Module
class DbModule {

    @Provides
    fun provideDb(app: Application): AppDatabase {
        return AppDatabase.getInstance(app)
    }


}