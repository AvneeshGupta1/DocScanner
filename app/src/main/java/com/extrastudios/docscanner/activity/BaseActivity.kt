package com.extrastudios.docscanner.activity

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.extrastudios.docscanner.BuildConfig
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.utils.*
import com.google.android.gms.ads.*
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.OnSuccessListener
import com.google.android.play.core.tasks.TaskExecutors
import kotlinx.android.synthetic.main.dialog_exit_app.view.*
import org.jetbrains.anko.alert
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {
    var toolbar: Toolbar? = null
    private var requestUpdateCode = 1
    var doubleBackToExitPressedOnce = false
    var mLastClickTime: Long = 0
    private  var mInterstitial: com.google.android.gms.ads.InterstitialAd?=null
    private var isGoogleAdLoaded: Boolean = false
    var isNativeAdsLoaded: Boolean = false
    private var mAdView: AdView? = null

    companion object {
        var countDisplay = 0
    }


    @Inject
    lateinit var preferencesService: PreferencesService
    private var installStateUpdatedListener: InstallStateUpdatedListener? = null

    var appUpdateManager: AppUpdateManager? = null

    private var playServiceExecutor: Executor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        (application as DocScannerApplication).getAppComponent().inject(this)
        setContentView(getLayout())
        toolbar = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            val actionbar = supportActionBar
            if (actionbar != null && this !is HomeActivity) {
                actionbar.setDisplayHomeAsUpEnabled(true)
                actionbar.setHomeButtonEnabled(true)
            }
        }
        if (this is HomeActivity) {
            appUpdateManager = AppUpdateManagerFactory.create(this)
            playServiceExecutor = TaskExecutors.MAIN_THREAD
            updateChecker()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.importantForAutofill =
                View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
        if (this is HomeActivity) {
            if (BuildConfig.FREE_VERSION && preferencesService.ratingTime != 0L && !isProAppInstall()) {
                val diff = System.currentTimeMillis() - preferencesService.ratingTime
                val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                if (days >= 1) showProPopup()
            }
            if (BuildConfig.FREE_VERSION) loadNativeAds()
            askForRating()
        }

        val adLayout = findViewById<LinearLayout>(R.id.adLayout)
        if (adLayout != null) {
            val adView = AdView(this)
            showBannerAd(adView, adLayout, this)
        }
    }

    private fun showProPopup() {
        alert(getString(R.string.buy_pro)) {
            cancellable(false)
            positiveButton(getString(R.string.ok_buy_now)) {
                preferencesService.ratingTime = System.currentTimeMillis()
                getPro()
            }
            negativeButton(getString(R.string.later)) {
                preferencesService.ratingTime = System.currentTimeMillis()
            }
            neutralButton(getString(R.string.no_thanks)) {
                preferencesService.ratingTime = System.currentTimeMillis()
            }
        }.show()
    }

    private fun loadNativeAds() {
        mAdView = AdView(this)
        mAdView?.adUnitId = ADMOB_NATIVE_ADS
        mAdView?.adSize = AdSize.MEDIUM_RECTANGLE
        val builder = AdRequest.Builder()
        mAdView?.loadAd(builder.build())
        mAdView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isNativeAdsLoaded = true
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                isNativeAdsLoaded = false
            }
        }
    }

    fun showExitPopup() {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(R.string.do_you_want_to_leave_app)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_exit_app, null)
        dialogView.ll_ads_container_exit.removeAllViews()

        if (isNativeAdsLoaded) {
            mAdView?.let { it ->
                it.parent?.let {
                    (it as ViewGroup).removeAllViews()
                }
                dialogView.ll_ads_container_exit.addView(mAdView)
                dialogBuilder.setView(dialogView)
            }
        }
        dialogBuilder.setPositiveButton(R.string.yes) { _, i ->
            finish()
        }
        dialogBuilder.setNegativeButton(R.string.No) { _, i ->

        }
        dialogBuilder.show()
    }

    private fun loadFullScreenAds() {
        if (BuildConfig.DEBUG || !BuildConfig.FREE_VERSION) {
            return
        }
        mInterstitial = InterstitialAd(this)
        mInterstitial?.adUnitId = ADMOB_FULL_SCREEN_ID
        val builder = AdRequest.Builder()
        mInterstitial?.loadAd(builder.build())
        mInterstitial?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isGoogleAdLoaded = true
            }

            override fun onAdFailedToLoad(errorCode: Int) {

            }
        }
    }


    fun showAdIfLoad() {
        if (!BuildConfig.FREE_VERSION) {
            return
        }
        if (countDisplay++ % 2 == 1) {
            return
        }

        try {
            if (isGoogleAdLoaded) {
                isGoogleAdLoaded = false
                mInterstitial?.show()
            }
        } catch (e: Exception) {

        }
    }

    private fun askForRating() {
        if (!preferencesService.isRatingDone && preferencesService.launchCount > 6) {
            RateDialogManager.showRateDialog(this, object : RateDialogManager.RateUsCallback {
                override fun onRateSuccess() {
                    preferencesService.isRatingDone = true
                }

                override fun onFeedback() {
                    preferencesService.isRatingDone = true
                }

                override fun mayBeLater() {
                    preferencesService.isRatingDone = true
                }
            })
        } else {
            preferencesService.launchCount++
        }
    }

    override fun onResume() {
        super.onResume()
        loadFullScreenAds()
        if (this is HomeActivity) playServiceExecutor?.let {
            appUpdateManager?.appUpdateInfo?.addOnSuccessListener(
                it,
                OnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) updaterDownloadCompleted()
                    } else {
                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                            appUpdateManager?.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.FLEXIBLE,
                                this,
                                requestUpdateCode
                            )
                        }
                    }
                })
        }
    }

    @SuppressLint("SwitchIntDef")
    private fun updateChecker() {
        installStateUpdatedListener = InstallStateUpdatedListener { installState ->
            when (installState.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    updaterDownloadCompleted()
                }
                InstallStatus.INSTALLED -> {
                    installStateUpdatedListener?.let { appUpdateManager?.unregisterListener(it) }
                }
            }
        }
        installStateUpdatedListener?.let {
            appUpdateManager?.registerListener(it)
        }

        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
        playServiceExecutor?.let {
            appUpdateInfoTask?.addOnSuccessListener(it, OnSuccessListener { appUpdateInfo ->
                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        val updateTypes = arrayOf(AppUpdateType.FLEXIBLE, AppUpdateType.IMMEDIATE)
                        run loop@{
                            updateTypes.forEach { type ->
                                if (appUpdateInfo.isUpdateTypeAllowed(type)) {
                                    appUpdateManager?.startUpdateFlowForResult(
                                        appUpdateInfo,
                                        type,
                                        this,
                                        requestUpdateCode
                                    )
                                    return@loop
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun updaterDownloadCompleted() {
        if (this is HomeActivity) {
            this.updateDownloadCompleted()
        }
    }

    abstract fun getLayout(): Int


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finishWithSlideAnimation()
        }
        return super.onOptionsItemSelected(item)
    }

    fun finishWithSlideAnimation() {
        finish()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)

    }

    fun finishWithFade() {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}