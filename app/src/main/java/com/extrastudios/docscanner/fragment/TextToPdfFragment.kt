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
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.activity.HomeActivity
import com.extrastudios.docscanner.adapter.HomeAdapter
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.extrastudios.docscanner.viewmodel.TextToPdfViewModel
import kotlinx.android.synthetic.main.fragment_text_to_pdf.*
import kotlinx.android.synthetic.main.storage_permission_view.*
import javax.inject.Inject

class TextToPdfFragment : BaseFragments(), View.OnClickListener {

    @Inject
    lateinit var textToPDFOptions: TextToPDFOptions

    private val excelToPdfOptions = ArrayList<CommonItem>()
    private var homeAdapter: HomeAdapter? = null
    private var mPath: String? = null
    private val mFileSelectCode = 34
    private var mTextFileUri: Uri? = null
    private var mFileExtension: String? = null
    private var mFileNameWithType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_text_to_pdf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity?.application as DocScannerApplication).getAppComponent().inject(this)
        select_images.setOnClickListener(this)
        mMorphButtonUtility?.morphToGrey(pdfCreate, mMorphButtonUtility!!.integer())
        pdfCreate.isEnabled = false
        pdfCreate.setOnClickListener(this)
        pdfOpen.setOnClickListener(this)
        textToPdfRecyclerView
        btnPermission.setOnClickListener(this)
        checkStoragePermission()
    }

    private val textToPdfRecyclerView by lazy {
        enhancement_options_recycle_view.itemAnimator = DefaultItemAnimator()
        enhancement_options_recycle_view.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(activity!!, 2)
        enhancement_options_recycle_view.layoutManager = layoutManager
        homeAdapter = HomeAdapter(excelToPdfOptions) { item -> onItemClick(item) }
        enhancement_options_recycle_view.adapter = homeAdapter
    }

    private fun loadTextToPdfOptions() {
        val excelToPDFViewModel = ViewModelProvider(this).get(TextToPdfViewModel::class.java)
        excelToPDFViewModel.getTextToPdfItems(activity!!, textToPDFOptions).observe(this, Observer {
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
            FONT_COLOR -> {
                showColorChangeDialog(textToPDFOptions)
            }
            FONT_FAMILY -> {
                showFontFamilyDialog(textToPDFOptions) { loadTextToPdfOptions() }
            }
            FONT_SIZE -> {
                showFontSizeDialog(textToPDFOptions) { loadTextToPdfOptions() }
            }
            PAGE_COLOR -> {
                showPageColorDialog(textToPDFOptions)
            }
            PAGE_SIZE -> {
                showPageSizeDialog(textToPDFOptions)
            }
            TEXT_TO_PDF_SET_PASSWORD -> {
                showSetPasswordDialog(textToPDFOptions) { loadTextToPdfOptions() }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == mFileSelectCode) {
            try {
                mTextFileUri = data!!.data
                showSnackbar(R.string.text_file_selected)
                val fileName = mFileUtils!!.getFileName(mTextFileUri!!)
                if (fileName != null) {
                    mFileExtension =
                        if (fileName.endsWith(textExtension)) textExtension else if (fileName.endsWith(
                                docxExtension
                            )
                        ) docxExtension else if (fileName.endsWith(docExtension)) docExtension else {
                            showSnackbar(R.string.extension_not_supported)
                            return
                        }
                }
                mFileNameWithType =
                    mFileUtils!!.stripExtension(fileName) + getString(R.string.pdf_suffix)
                pdfCreate.isEnabled = true
                pdfOpen.hide()
                mPath = null
                pdfCreate.show()
                pdfCreate.unblockTouch()
                mMorphButtonUtility?.morphToSquare(pdfCreate, mMorphButtonUtility!!.integer())
            } catch (e: Exception) {
                showSnackbar(R.string.error_occurred)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun resultToTextPdf(uri: Uri) {
        openSaveDialog(null) { filename -> createPdf(filename, uri) }
    }

    private fun createPdf(mFilename: String, uri: Uri) {
        try {
            textToPDFOptions.outFileName = mFilename
            textToPDFOptions.inFileUri = uri
            val fileUtil = TextToPDFUtils(activity!!)
            val finalOutput = fileUtil.createPdfFromTextFile(textToPDFOptions, textExtension)
            saveToHistory(finalOutput)
            mPath = finalOutput
            mMorphButtonUtility!!.morphToGrey(pdfCreate, mMorphButtonUtility!!.integer())
            pdfCreate.isEnabled = false
            pdfCreate.hide()
            pdfOpen.show()
            mTextFileUri = null
            getSnackbarwithAction(
                enhancement_options_recycle_view,
                R.string.snackbar_pdfCreated
            ).setAction(R.string.snackbar_viewAction) { v ->
                mFileUtils?.openFile(
                    finalOutput,
                    FileUtils.FileType.e_PDF
                )
            }.show()
            textToPDFOptions.reset()
            loadTextToPdfOptions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveToHistory(finalOutput: String) {
        val qrBarCodeViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrBarCodeViewModel.saveHistory(finalOutput, OPERATION_CREATED)
        (activity as HomeActivity).showAdIfLoad()
    }

    override fun onStoragePermissionAllow() {
        layoutPermission.hide()
        loadTextToPdfOptions()
    }

    override fun onCameraPermissionAllow() {

    }

    override fun onPermissionCancel() {
        layoutPermission.show()
    }

    override fun onClick(v: View?) {
        when (v) {
            select_images -> {
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
                        ), mFileSelectCode
                    )
                } catch (ex: ActivityNotFoundException) {
                    showSnackbar(R.string.install_file_manager)
                }
            }
            pdfCreate -> {
                mTextFileUri?.let {
                    resultToTextPdf(it)
                }
            }
            btnPermission -> checkStoragePermission()
            pdfOpen -> {
                if (!mPath.isNullOrEmpty()) mFileUtils!!.openFile(mPath, FileUtils.FileType.e_PDF)
            }
        }
    }
}