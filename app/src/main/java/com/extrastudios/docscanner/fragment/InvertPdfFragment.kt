package com.extrastudios.docscanner.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.adapter.BottomAdapter
import com.extrastudios.docscanner.interfaces.OnBackPressedInterface
import com.extrastudios.docscanner.interfaces.OnPDFCreatedInterface
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.AddRemovePasswordViewModel
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_invert_pdf.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import java.io.File
import java.util.*

class InvertPdfFragment : BaseFragments(), OnPDFCreatedInterface, OnBackPressedInterface,
    View.OnClickListener {

    private val mFileSelectCode = 10
    private var mPath: String? = null
    private var mMaterialDialog: MaterialDialog? = null
    private var bottomAdapter: BottomAdapter? = null
    private val bottomList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_invert_pdf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkStoragePermission()
        bottomRecyclerView
        layout_view_files.setOnClickListener(this)
        selectFile.setOnClickListener(this)
        invert.setOnClickListener(this)
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
        if (requestCode == mFileSelectCode) {
            setTextAndActivateButtons(mRealPath)
        }
    }

    private fun resetValues() {
        mPath = null
        mMorphButtonUtility?.initializeButton(selectFile, invert)
    }

    private fun setTextAndActivateButtons(path: String) {
        mPath = path
        view_pdf!!.hide()
        mMorphButtonUtility?.setTextAndActivateButtons(path, selectFile, invert)
    }


    fun onItemClick(path: String, index: Int) {
        mSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        setTextAndActivateButtons(path)
        mPath = path
    }

    private fun viewPdfButton(path: String) {
        view_pdf!!.show()
        view_pdf!!.setOnClickListener { v: View? ->
            mFileUtils?.openFile(
                path,
                FileUtils.FileType.e_PDF
            )
        }
    }

    override fun onPDFCreationStarted() {
        mMaterialDialog =
            MaterialDialog.Builder(activity!!).customView(R.layout.lottie_anim_dialog, false)
                .build()
        mMaterialDialog?.show()
    }


    override fun onPDFCreated(success: Boolean, path: String?) {
        mMaterialDialog!!.dismiss()
        if (!success) {
            showSnackbar(R.string.snackbar_invert_unsuccessful)
            return
        }
        if (path.isNullOrEmpty()) return
        saveToHistory(path)
        getSnackbarwithAction(
            tableLayout,
            R.string.snackbar_pdfCreated
        ).setAction(R.string.snackbar_viewAction) { v ->
            mFileUtils?.openFile(
                path,
                FileUtils.FileType.e_PDF
            )
        }.show()
        viewPdfButton(path)
        resetValues()
    }

    private fun saveToHistory(finalOutput: String) {
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, OPERATION_INVERTED)
        (activity as HomeActivity).showAdIfLoad()
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
            invert -> InvertPdf(mPath!!, this).execute()
            btnPermission -> checkStoragePermission()
        }
    }
}