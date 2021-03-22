package com.extrastudios.docscanner.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.activity.RearrangePdfPages
import com.extrastudios.docscanner.adapter.BottomAdapter
import com.extrastudios.docscanner.interfaces.OnBackPressedInterface
import com.extrastudios.docscanner.interfaces.OnPDFCompressedInterface
import com.extrastudios.docscanner.interfaces.OnPdfReorderedInterface
import com.extrastudios.docscanner.model.PDFFile
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.AddRemovePasswordViewModel
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_remove_pages.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import java.io.File
import java.util.*

class RemovePagesFragment : BaseFragments(), OnPDFCompressedInterface, OnBackPressedInterface,
    OnPdfReorderedInterface, View.OnClickListener {

    private var mPath: String? = null
    private var mOperation: Int = 0
    private var mMaterialDialog: MaterialDialog? = null
    private var mUri: Uri? = null
    private var bottomAdapter: BottomAdapter? = null
    private val bottomList = ArrayList<String>()
    private val mFileSelectCode = 10
    private val rearrangePdfCode = 11

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_remove_pages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkStoragePermission()
        bottomRecyclerView
        mOperation = arguments!!.getInt(BUNDLE_DATA)
        layout_view_files.setOnClickListener(this)
        pdfCreate.setOnClickListener(this)
        selectFile.setOnClickListener(this)
        btnPermission.setOnClickListener(this)
        resetValues()
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


    @Throws(NullPointerException::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null || resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            mFileSelectCode -> {
                if (data.data != null) {
                    val mRealPath = RealPathUtil.instance.getRealPath(activity!!, data.data!!)
                    if (mRealPath.isNullOrEmpty()) {
                        showSnackbar(R.string.file_access_error)
                        return
                    }
                    if (File(mRealPath).isPasswordProtected()) {
                        showSnackbar(R.string.encrypted_pdf)
                        return
                    }
                    setTextAndActivateButtons(mRealPath)
                }
            }
            rearrangePdfCode -> {
                val pages = data.getStringExtra(RESULT)
                val sameFile = data.getBooleanExtra("SameFile", false)
                if (mPath == null) return
                val outputPath: String
                outputPath = setPath(pages!!)
                if (!sameFile) {
                    val pdfEncryptionUtility = PDFEncryptionUtility(activity!!, preferencesService)

                    pdfEncryptionUtility.reorderRemovePDF(mPath!!, outputPath, pages) {
                        viewPdfButton(outputPath)
                    }
                } else {
                    showSnackbar(R.string.file_order)
                }
                resetValues()
            }
        }
    }

    private fun setPath(pages: String): String {
        val outputPath: String = if (pages.length > 50) {
            mPath!!.replace(pdfExtension, "_edited$pdfExtension")
        } else {
            mPath!!.replace(pdfExtension, "_edited$pages$pdfExtension")
        }
        return outputPath
    }

    fun parse() {
        hideKeyboard()
        if (mOperation == COMPRESS_PDF) {
            compressPDF()
            return
        }

        ReorderPdfPagesAsync(mUri, mPath, activity!!, this).execute()
    }

    private fun compressPDF() {
        val input = pages!!.text.toString()
        val check: Int
        try {
            if (input.isEmpty()) {
                showSnackbar(R.string.entry_empty)
                return
            }
            check = input.toInt()
            if (check > 100 || check <= 0 || mPath == null) {
                showSnackbar(R.string.invalid_entry)
            } else {
                val outputPath = mPath!!.replace(pdfExtension, "_edited$check$pdfExtension")
                CompressPdfAsync(mPath!!, outputPath, 100 - check, this).execute()
            }
        } catch (e: NumberFormatException) {
            showSnackbar(R.string.invalid_entry)
        }
    }

    private fun resetValues() {
        mPath = null
        pages!!.text = null
        mMorphButtonUtility?.initializeButton(selectFile, pdfCreate)
        when (mOperation) {
            REORDER_PAGES, REMOVE_PAGES -> {
                infoText!!.hide()
                pages!!.hide()
            }
            COMPRESS_PDF -> infoText.setText(R.string.compress_pdf_prompt)
        }
    }

    fun onItemClick(path: String, index: Int) {
        mSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        setTextAndActivateButtons(path)
    }

    private fun setTextAndActivateButtons(path: String?) {
        if (path == null) {
            showSnackbar(R.string.file_access_error)
            resetValues()
            return
        }
        mPath = path
        compressionInfoText!!.hide()
        view_pdf!!.hide()
        mMorphButtonUtility?.setTextAndActivateButtons(path, selectFile, pdfCreate)
    }

    override fun pdfCompressionStarted() {
        mMaterialDialog =
            MaterialDialog.Builder(activity!!).customView(R.layout.lottie_anim_dialog, false)
                .build()
        mMaterialDialog?.show()
    }


    override fun pdfCompressionEnded(path: String, success: Boolean) {
        mMaterialDialog!!.dismiss()
        if (success && mPath != null) {
            getSnackbarwithAction(
                tableLayout,
                R.string.snackbar_pdfCreated
            ).setAction(R.string.snackbar_viewAction) { v ->
                mFileUtils?.openFile(
                    path,
                    FileUtils.FileType.e_PDF
                )
            }.show()
            saveToHistory(path)
            val input = File(mPath)
            val output = File(path)
            viewPdfButton(path)
            compressionInfoText!!.show()
            compressionInfoText!!.text = String.format(
                getString(R.string.compress_info),
                PDFFile(input, false).formattedSize,
                PDFFile(output, false).formattedSize
            )
        } else {
            showSnackbar(R.string.encrypted_pdf)
        }
        resetValues()
    }

    private fun saveToHistory(finalOutput: String) {
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, OPERATION_CREATED)
        (activity as HomeActivity).showAdIfLoad()
    }

    private fun viewPdfButton(path: String) {
        view_pdf!!.visibility = View.VISIBLE
        view_pdf!!.setOnClickListener { v: View? ->
            mFileUtils?.openFile(
                path,
                FileUtils.FileType.e_PDF
            )
        }
    }


    override fun onPdfReorderStarted() {
        mMaterialDialog =
            MaterialDialog.Builder(activity!!).customView(R.layout.lottie_anim_dialog, false)
                .build()
        mMaterialDialog?.show()
    }

    override fun onPdfReorderCompleted(bitmaps: MutableList<Bitmap>) {
        mMaterialDialog!!.dismiss()
        RearrangePdfPages.mImages = ArrayList(bitmaps)
        bitmaps.clear() //releasing memory
        startActivityForResult(RearrangePdfPages.getStartIntent(activity!!), rearrangePdfCode)
    }

    override fun onPdfReorderFailed() {
        mMaterialDialog!!.dismiss()
        showSnackbar(R.string.file_access_error)
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

    override fun onClick(view: View?) {
        when (view) {
            layout_view_files -> showHideSheet(mSheetBehavior!!)
            selectFile -> startActivityForResult(mFileUtils?.fileChooser, mFileSelectCode)
            pdfCreate -> parse()
            btnPermission -> checkStoragePermission()
        }
    }
}