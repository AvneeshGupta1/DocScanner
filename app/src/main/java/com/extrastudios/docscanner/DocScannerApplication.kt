package com.extrastudios.docscanner

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.avneesh.crashreporter.CrashReporter
import com.extrastudios.docscanner.dagger.ApiComponent
import com.extrastudios.docscanner.dagger.AppModule
import com.extrastudios.docscanner.dagger.DaggerApiComponent
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class DocScannerApplication : MultiDexApplication() {
    private lateinit var mAppComponent: ApiComponent
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this);
        externalCacheDir?.path?.let {
            CrashReporter.initialize(this, it)
        }
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseCrashlytics.getInstance().sendUnsentReports()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        mAppComponent = DaggerApiComponent.builder().appModule(AppModule(this)).build()
    }

    fun getAppComponent(): ApiComponent {
        return mAppComponent
    }

}