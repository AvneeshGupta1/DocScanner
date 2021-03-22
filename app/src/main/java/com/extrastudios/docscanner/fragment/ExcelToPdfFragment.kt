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
import com.extrastudios.docscanner.model.HomeItem
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.ExcelToPDFViewModel
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_excel_to_pdf.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import java.io.File
import javax.inject.Inject

class ExcelToPdfFragment : BaseFragments(), View.OnClickListener, OnBackPressedInterface {

    @Inject
    lateinit var textToPDFOptions: TextToPDFOptions
    private var mDestPath: String = ""
    private var mMaterialDialog: MaterialDialog? = null
    private val excelToPdfOptions = ArrayList<CommonItem>()
    private val bottomList = ArrayList<String>()
    private var homeAdapter: HomeAdapter? = null
    private var bottomAdapter: BottomAdapter? = null
    private var mExcelFileUri: Uri? = null
    private var mRealPath: String = ""
    private var mPasswordProtected = false
    private var mPassword: String? = null
    private val mFileSelectCode: Int = 89

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_excel_to_pdf, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity?.application as DocScannerApplication).getAppComponent().inject(this)
        create_excel_to_pdf.setOnClickListener(this)
        select_excel_file.setOnClickListener(this)
        open_pdf.setOnClickListener(this)
        btnPermission.setOnClickListener(this)
        layout_view_files.setOnClickListener(this)
        checkStoragePermission()
        mMorphButtonUtility?.morphToGrey(create_excel_to_pdf, mMorphButtonUtility!!.integer())
        create_excel_to_pdf.isEnabled = false
        mRecyclerView
        bottomRecyclerView
    }

    private val mRecyclerView by lazy {
        enhancement_options_recycle_view.itemAnimator = DefaultItemAnimator()
        enhancement_options_recycle_view.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(activity!!, 2)
        enhancement_options_recycle_view.layoutManager = layoutManager
        homeAdapter = HomeAdapter(excelToPdfOptions) { item -> onItemClick(item) }
        enhancement_options_recycle_view.adapter = homeAdapter
    }

    private fun loadExcelToPdfOptions() {
        val excelToPDFViewModel = ViewModelProvider(this).get(ExcelToPDFViewModel::class.java)
        excelToPDFViewModel.getExcelToPdfItems(activity!!).observe(this, {
            excelToPdfOptions.clear()
            excelToPdfOptions.addAll(it)
            homeAdapter?.notifyDataSetChanged()
        })
    }

    private fun onItemClick(type: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        when (type) {
            SET_PASSWORD -> {
                if (mExcelFileUri == null) {
                    showSnackbar(R.string.no_excel_file)
                    return
                }
                setPassword()
            }
        }
    }

    private val bottomRecyclerView by lazy {
        recyclerViewFiles.itemAnimator = DefaultItemAnimator()
        recyclerViewFiles.setHasFixedSize(true)
        recyclerViewFiles.setDivider()
        bottomAdapter = BottomAdapter(bottomList) { path, index -> onExcelClick(path, index) }
        recyclerViewFiles.adapter = bottomAdapter
    }

    private fun loadBottomFiles() {
        lottie_progress.show()
        val excelToPDFViewModel = ViewModelProvider(this).get(ExcelToPDFViewModel::class.java)
        excelToPDFViewModel.loadAllExcelFiles().observe(this, Observer {
            lottie_progress.hide()
            bottomList.clear()
            bottomList.addAll(it)
            bottomAdapter?.notifyDataSetChanged()
            bottom_sheet.visibleIf(bottomList.isNotEmpty())
        })
    }

    private fun onExcelClick(path: String, index: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        mSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        mExcelFileUri = activity!!.getUriFromFile(File(path))
        mRealPath = path
        processUri()
    }

    private fun processUri() {
        val fileName: String? = mRealPath.getFileName()
        if (fileName != null && !fileName.endsWith(excelExtension) && !fileName.endsWith(
                excelWorkbookExtension
            )
        ) {
            showSnackbar(R.string.extension_not_supported)
            return
        }
        showSnackbar(resources.getString(R.string.excel_selected))
        create_excel_to_pdf.isEnabled = true
        create_excel_to_pdf.unblockTouch()
        mMorphButtonUtility?.morphToSquare(create_excel_to_pdf, mMorphButtonUtility!!.integer())
        open_pdf.hide()
    }

    private fun convertToPdf(pdfFileName: String) {
        mMaterialDialog =
            MaterialDialog.Builder(activity!!).customView(R.layout.lottie_anim_dialog, false)
                .build()
        mMaterialDialog?.show()
        val excelToPDFViewModel = ViewModelProvider(this).get(ExcelToPDFViewModel::class.java)
        val mStorePath: String = preferencesService.storageLocation + pdfDirectory
        mDestPath = mStorePath + pdfFileName + pdfExtension
        excelToPDFViewModel.convertToPdf(mRealPath, mDestPath, mPasswordProtected, mPassword)
            .observe(this, Observer { it ->
                mMaterialDialog?.let {
                    if (it.isShowing) {
                        it.dismiss()
                    }
                }
                if (it) {
                    saveToHistory(mDestPath)
                    getSnackbarwithAction(
                        content,
                        R.string.snackbar_pdfCreated
                    ).setAction(R.string.snackbar_viewAction) { v ->
                        mFileUtils?.openFile(
                            mDestPath,
                            FileUtils.FileType.e_PDF
                        )
                    }.show()
                    open_pdf.show()

                    mMorphButtonUtility!!.morphToSuccess(create_excel_to_pdf)
                    create_excel_to_pdf.blockTouch()
                    mMorphButtonUtility!!.morphToGrey(
                        create_excel_to_pdf,
                        mMorphButtonUtility!!.integer()
                    )
                    mExcelFileUri = null
                    mPasswordProtected = false
                    textToPDFOptions.password = null
                    setPasswordIcon(R.drawable.baseline_enhanced_encryption_24)
                } else {
                    showSnackbar(R.string.error_pdf_not_created)
                    mMorphButtonUtility!!.morphToGrey(
                        create_excel_to_pdf,
                        mMorphButtonUtility!!.integer()
                    )
                    create_excel_to_pdf.setEnabled(false)
                    mExcelFileUri = null
                }
            })
    }

    private fun saveToHistory(finalOutput: String) {
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, OPERATION_CREATED)
        (activity as HomeActivity).showAdIfLoad()
    }

    private fun setPassword() {
        showSetPasswordDialog(textToPDFOptions) {
            if (!textToPDFOptions.password.isNullOrEmpty()) {
                mPassword = null
                setPasswordIcon(R.drawable.baseline_done_24)
                mPasswordProtected = false
            }
            if (textToPDFOptions.password.isNullOrEmpty()) {
                setPasswordIcon(R.drawable.baseline_enhanced_encryption_24)
                showSnackbar(R.string.password_remove)
            }
        }
    }

    private fun setPasswordIcon(icon: Int) {
        (excelToPdfOptions[0] as HomeItem).icon = icon
        homeAdapter?.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == mFileSelectCode) {
            try {
                mExcelFileUri = data?.data
                mRealPath = RealPathUtil.instance.getRealPath(activity!!, mExcelFileUri!!) ?: ""
                processUri()
            } catch (e: Exception) {
                showSnackbar(R.string.error_occurred)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onStoragePermissionAllow() {
        layoutPermission.hide()
        loadExcelToPdfOptions()
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
            create_excel_to_pdf -> {
                openSaveDialog(null) { filename -> convertToPdf(filename) }
            }
            select_excel_file -> {
                val uri = Uri.parse(Environment.getRootDirectory().toString() + "/")
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setDataAndType(uri, "application/vnd.ms-excel")
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                try {
                    startActivityForResult(
                        Intent.createChooser(
                            intent,
                            java.lang.String.valueOf(R.string.select_file)
                        ), mFileSelectCode
                    )
                } catch (ex: ActivityNotFoundException) {
                    showSnackbar(R.string.install_file_manager)
                }
            }
            btnPermission -> checkStoragePermission()
            open_pdf -> {
                mFileUtils?.openFile(mDestPath, FileUtils.FileType.e_PDF)
            }
            layout_view_files -> {
                showHideSheet(mSheetBehavior!!)
            }
        }
    }
}