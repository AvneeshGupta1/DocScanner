package com.extrastudios.docscanner.fragment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils.isEmpty
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.adapter.BottomAdapter
import com.extrastudios.docscanner.interfaces.OnBackPressedInterface
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.utils.FileUtils.Companion.getFileName
import com.extrastudios.docscanner.viewmodel.AddRemovePasswordViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_extract_text.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import java.io.File
import java.io.FileWriter
import java.util.*

class ExtractTextFragment : BaseFragments(), OnBackPressedInterface, View.OnClickListener {
    private val mFileSelectCode = 0
    private var mExcelFileUri: Uri? = null
    private var mRealPath: String? = null
    private var mFileName: String? = null
    private var bottomAdapter: BottomAdapter? = null
    private val bottomList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_extract_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkStoragePermission()
        bottomRecyclerView
        mMorphButtonUtility?.morphToGrey(extract_text, mMorphButtonUtility!!.integer())
        extract_text!!.isEnabled = false
        layout_view_files.setOnClickListener(this)
        select_pdf_file.setOnClickListener(this)
        extract_text.setOnClickListener(this)
        btnPermission.setOnClickListener(this)
    }

    private fun selectPdfFile() {
        val uri = Uri.parse(Environment.getRootDirectory().toString() + "/")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setDataAndType(uri, "*/*")
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(
                Intent.createChooser(intent, R.string.select_file.toString()),
                mFileSelectCode
            )
        } catch (ex: ActivityNotFoundException) {
            showSnackbar(R.string.install_file_manager)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (data == null || resultCode != Activity.RESULT_OK || data.data == null) return

        var mRealPath = RealPathUtil.instance.getRealPath(activity!!, data.data!!)
        if (mRealPath.isNullOrEmpty()) {
            showSnackbar(R.string.file_access_error)
            return
        }
        if (File(mRealPath).isPasswordProtected()) {
            showSnackbar(R.string.encrypted_pdf)
            return
        }
        if (requestCode == mFileSelectCode) {
            mExcelFileUri = data.data
            mRealPath = RealPathUtil.instance.getRealPath(context!!, data.data!!)
            showSnackbar(R.string.snackbar_pdfselected)
            mFileName = getFileName(mRealPath)
            if (mFileName != null && !mFileName!!.endsWith(pdfExtension)) {
                showSnackbar(R.string.extension_not_supported)
                return
            }
            mFileName = getString(R.string.pdf_selected) + mFileName

            tv_extract_text_bottom.text = mFileName
            tv_extract_text_bottom.show()
            extract_text.isEnabled = true
            mMorphButtonUtility?.morphToSquare(extract_text, mMorphButtonUtility!!.integer())
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openExtractText() {
        MaterialDialog.Builder(activity!!).title(R.string.creating_txt)
            .content(R.string.enter_file_name).input(
                getString(R.string.example),
                null
            ) { dialog: MaterialDialog?, input: CharSequence ->
            if (isEmpty(input)) {
                showSnackbar(R.string.snackbar_name_not_blank)
            } else {
                val inputName = input.toString()
                if (!mFileUtils!!.isFileExist(inputName + textExtension)) {
                    extractTextFromPdf(inputName)
                } else {
                    val builder: MaterialDialog.Builder =
                        MaterialDialog.Builder(activity!!).title(R.string.warning)
                            .content(R.string.overwrite_message).positiveText(R.string.ok)
                            .negativeText(R.string.cancel)
                    builder.onPositive { dialog12: MaterialDialog?, which: DialogAction? ->
                        extractTextFromPdf(
                            inputName
                        )
                    }
                        .onNegative { dialog1: MaterialDialog?, which: DialogAction? -> openExtractText() }
                        .show()
                }
            }
        }.show()
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

    private fun extractTextFromPdf(inputName: String) {
        val mStorePath = preferencesService.storageLocation + pdfDirectory
        val mPath = mStorePath + inputName + textExtension
        try {
            val parsedText = StringBuilder()
            val reader = PdfReader(mRealPath)
            val n = reader.numberOfPages
            for (i in 0 until n) {
                parsedText.append(
                    PdfTextExtractor.getTextFromPage(reader, i + 1).trim { it <= ' ' }).append("\n")
            }
            reader.close()
            if (isEmpty(parsedText.toString().trim { it <= ' ' })) {
                showSnackbar(R.string.snack_bar_empty_txt_in_pdf)
                return
            }
            val textFile = File(mStorePath, inputName + textExtension)
            val writer = FileWriter(textFile)
            writer.append(parsedText.toString())
            writer.flush()
            writer.close()
            getSnackbarwithAction(
                buttonLayoutExtract,
                R.string.snackbar_txtExtracted
            ).setAction(R.string.snackbar_viewAction) { v ->
                mFileUtils?.openFile(
                    mPath,
                    FileUtils.FileType.e_TXT
                )
            }.show()
            tv_extract_text_bottom!!.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mMorphButtonUtility?.morphToGrey(extract_text, mMorphButtonUtility!!.integer())
            extract_text!!.isEnabled = false
            mRealPath = null
            mExcelFileUri = null
        }
    }

    fun onItemClick(path: String, index: Int) {
        mSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        mRealPath = path
        mFileName = getFileName(path)
        mFileName = resources.getString(R.string.pdf_selected) + mFileName
        tv_extract_text_bottom!!.text = mFileName
        tv_extract_text_bottom!!.visibility = View.VISIBLE
        extract_text!!.isEnabled = true
        mMorphButtonUtility?.morphToSquare(extract_text, mMorphButtonUtility!!.integer())
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
            layout_view_files -> showHideSheet(mSheetBehavior!!)
            select_pdf_file -> selectPdfFile()
            extract_text -> openExtractText()
            btnPermission -> checkStoragePermission()
        }
    }
}