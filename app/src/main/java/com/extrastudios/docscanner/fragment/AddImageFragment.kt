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
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.adapter.BottomAdapter
import com.extrastudios.docscanner.interfaces.OnBackPressedInterface
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.AddRemovePasswordViewModel
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.zhihu.matisse.Matisse
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_add_images.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import java.io.File

class AddImageFragment : BaseFragments(), View.OnClickListener, OnBackPressedInterface {

    private val mImagesUri = ArrayList<String>()
    private val bottomList = ArrayList<String>()
    private var bottomAdapter: BottomAdapter? = null
    private var mPdfFileUri: Uri? = null
    private val mFileSelectCode: Int = 89
    private val requestGetImages = 13
    private var mPath: String? = null
    private var mPDFUtil: PDFUtil? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_images, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPDFUtil = PDFUtil(activity!!)
        resetValues()
        selectFileButton.setOnClickListener(this)
        createPdf.setOnClickListener(this)
        addImages.setOnClickListener(this)
        btnPermission.setOnClickListener(this)
        pdfOpen.setOnClickListener(this)
        viewFiles.setOnClickListener(this)
        checkStoragePermission()
        mMorphButtonUtility?.morphToGrey(createPdf, mMorphButtonUtility!!.integer())
        createPdf.isEnabled = false
        bottomRecyclerView
    }

    private fun resetValues() {
        mImagesUri.clear()
        mMorphButtonUtility!!.initializeButton(selectFileButton, createPdf)
        mMorphButtonUtility!!.initializeButton(selectFileButton, addImages)
        mNoOfImages.hide()
    }

    private val bottomRecyclerView by lazy {
        recyclerViewFiles.itemAnimator = DefaultItemAnimator()
        recyclerViewFiles.setHasFixedSize(true)
        recyclerViewFiles.setDivider()
        bottomAdapter = BottomAdapter(bottomList, false) { path, index -> onItemClick(path, index) }
        recyclerViewFiles.adapter = bottomAdapter
    }

    private fun loadBottomFiles() {
        lottie_progress.show()
        val addRemovePwdViewModel =
            ViewModelProvider(this).get(AddRemovePasswordViewModel::class.java)
        addRemovePwdViewModel.loadAllPdfFiles(ADD_PASSWORD).observe(this, Observer {
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
        setTextAndActivateButtons(path)
        createPdf.setText(R.string.create_pdf)
        mMorphButtonUtility?.morphToGrey(createPdf, mMorphButtonUtility!!.integer())
        createPdf.setTextColor(resources.getColor(R.color.mb_white))
        createPdf.isEnabled = false
    }

    private fun saveToHistory(finalOutput: String, action: Int) {
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, action)
        pdfOpen.show()
        mPath = finalOutput
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                mFileSelectCode -> {
                    mSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                    try {
                        mPdfFileUri = data?.data
                        val mRealPath =
                            RealPathUtil.instance.getRealPath(activity!!, mPdfFileUri!!) ?: ""
                        if (mRealPath.isEmpty()) {
                            showSnackbar(R.string.file_access_error)
                            return
                        }
                        if (File(mRealPath).isPasswordProtected()) {
                            showSnackbar(R.string.encrypted_pdf)
                            return
                        }
                        setTextAndActivateButtons(mRealPath)
                    } catch (e: Exception) {
                        showSnackbar(R.string.error_occurred)
                    }
                    pdfOpen.hide()
                }
                requestGetImages -> {
                    mImagesUri.clear()
                    mImagesUri.addAll(Matisse.obtainPathResult(data))

                    if (mImagesUri.size > 0) {
                        mNoOfImages.text =
                            String.format(getString(R.string.images_selected), mImagesUri.size)
                        mNoOfImages.show()
                        showSnackbar(R.string.snackbar_images_added)
                        createPdf.isEnabled = true

                    } else {
                        mNoOfImages.hide()
                    }
                    pdfOpen.hide()
                    mMorphButtonUtility!!.morphToSquare(createPdf, mMorphButtonUtility!!.integer())
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setTextAndActivateButtons(path: String) {
        mPath = path
        pdfOpen.hide()
        mMorphButtonUtility!!.setTextAndActivateButtons(path, selectFileButton, addImages)
    }

    private fun createPdf() {
        openSaveDialog(null) {
            addImagesToPdf(it)
        }
    }

    private fun addImagesToPdf(output: String) {
        val index = mPath!!.lastIndexOf("/")
        val outputPath = mPath!!.replace(mPath!!.substring(index + 1), output + pdfExtension)
        if (mImagesUri.size > 0) {
            val progressDialog: MaterialDialog =
                MaterialDialog.Builder(activity!!).customView(R.layout.lottie_anim_dialog, false)
                    .build()
            progressDialog.show()
            mPDFUtil?.addImagesToPdf(mPath, outputPath, mImagesUri) { path, action ->
                saveToHistory(
                    path,
                    action
                )
            }
            mMorphButtonUtility!!.morphToSuccess(createPdf)
            resetValues()
            progressDialog.dismiss()
            (activity as HomeActivity).showAdIfLoad()
        } else {
            showSnackbar(R.string.no_images_selected)
        }
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
                createPdf()
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
            addImages -> {
                ImageUtils.selectImages(this, requestGetImages)
            }
            viewFiles -> showHideSheet(mSheetBehavior!!)
            btnPermission -> checkStoragePermission()
            pdfOpen -> {
                if (!mPath.isNullOrEmpty()) mFileUtils!!.openFile(mPath, FileUtils.FileType.e_PDF)
            }
        }
    }
}