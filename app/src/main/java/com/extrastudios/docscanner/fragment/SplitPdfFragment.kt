package com.extrastudios.docscanner.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.BottomAdapter
import com.extrastudios.docscanner.adapter.FilesListAdapter
import com.extrastudios.docscanner.interfaces.OnBackPressedInterface
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.AddRemovePasswordViewModel
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_split_files.*
import kotlinx.android.synthetic.main.storage_camera_permission_view.*
import java.io.File
import java.util.*
import javax.inject.Inject

class SplitPdfFragment : BaseFragments(), OnBackPressedInterface, View.OnClickListener {

    @Inject
    lateinit var textToPDFOptions: TextToPDFOptions

    private val bottomList = ArrayList<String>()
    private var bottomAdapter: BottomAdapter? = null
    private var mPath: String? = null
    private var mSplitPDFUtils: SplitPDFUtils? = null
    private val mFileSelectCode = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_split_files, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity?.application as DocScannerApplication).getAppComponent().inject(this)
        checkStorageAndCameraPermission()
        bottomRecyclerView
        mSplitPDFUtils = SplitPDFUtils(activity!!, preferencesService)
        lottie_progress!!.show()
        layout_view_files.setOnClickListener(this)
        selectFile.setOnClickListener(this)
        splitFiles.setOnClickListener(this)
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null || resultCode != Activity.RESULT_OK || data.data == null) return

        val mRealPath = RealPathUtil.instance.getRealPath(activity!!, data.data!!) ?: ""
        if (mRealPath.isEmpty()) {
            showSnackbar(R.string.file_access_error)
            return
        }

        if (File(mRealPath).isPasswordProtected()) {
            showSnackbar(R.string.encrypted_pdf)
            return
        }

        if (requestCode == mFileSelectCode && data.data != null) {
            val path: String? = RealPathUtil.instance.getRealPath(activity!!, data.data!!)
            if (path != null) {
                setTextAndActivateButtons(path)
            }
        }
    }


    private fun setTextAndActivateButtons(path: String) {
        splitted_files.hide()
        splitfiles_text.hide()
        mPath = path
        val defaultSplitConfig: String? = getDefaultSplitConfig(mPath!!)
        if (defaultSplitConfig != null) {
            mMorphButtonUtility!!.setTextAndActivateButtons(path, selectFile, splitFiles)
            split_config.show()
            split_config.setText(defaultSplitConfig)
        } else resetValues()
    }

    private fun resetValues() {
        mPath = null
        mMorphButtonUtility!!.initializeButton(selectFile, splitFiles)
    }

    private fun getDefaultSplitConfig(mPath: String?): String? {
        val splitConfig = StringBuilder()
        var fileDescriptor: ParcelFileDescriptor? = null
        try {
            if (mPath != null) fileDescriptor =
                ParcelFileDescriptor.open(File(mPath), ParcelFileDescriptor.MODE_READ_ONLY)
            if (fileDescriptor != null) {
                val renderer = PdfRenderer(fileDescriptor)
                val pageCount = renderer.pageCount
                for (i in 1..pageCount) {
                    splitConfig.append(i).append(",")
                }
            }
        } catch (er: Exception) {
            er.printStackTrace()
            showSnackbar(R.string.encrypted_pdf)
            return null
        }
        return splitConfig.toString()
    }

    private fun onItemClick(path: String, index: Int) {
        if (File(path).isPasswordProtected()) {
            showSnackbar(R.string.encrypted_pdf)
            return
        }
        mSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        setTextAndActivateButtons(path)
    }

    private fun splitFiles() {
        hideKeyboard()
        val outputFilePaths = mSplitPDFUtils!!.splitPDFByConfig(mPath, split_config.text.toString())
        val numberOfPages = outputFilePaths.size
        if (numberOfPages == 0) {
            return
        }
        val output = String.format(getString(R.string.split_success), numberOfPages)
        showSnackbar(output)

        outputFilePaths.forEach {
            saveToHistory(it)
        }

        splitfiles_text.show()
        splitfiles_text.text = output
        val splitFilesAdapter = FilesListAdapter(outputFilePaths) { item -> onFileItemClick(item) }
        splitted_files.show()
        splitted_files.adapter = splitFilesAdapter
        splitted_files.setDivider()
        resetValues()
    }

    private fun onFileItemClick(path: String?) {
        mFileUtils!!.openFile(path, FileUtils.FileType.e_PDF)
    }

    private fun saveToHistory(finalOutput: String) {
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, OPERATION_CREATED)
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
            selectFile -> startActivityForResult(mFileUtils!!.fileChooser, mFileSelectCode)
            splitFiles -> splitFiles()
            btnPermission -> checkStorageAndCameraPermission()
        }
    }
}