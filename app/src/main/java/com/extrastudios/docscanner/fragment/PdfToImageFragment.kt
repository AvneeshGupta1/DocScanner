package com.extrastudios.docscanner.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils.isEmpty
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.activity.ImagesPreviewActivity
import com.extrastudios.docscanner.adapter.BottomAdapter
import com.extrastudios.docscanner.adapter.ExtractImagesAdapter
import com.extrastudios.docscanner.interfaces.ExtractImagesListener
import com.extrastudios.docscanner.interfaces.OnBackPressedInterface
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.AddRemovePasswordViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_pdf_to_image.*
import kotlinx.android.synthetic.main.storage_camera_permission_view.*
import java.io.File
import java.util.*
import javax.inject.Inject

class PdfToImageFragment : BaseFragments(), ExtractImagesListener,
    ExtractImagesAdapter.OnFileItemClickedListener, OnBackPressedInterface, View.OnClickListener {

    @Inject
    lateinit var textToPDFOptions: TextToPDFOptions
    private var mPath: String? = ""
    private var mUri: Uri? = null
    private var mOutputFilePaths: ArrayList<String>? = null
    private var mMaterialDialog: MaterialDialog? = null
    private var mOperation: Int = 0
    private val mFileSelectCode = 10
    private val bottomList = ArrayList<String>()
    private var bottomAdapter: BottomAdapter? = null
    private var mInputPassword: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pdf_to_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity?.application as DocScannerApplication).getAppComponent().inject(this)
        checkStorageAndCameraPermission()
        bottomRecyclerView
        mOperation = arguments!!.getInt(BUNDLE_DATA)
        shareImages.setOnClickListener(this)
        createImages.setOnClickListener(this)
        layout_view_files.setOnClickListener(this)
        viewImages.setOnClickListener(this)
        selectFile.setOnClickListener(this)
        btnPermission.setOnClickListener(this)
        resetView()
    }

    private val bottomRecyclerView by lazy {
        recyclerViewFiles.itemAnimator = DefaultItemAnimator()
        recyclerViewFiles.setHasFixedSize(true)
        recyclerViewFiles.setDivider()
        bottomAdapter = BottomAdapter(bottomList) { path, index -> onItemClick(path, index) }
        recyclerViewFiles.adapter = bottomAdapter
    }

    private fun loadBottomFiles() {
        lottie_progress.show()
        val viewModel = ViewModelProvider(this).get(AddRemovePasswordViewModel::class.java)
        viewModel.loadAllPdfFiles(ACTION_ADD_PWD).observe(this, androidx.lifecycle.Observer {
            lottie_progress.hide()
            bottomList.clear()
            bottomList.addAll(it)
            bottomAdapter?.notifyDataSetChanged()
            bottom_sheet.visibleIf(bottomList.isNotEmpty())
        })
    }

    private fun onShareFilesClick() {
        if (mOutputFilePaths != null) {
            val fileArrayList = ArrayList<File>()
            for (path in mOutputFilePaths!!) {
                fileArrayList.add(File(path))
            }
            mFileUtils?.shareMultipleFiles(fileArrayList)
        }
    }

    @Throws(NullPointerException::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null || resultCode != Activity.RESULT_OK || data.data == null) return
        if (requestCode == mFileSelectCode) {
            mUri = data.data
            val path: String? = RealPathUtil.instance.getRealPath(activity!!, data.data!!)
            setTextAndActivateButtons(path)
        }
    }

    private fun parse() {
        if (isPDFEncrypted(mPath!!)) {
            MaterialDialog.Builder(activity!!).title(R.string.enter_password)
                .content(R.string.decrypt_protected_file)
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input(null, null) { dialog: MaterialDialog?, input: CharSequence ->
                    if (isEmpty(input)) {
                        showSnackbar(R.string.snackbar_name_not_blank)
                    } else {
                        val inputName = input.toString()
                        mInputPassword = inputName
                        pdfToImage(mInputPassword!!)
                    }
                }.show()
        } else {
            mInputPassword = textToPDFOptions.password
            pdfToImage(mInputPassword)
        }
    }

    private fun pdfToImage(mInputPassword: String?) {
        if (mOperation == PDF_TO_IMAGES) {
            PdfToImages(activity!!, mInputPassword, mPath, mUri, this, preferencesService).execute()
        } else ExtractImages(mPath!!, this, preferencesService.storageLocation).execute()
    }


    fun onItemClick(path: String, index: Int) {
        mUri = null
        mSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        setTextAndActivateButtons(path)
    }

    private fun setTextAndActivateButtons(path: String?) {
        if (path == null) {
            showSnackbar(R.string.error_path_not_found)
            resetView()
            return
        }
        created_images!!.hide()
        options!!.hide()
        pdfToImagesText!!.hide()
        mPath = path
        mMorphButtonUtility?.setTextAndActivateButtons(path, selectFile, createImages)
    }


    override fun onFileItemClick(path: String?) {
        mFileUtils?.openImage(path)
    }


    override fun resetView() {
        mPath = null
        mMorphButtonUtility?.initializeButton(selectFile, createImages)
    }

    override fun extractionStarted() {
        mMaterialDialog =
            MaterialDialog.Builder(activity!!).customView(R.layout.lottie_anim_dialog, false)
                .build()
        mMaterialDialog?.show()
    }

    override fun updateView(imageCount: Int, outputFilePaths: ArrayList<String>) {
        mMaterialDialog!!.dismiss()
        resetView()
        mOutputFilePaths = outputFilePaths
        updateView(imageCount, outputFilePaths, this)
        (activity as HomeActivity).showAdIfLoad()
    }

    private fun updateView(
        imageCount: Int,
        outputFilePaths: ArrayList<String>?,
        listener: ExtractImagesAdapter.OnFileItemClickedListener
    ) {
        if (imageCount == 0) {
            showSnackbar(R.string.extract_images_failed)
            return
        }
        val text = String.format(getString(R.string.extract_images_success), imageCount)
        showSnackbar(text)
        pdfToImagesText.show()
        options.show()
        val extractImagesAdapter = ExtractImagesAdapter(outputFilePaths, listener)
        pdfToImagesText.text = text
        created_images.show()
        created_images.adapter = extractImagesAdapter
        created_images.setDivider()
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
            createImages -> parse()
            shareImages -> onShareFilesClick()
            selectFile -> startActivityForResult(mFileUtils!!.fileChooser, mFileSelectCode)
            layout_view_files -> showHideSheet(mSheetBehavior!!)
            viewImages -> startActivity(
                ImagesPreviewActivity.getStartIntent(
                    activity!!,
                    mOutputFilePaths
                )
            )
            btnPermission -> checkStorageAndCameraPermission()
        }
    }
}