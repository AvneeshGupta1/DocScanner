package com.extrastudios.docscanner.dagger

import android.app.Application
import com.extrastudios.docscanner.utils.ImageToPDFOptions
import com.extrastudios.docscanner.utils.TextToPDFOptions
import com.orhanobut.hawk.Hawk
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private var app: Application) {
    @Singleton
    @Provides
    fun provideApplication(): Application {
        return app
    }

    @Singleton
    @Provides
    fun providePreferenceService(): PreferencesService {
        if (!Hawk.isBuilt()) Hawk.init(app).build()
        return PreferencesService()
    }

    @Singleton
    @Provides
    fun provideString(app: Application): StringService {
        return StringService(app)
    }

    @Provides
    fun provideTextToPdfOptions(preferencesService: PreferencesService) =
        TextToPDFOptions(preferencesService)

    @Provides
    fun provideImageToPdfOptions(preferencesService: PreferencesService) =
        ImageToPDFOptions(preferencesService)
}