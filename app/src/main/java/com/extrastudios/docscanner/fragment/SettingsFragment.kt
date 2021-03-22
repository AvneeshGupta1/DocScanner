package com.extrastudios.docscanner.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.HomeAdapter
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.SettingsViewModel
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import lib.folderpicker.FolderPicker
import javax.inject.Inject

class SettingsFragment : BaseFragments(), View.OnClickListener {

    @Inject
    lateinit var imagePdfOptions: ImageToPDFOptions

    @Inject
    lateinit var ttPdfOption: TextToPDFOptions

    private var homeAdapter: HomeAdapter? = null
    private var settingsOptions = ArrayList<CommonItem>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity?.application as DocScannerApplication).getAppComponent().inject(this)
        checkStoragePermission()
        btnPermission.setOnClickListener(this)
        storageLocation.setOnClickListener(this)
        storageLocation.text = preferencesService.storageLocation + pdfDirectory
        settingsRecyclerView
    }


    private val settingsRecyclerView by lazy {
        settingsRecylerView.itemAnimator = DefaultItemAnimator()
        settingsRecylerView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(activity, 2)
        settingsRecylerView.layoutManager = layoutManager
        homeAdapter = HomeAdapter(settingsOptions) { item -> onItemClick(item) }
        settingsRecylerView.adapter = homeAdapter
    }

    private fun loadSettingsOptions() {
        val settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        settingsViewModel.getImageToPdfItems(this, preferencesService).observe(this, Observer {
            settingsOptions.clear()
            settingsOptions.addAll(it)
            homeAdapter?.notifyDataSetChanged()
        })
    }

    private fun onItemClick(type: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (type) {
            IMAGE_COMPRESSION -> {
                showImageCompressionDialog(imagePdfOptions, true) { loadSettingsOptions() }
            }
            SET_PAGE_SIZE -> {
                showPageSizeDialog(imagePdfOptions, true)
            }
            FONT_SIZE -> {
                showFontSizeDialog(ttPdfOption, true) { loadSettingsOptions() }
            }
            FONT_FAMILY -> {
                showFontFamilyDialog(ttPdfOption, true) { loadSettingsOptions() }
            }
            SET_IMAGE_SCALE_TYPE -> {
                showImageScaleTypeDialog(imagePdfOptions, true)
            }
            CHANGE_MASTER_PASSWORD -> {
                showMasterPasswordDialog(imagePdfOptions)
            }
            SHOW_PAGE_NUMBERS -> {
                showPageNumberStyleDialog(imagePdfOptions, true)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MODIFY_STORAGE_LOCATION_CODE) {
            if (data!!.extras != null) {
                val folderLocation = data.extras!!.getString("data") ?: ""
                preferencesService.storageLocation = folderLocation
                showSnackbar(R.string.storage_location_modified)
                storageLocation.text = preferencesService.storageLocation + pdfDirectory
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View?) {
        when (v) {
            storageLocation -> {
                val intent = Intent(activity, FolderPicker::class.java)
                startActivityForResult(intent, MODIFY_STORAGE_LOCATION_CODE)
            }
            btnPermission -> {
                checkStoragePermission()
            }
        }
    }

    override fun onStoragePermissionAllow() {
        layoutPermission.hide()
        loadSettingsOptions()
    }


    override fun onCameraPermissionAllow() {

    }

    override fun onPermissionCancel() {
        layoutPermission.show()
    }
}