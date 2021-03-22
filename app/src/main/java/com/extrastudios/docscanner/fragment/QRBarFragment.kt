package com.extrastudios.docscanner.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.extrastudios.docscanner.activity.QRBarCodeScanActivity
import com.extrastudios.docscanner.adapter.HomeAdapter
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.utils.*
import com.extrastudios.docscanner.viewmodel.QrBarCodeViewModel
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.itextpdf.text.DocumentException
import kotlinx.android.synthetic.main.fragment_qr.*
import kotlinx.android.synthetic.main.storage_camera_permission_view.*
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter
import javax.inject.Inject

class QRBarFragment : BaseFragments(), View.OnClickListener {

    @Inject
    lateinit var textToPdfOption: TextToPDFOptions

    private val barCodeValue: Int = 78
    private val homeItemList = ArrayList<CommonItem>()
    private var homeAdapter: HomeAdapter? = null
    private val mTempFileName = "scan_result_temp.txt"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity?.application as DocScannerApplication).getAppComponent().inject(this)
        btnPermission.setOnClickListener(this)
        checkStorageAndCameraPermission()
        barCodeRecyclerView
    }

    private val barCodeRecyclerView by lazy {
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(activity, 2)
        recyclerView.layoutManager = layoutManager
        homeAdapter = HomeAdapter(homeItemList) { item -> onItemClick(item) }
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                homeAdapter?.let {
                    if (it.isHeader(position)) return 2
                    return 1
                }
                return 1
            }
        }
        recyclerView.adapter = homeAdapter
        loadQrOptionData()
    }

    private fun loadQrOptionData() {
        val qrViewModel = ViewModelProvider(this).get(QrBarCodeViewModel::class.java)
        qrViewModel.getQrCodeItems(activity!!).observe(this, Observer {
            homeItemList.clear()
            homeItemList.addAll(it)
            homeAdapter?.notifyDataSetChanged()
        })
    }

    private fun onItemClick(type: Int) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        when (type) {
            SCAN_QR_CODE -> {
                val barcodeDetector = BarcodeDetector.Builder(activity!!).build()
                if (barcodeDetector.isOperational) {
                    val intent = Intent(activity, QRBarCodeScanActivity::class.java)
                    intent.putExtra(TITLE, getString(R.string.scan_qrcode))
                    startActivityForResult(intent, barCodeValue)
                }
            }
            SCAN_BAR_CODE -> {
                val barcodeDetector = BarcodeDetector.Builder(activity!!).build()
                if (barcodeDetector.isOperational) {
                    val intent = Intent(activity, QRBarCodeScanActivity::class.java)
                    intent.putExtra(TITLE, getString(R.string.scan_barcode))
                    startActivityForResult(intent, barCodeValue)
                }
            }
        }
        requireActivity().overridePendingTransition(R.anim.enter, R.anim.exit)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == barCodeValue) {
            val scanContent = data?.extras?.getString(BAR_CODE_VALUE) ?: ""
            toast(scanContent)
            val mTempFile = File(activity!!.cacheDir.path + "/" + mTempFileName)
            val mWriter: PrintWriter
            try {
                mWriter = PrintWriter(mTempFile)
                mWriter.print("")
                mWriter.append(scanContent)
                mWriter.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            val uri = Uri.fromFile(mTempFile)
            resultToTextPdf(uri)
        }
    }

    private fun resultToTextPdf(uri: Uri) {
        openSaveDialog(null) { filename -> createPdf(filename, uri) }
    }

    private fun createPdf(mFilename: String, uri: Uri) {
        try {
            val fileUtil = TextToPDFUtils(activity!!)
            textToPdfOption.outFileName = mFilename
            textToPdfOption.inFileUri = uri

            val finalOutput = fileUtil.createPdfFromTextFile(textToPdfOption, textExtension)
            saveToHistory(finalOutput)
            getSnackbarwithAction(
                recyclerView,
                R.string.snackbar_pdfCreated
            ).setAction(R.string.snackbar_viewAction) { v ->
                mFileUtils?.openFile(
                    finalOutput,
                    FileUtils.FileType.e_PDF
                )
            }.show()
        } catch (e: DocumentException) {
            e.printStackTrace()
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
    }

    override fun onCameraPermissionAllow() {

    }

    override fun onPermissionCancel() {
        layoutPermission.show()
    }

    override fun onClick(v: View?) {
        when (v) {
            btnPermission -> checkStorageAndCameraPermission()
        }
    }
}