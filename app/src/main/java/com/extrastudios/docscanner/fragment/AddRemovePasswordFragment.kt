package com.extrastudios.docscanner.fragment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.adapter.BottomAdapter
import com.extrastudios.docscanner.interfaces.OnBackPressedInterface
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.AddRemovePasswordViewModel
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_add_password.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import java.io.File

class AddRemovePasswordFragment : BaseFragments(), View.OnClickListener, OnBackPressedInterface {

    private val bottomList = ArrayList<String>()
    private var bottomAdapter: BottomAdapter? = null
    private var mExcelFileUri: Uri? = null
    private var mRealPath: String? = ""
    private val mFileSelectCode: Int = 89
    private var action: Int = 0
    private var mPath: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        action = arguments?.getInt(ACTION_TYPE) ?: 1
        selectFileButton.setOnClickListener(this)
        createPdf.setOnClickListener(this)
        layout_view_files.setOnClickListener(this)
        btnPermission.setOnClickListener(this)
        pdfOpen.setOnClickListener(this)
        checkStoragePermission()
        mMorphButtonUtility?.morphToGrey(createPdf, mMorphButtonUtility!!.integer())
        createPdf.isEnabled = false
        bottomRecyclerView
        resetValue()
    }

    private fun resetValue() {
        mRealPath = null
        mMorphButtonUtility?.initializeButton(selectFileButton, createPdf)
    }

    private val bottomRecyclerView by lazy {
        recyclerViewFiles.itemAnimator = DefaultItemAnimator()
        recyclerViewFiles.setHasFixedSize(true)
        recyclerViewFiles.setDivider()
        bottomAdapter = BottomAdapter(
            bottomList,
            action == ACTION_REMOVE_PWD
        ) { path, index -> onItemClick(path, index) }
        recyclerViewFiles.adapter = bottomAdapter
    }

    private fun loadBottomFiles() {
        lottie_progress.show()
        val addRemovePwdViewModel =
            ViewModelProvider(this).get(AddRemovePasswordViewModel::class.java)
        addRemovePwdViewModel.loadAllPdfFiles(action).observe(this, Observer {
            lottie_progress.hide()
            bottomList.clear()
            bottomList.addAll(it)
            bottomAdapter?.notifyDataSetChanged()
            bottom_sheet.visibleIf(bottomList.isNotEmpty())
        })
    }

    private fun onItemClick(path: String, index: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        mSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        mMorphButtonUtility!!.setTextAndActivateButtons(path, selectFileButton, createPdf)
        mRealPath = path
        pdfOpen.hide()
        createPdf.show()
    }

    private fun processUri() {
        val pdfEncryptionUtility = PDFEncryptionUtility(activity!!, preferencesService)
        if (action == ACTION_ADD_PWD) {
            if (!File(mRealPath).isPasswordProtected()) {
                pdfEncryptionUtility.setPassword(mRealPath!!) { path, action ->
                    saveToHistory(
                        path,
                        action
                    )
                }
            } else {
                showSnackbar(R.string.encrypted_pdf)
            }
            return
        }

        if (action == ACTION_REMOVE_PWD) {
            if (File(mRealPath).isPasswordProtected()) {
                pdfEncryptionUtility.removePassword(mRealPath!!) { path, action ->
                    saveToHistory(
                        path,
                        action
                    )
                }
            } else {
                showSnackbar(R.string.not_encrypted)
            }
            return
        }
    }

    private fun saveToHistory(finalOutput: String, action: Int) {
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, action)
        (activity as HomeActivity).showAdIfLoad()
        mPath = finalOutput
        pdfOpen.show()
        createPdf.hide()
        resetValue()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == mFileSelectCode) {
            mSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            try {
                mExcelFileUri = data?.data
                mRealPath = RealPathUtil.instance.getRealPath(activity!!, mExcelFileUri!!) ?: ""
                if (mRealPath.isNullOrEmpty()) {
                    showSnackbar(R.string.file_access_error)
                    return
                }
                mPath = ""
                pdfOpen.hide()
                createPdf.show()
                processUri()
            } catch (e: Exception) {
                showSnackbar(R.string.error_occurred)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStoragePermissionAllow() {
        layoutPermission.hide()
        loadBottomFiles()
    }

    override fun onCameraPermissionAllow() {

    }

    override fun onPermissionCancel() {
        layoutPermission.show()
    }


    override fun closeBottomSheet() {
        closeSheet(mSheetBehavior!!)
    }

    override fun checkSheetBehaviour(): Boolean {
        return mSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED
    }


    override fun onClick(v: View?) {
        when (v) {
            createPdf -> {
                processUri()
            }
            selectFileButton -> {
                val uri = Uri.parse(Environment.getRootDirectory().toString() + "/")
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setDataAndType(uri, "application/pdf")
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                try {
                    startActivityForResult(
                        Intent.createChooser(
                            intent,
                            getString(R.string.select_file)
                        ), mFileSelectCode
                    )
                } catch (ex: ActivityNotFoundException) {
                    showSnackbar(R.string.install_file_manager)
                }
            }
            btnPermission -> checkStoragePermission()
            layout_view_files -> {
                showHideSheet(mSheetBehavior!!)
            }
            pdfOpen -> if (!mPath.isNullOrEmpty()) mFileUtils!!.openFile(
                mPath,
                FileUtils.FileType.e_PDF
            )
        }
    }

}