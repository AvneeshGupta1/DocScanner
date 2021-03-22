package com.extrastudios.docscanner.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.bottom_sheet.*
import org.jetbrains.anko.alert
import javax.inject.Inject

abstract class BaseFragments : Fragment(), OnPermissionListener {
    var mLastClickTime: Long = 0
    private val openSetting = 101
    var mMorphButtonUtility: MorphButtonUtility? = null

    @Inject
    lateinit var preferencesService: PreferencesService
    var mSheetBehavior: BottomSheetBehavior<*>? = null
    var mFileUtils: FileUtils? = null

    var isRationaleShow: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity?.application as DocScannerApplication).getAppComponent().inject(this)
        mMorphButtonUtility = MorphButtonUtility(activity!!)
        mFileUtils = FileUtils(activity!!)
        bottom_sheet?.let {
            mSheetBehavior = BottomSheetBehavior.from(it)
            mSheetBehavior?.setBottomSheetCallback(BottomSheetCallback(upArrow, true))
        }
    }

    fun checkStorageAndCameraPermission() {
        isRationaleShow = false
        Dexter.withActivity(activity)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (it.areAllPermissionsGranted()) {
                            onStoragePermissionAllow()
                            onCameraPermissionAllow()
                        } else {
                            onPermissionCancel()
                            if (report.isAnyPermissionPermanentlyDenied) {
                                if (!isRationaleShow) showPermissionSettingDialog()
                            }
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    isRationaleShow = true
                    showPermissionRationale(token)
                }
            }).check()
    }

    fun checkStoragePermission() {
        isRationaleShow = false
        Dexter.withActivity(activity).withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    onStoragePermissionAllow()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        if (!isRationaleShow) showPermissionSettingDialog()
                    } else {
                        onPermissionCancel()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                    isRationaleShow = true
                    showPermissionRationale(token)
                }
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == openSetting) {
            if (hasCameraPermission()) {
                onCameraPermissionAllow()
            }
            if (hasWritePermission()) {
                onStoragePermissionAllow()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun showPermissionRationale(token: PermissionToken?) {
        activity?.alert(
            getString(R.string.label_permission_issue),
            getString(R.string.permission_issue)
        ) {
            positiveButton(getString(R.string.label_ok)) { token?.continuePermissionRequest() }
            negativeButton(getString(R.string.label_cancel)) { token?.cancelPermissionRequest() }
        }?.show()
    }

    fun showPermissionSettingDialog() {
        activity?.alert(
            getString(R.string.label_setting_msg),
            getString(R.string.permission_reuired)
        ) {
            positiveButton(getString(R.string.label_ok)) { openAppSettings(openSetting) }
            negativeButton(getString(R.string.label_cancel)) { }
        }?.show()
    }

}