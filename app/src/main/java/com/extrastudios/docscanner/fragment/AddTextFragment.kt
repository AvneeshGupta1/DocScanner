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
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.adapter.BottomAdapter
import com.extrastudios.docscanner.adapter.HomeAdapter
import com.extrastudios.docscanner.interfaces.OnBackPressedInterface
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.AddRemovePasswordViewModel
import com.extrastudios.docscanner.viewmodel.AddTextViewModel
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_add_text.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import java.io.File
import javax.inject.Inject

class AddTextFragment : BaseFragments(), View.OnClickListener, OnBackPressedInterface {

    @Inject
    lateinit var textToPDFOptions: TextToPDFOptions
    private var mMaterialDialog: MaterialDialog? = null
    private val bottomList = ArrayList<String>()
    private var bottomAdapter: BottomAdapter? = null
    private var mExcelFileUri: Uri? = null
    private val mFileSelectCode: Int = 89
    private val requestTextDocument: Int = 90
    private val addTextToPdfOptions = ArrayList<CommonItem>()
    private var homeAdapter: HomeAdapter? = null
    private var mPdfPath: String? = null
    private var mTextPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity?.application as DocScannerApplication).getAppComponent().inject(this)
        checkStoragePermission()
        selectFileButton.setOnClickListener(this)
        select_text_file.setOnClickListener(this)
        createPdf.setOnClickListener(this)
        layout_view_files.setOnClickListener(this)
        btnPermission.setOnClickListener(this)
        mMorphButtonUtility?.morphToGrey(createPdf, mMorphButtonUtility!!.integer())
        createPdf.isEnabled = false
        bottomRecyclerView
        mRecyclerView
    }

    private val mRecyclerView by lazy {
        enhancement_options_recycle_view_text.itemAnimator = DefaultItemAnimator()
        enhancement_options_recycle_view_text.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(activity!!, 2)
        enhancement_options_recycle_view_text.layoutManager = layoutManager
        homeAdapter = HomeAdapter(addTextToPdfOptions) { item -> onAddTextItemClick(item) }
        enhancement_options_recycle_view_text.adapter = homeAdapter
    }


    private val bottomRecyclerView by lazy {
        recyclerViewFiles.itemAnimator = DefaultItemAnimator()
        recyclerViewFiles.setHasFixedSize(true)
        recyclerViewFiles.setDivider()
        bottomAdapter = BottomAdapter(bottomList, false) { path, index -> onItemClick(path, index) }
        recyclerViewFiles.adapter = bottomAdapter
    }

    private fun loadAddTextOptionFiles() {
        lottie_progress.show()
        val addRemovePwdViewModel = ViewModelProvider(this).get(AddTextViewModel::class.java)
        addRemovePwdViewModel.getAddTextItems(this, textToPDFOptions).observe(this, Observer {
            lottie_progress.hide()
            addTextToPdfOptions.clear()
            addTextToPdfOptions.addAll(it)
            homeAdapter?.notifyDataSetChanged()
        })
    }

    private fun loadBottomFiles() {
        lottie_progress.show()
        val addRemovePwdViewModel =
            ViewModelProvider(this).get(AddRemovePasswordViewModel::class.java)
        addRemovePwdViewModel.loadAllPdfFiles(ACTION_ADD_PWD).observe(this, Observer {
            lottie_progress.hide()
            bottomList.clear()
            bottomList.addAll(it)
            bottomAdapter?.notifyDataSetChanged()
            bottom_sheet.visibleIf(bottomList.isNotEmpty())
        })
    }

    private fun onAddTextItemClick(type: Int) {
        when (type) {
            FONT_SIZE -> {
                showFontSizeDialog(textToPDFOptions) { loadAddTextOptionFiles() }
            }
            FONT_FAMILY -> {
                showFontFamilyDialog(textToPDFOptions) { loadAddTextOptionFiles() }
            }
        }
    }

    private fun onItemClick(path: String, index: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        if (File(path).isPasswordProtected()) {
            showSnackbar(R.string.encrypted_pdf)
            return
        }

        mSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        mPdfPath = path
        showSnackbar(resources.getString(R.string.snackbar_pdfselected))
        if (mPdfPath != null && mTextPath != null) setTextAndActivateButtons(mPdfPath, mTextPath)
    }

    private fun processUri() {
        openSaveDialog(null) { processFileCreate(it) }
    }

    private fun processFileCreate(name: String) {
        mMaterialDialog =
            MaterialDialog.Builder(activity!!).customView(R.layout.lottie_anim_dialog, false)
                .build()
        mMaterialDialog?.show()

        val viewModel = ViewModelProvider(this).get(AddTextViewModel::class.java)
        viewModel.addText(name, mTextPath!!, mPdfPath!!, textToPDFOptions)
            .observe(this, Observer { path ->
                mMaterialDialog?.let {
                    if (it.isShowing) {
                        it.dismiss()
                    }
                }
                if (path.isNotEmpty()) {
                    saveToHistory(path)
                    activity!!.getSnackbarwithAction(R.string.snackbar_pdfCreated)
                        .setAction(R.string.snackbar_viewAction) {
                            mFileUtils!!.openFile(
                                path,
                                FileUtils.FileType.e_PDF
                            )
                        }.show()
                    textToPDFOptions.reset()
                    loadAddTextOptionFiles()
                }
            })
    }

    private fun saveToHistory(finalOutput: String) {
        mMorphButtonUtility!!.morphToGrey(createPdf, mMorphButtonUtility!!.integer())
        createPdf.isEnabled = false
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, OPERATION_CREATED)
        (activity as HomeActivity).showAdIfLoad()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                mFileSelectCode -> {
                    mSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                    try {
                        mExcelFileUri = data?.data
                        mPdfPath =
                            RealPathUtil.instance.getRealPath(activity!!, mExcelFileUri!!) ?: ""
                        if (mPdfPath.isNullOrEmpty()) {
                            showSnackbar(R.string.file_access_error)
                            return
                        }
                        if (File(mPdfPath!!).isPasswordProtected()) {
                            showSnackbar(R.string.encrypted_pdf)
                            return
                        }
                        showSnackbar(resources.getString(R.string.snackbar_pdfselected))
                        if (mPdfPath != null && mTextPath != null) setTextAndActivateButtons(
                            mPdfPath,
                            mTextPath
                        )
                    } catch (e: Exception) {
                        showSnackbar(R.string.error_occurred)
                    }
                }
                requestTextDocument -> {
                    mTextPath = RealPathUtil.instance.getRealPath(activity!!, data!!.data!!)
                    if (mTextPath != null) {
                        showSnackbar(resources.getString(R.string.snackbar_txtselected))
                    } else {
                        showSnackbar(R.string.snackbar_no_txtselected)
                    }

                    if (mPdfPath != null && mTextPath != null) setTextAndActivateButtons(
                        mPdfPath,
                        mTextPath
                    )
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setTextAndActivateButtons(pdfPath: String?, textPath: String?) {
        if (pdfPath == null || textPath == null) {
            showSnackbar(R.string.error_path_not_found)
            return
        }
        mMorphButtonUtility!!.setTextAndActivateButtons(pdfPath, selectFileButton, createPdf)
        mMorphButtonUtility!!.setTextAndActivateButtons(textPath, select_text_file, createPdf)
    }


    override fun onStoragePermissionAllow() {
        layoutPermission.hide()
        loadBottomFiles()
        loadAddTextOptionFiles()
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
            select_text_file -> {
                val uri = Uri.parse(Environment.getRootDirectory().toString() + "/")
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setDataAndType(uri, "*/*")
                val mimeTypes = arrayOf(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword",
                    getString(R.string.text_type)
                )
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                try {
                    startActivityForResult(
                        Intent.createChooser(
                            intent,
                            java.lang.String.valueOf(R.string.select_file)
                        ), requestTextDocument
                    )
                } catch (ex: ActivityNotFoundException) {
                    showSnackbar(R.string.install_file_manager)
                }
            }
            btnPermission -> checkStoragePermission()
            layout_view_files -> {
                showHideSheet(mSheetBehavior!!)
            }
        }
    }
}