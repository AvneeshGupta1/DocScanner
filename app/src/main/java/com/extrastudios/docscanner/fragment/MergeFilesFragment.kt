package com.extrastudios.docscanner.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.adapter.BottomAdapter
import com.extrastudios.docscanner.adapter.HomeAdapter
import com.extrastudios.docscanner.adapter.MergeSelectedFilesAdapter
import com.extrastudios.docscanner.interfaces.MergeFilesListener
import com.extrastudios.docscanner.interfaces.OnBackPressedInterface
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.model.HomeItem
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.AddRemovePasswordViewModel
import com.extrastudios.docscanner.viewmodel.ExcelToPDFViewModel
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_merge_files.*
import kotlinx.android.synthetic.main.storage_camera_permission_view.*
import java.io.File
import java.util.*
import javax.inject.Inject

class MergeFilesFragment : BaseFragments(), View.OnClickListener, MergeFilesListener,
    OnBackPressedInterface, MergeSelectedFilesAdapter.OnFileItemClickListener {

    @Inject
    lateinit var textToPDFOptions: TextToPDFOptions
    private var homeAdapter: HomeAdapter? = null
    private var mFilePaths = ArrayList<String>()
    private val bottomList = ArrayList<String>()
    private val excelToPdfOptions = ArrayList<CommonItem>()
    private var mMergeSelectedFilesAdapter: MergeSelectedFilesAdapter? = null
    private var mMaterialDialog: MaterialDialog? = null
    private var mPasswordProtected = false
    private var mPassword: String? = null
    private var bottomAdapter: BottomAdapter? = null
    private val mFileSelectCode = 10
    private var mPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_merge_files, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity?.application as DocScannerApplication).getAppComponent().inject(this)
        checkStorageAndCameraPermission()
        mRecyclerView
        bottomRecyclerView
        mMergeSelectedFilesAdapter = MergeSelectedFilesAdapter(mFilePaths, this)
        selected_files!!.adapter = mMergeSelectedFilesAdapter
        selected_files!!.setDivider()
        setMorphingButtonState(false)
        selectFiles.setOnClickListener(this)
        pdfOpen.setOnClickListener(this)
        layout_view_files.setOnClickListener(this)
        mergebtn.setOnClickListener(this)
        btnPermission.setOnClickListener(this)
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

    private val bottomRecyclerView by lazy {
        recyclerViewFiles.itemAnimator = DefaultItemAnimator()
        recyclerViewFiles.setHasFixedSize(true)
        recyclerViewFiles.setDivider()
        bottomAdapter = BottomAdapter(bottomList, isMergeFiles = true) { path, index ->
            onItemClick(
                path,
                index
            )
        }
        recyclerViewFiles.adapter = bottomAdapter
    }

    private fun loadBottomFiles() {
        lottie_progress.show()
        val viewModel = ViewModelProvider(this).get(AddRemovePasswordViewModel::class.java)
        viewModel.loadAllPdfFiles(ACTION_ADD_PWD).observe(this, {
            lottie_progress.hide()
            bottomList.clear()
            bottomList.addAll(it)
            bottomAdapter?.notifyDataSetChanged()
            bottom_sheet.visibleIf(bottomList.isNotEmpty())
        })
    }

    fun onItemClick(type: Int) {
        if (mFilePaths.size == 0) {
            showSnackbar(R.string.snackbar_no_pdfs_selected)
            return
        }
        when (type) {
            SET_PASSWORD -> showSetPasswordDialog(textToPDFOptions) {
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
    }

    private fun setPasswordIcon(icon: Int) {
        (excelToPdfOptions[0] as HomeItem).icon = icon
        homeAdapter?.notifyDataSetChanged()
    }


    private fun mergeFiles() {
        val pdfpaths = mFilePaths.toTypedArray()
        openSaveDialog("") {
            MergePdf(
                it,
                preferencesService.storageLocation + pdfDirectory,
                mPasswordProtected,
                mPassword,
                this,
                preferencesService.masterPassword
            ).execute(*pdfpaths)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null || resultCode != Activity.RESULT_OK || data.data == null) return

        val mRealPath = RealPathUtil.instance.getRealPath(activity!!, data.data!!)
        if (mRealPath.isNullOrEmpty()) {
            showSnackbar(R.string.file_access_error)
            return
        }

        if (File(mRealPath).isPasswordProtected()) {
            showSnackbar(R.string.encrypted_pdf)
            return
        }
        mergebtn.show()
        pdfOpen.hide()
        if (requestCode == mFileSelectCode && data.data != null) {
            mFilePaths.add(mRealPath)
            bottomAdapter?.setSelectedItemList(mFilePaths)
            bottomAdapter?.notifyDataSetChanged()
            mMergeSelectedFilesAdapter?.notifyDataSetChanged()
            showSnackbar(getString(R.string.pdf_added_to_list))
            if (mFilePaths.size > 1 && !mergebtn!!.isEnabled) setMorphingButtonState(true)
        }
    }

    private fun onItemClick(path: String?, index: Int) {
        if (File(path).isPasswordProtected()) {
            showSnackbar(R.string.encrypted_pdf)
            return
        }
        mergebtn.show()
        pdfOpen.hide()
        if (mFilePaths.contains(path)) {
            mFilePaths.remove(path)
            showSnackbar(getString(R.string.pdf_removed_from_list))
        } else {
            mFilePaths.add(path!!)
            showSnackbar(getString(R.string.pdf_added_to_list))
        }
        bottomAdapter?.setSelectedItemList(mFilePaths)
        bottomAdapter?.notifyItemChanged(index)
        mMergeSelectedFilesAdapter?.notifyDataSetChanged()
        if (mFilePaths.size > 1) {
            if (!mergebtn!!.isEnabled) setMorphingButtonState(true)
        } else {
            if (mergebtn!!.isEnabled) setMorphingButtonState(false)
        }
    }

    override fun resetValues(isPDFMerged: Boolean, path: String?) {
        mMaterialDialog!!.dismiss()
        if (isPDFMerged) {
            mPath = path
            mergebtn.hide()
            pdfOpen.show()
            getSnackbarwithAction(
                content,
                R.string.pdf_merged
            ).setAction(R.string.snackbar_viewAction) { v ->
                mFileUtils?.openFile(
                    path,
                    FileUtils.FileType.e_PDF
                )
            }.show()
            saveToHistory(path!!)
        } else showSnackbar(R.string.file_access_error)
        setMorphingButtonState(false)
        mFilePaths.clear()
        mMergeSelectedFilesAdapter?.notifyDataSetChanged()
        mPasswordProtected = false
        loadExcelToPdfOptions()
    }

    private fun saveToHistory(finalOutput: String) {
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, OPERATION_CREATED)
        (activity as HomeActivity).showAdIfLoad()
    }

    override fun mergeStarted() {
        mMaterialDialog =
            MaterialDialog.Builder(activity!!).customView(R.layout.lottie_anim_dialog, false)
                .build()
        mMaterialDialog?.show()
    }

    override fun viewFile(path: String?) {
        mFileUtils?.openFile(path, FileUtils.FileType.e_PDF)
    }

    override fun removeFile(path: String?) {
        mFilePaths.remove(path)
        mMergeSelectedFilesAdapter?.notifyDataSetChanged()
        bottomAdapter?.setSelectedItemList(mFilePaths)
        bottomAdapter?.notifyDataSetChanged()
        showSnackbar(getString(R.string.pdf_removed_from_list))
        if (mFilePaths.size < 2 && mergebtn!!.isEnabled) setMorphingButtonState(false)
    }

    override fun moveUp(position: Int) {
        Collections.swap(mFilePaths, position, position - 1)
        mMergeSelectedFilesAdapter?.notifyDataSetChanged()
    }

    override fun moveDown(position: Int) {
        Collections.swap(mFilePaths, position, position + 1)
        mMergeSelectedFilesAdapter?.notifyDataSetChanged()
    }


    private fun setMorphingButtonState(enabled: Boolean) {
        if (enabled) mMorphButtonUtility?.morphToSquare(
            mergebtn,
            mMorphButtonUtility!!.integer()
        ) else mMorphButtonUtility?.morphToGrey(mergebtn, mMorphButtonUtility!!.integer())
        mergebtn!!.isEnabled = enabled
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

    override fun onClick(view: View?) {
        when (view) {
            layout_view_files -> showHideSheet(mSheetBehavior!!)
            selectFiles -> startActivityForResult(mFileUtils!!.fileChooser, mFileSelectCode)
            mergebtn -> mergeFiles()
            btnPermission -> checkStorageAndCameraPermission()
            pdfOpen -> {
                if (!mPath.isNullOrEmpty()) mFileUtils!!.openFile(mPath, FileUtils.FileType.e_PDF)
            }
        }
    }
}