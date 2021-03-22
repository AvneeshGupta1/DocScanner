package com.extrastudios.docscanner.activity

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.utils.BAR_CODE_VALUE
import com.extrastudios.docscanner.utils.TITLE
import com.extrastudios.docscanner.utils.vibrate
import com.google.android.gms.vision.barcode.Barcode
import com.scan.barcode.BarcodeReader

class QRBarCodeScanActivity : BaseActivity(), BarcodeReader.BarcodeReaderListener {
    private var barcodeReader: BarcodeReader? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra(TITLE) ?: ""
        barcodeReader =
            supportFragmentManager.findFragmentById(R.id.barcode_fragment) as BarcodeReader?
        barcodeReader?.setTitle(title)
    }

    override fun onBitmapScanned(sparseArray: SparseArray<Barcode>?) {

    }

    override fun onScannedMultiple(barcodes: MutableList<Barcode>?) {

    }

    override fun onCameraPermissionDenied() {

    }

    override fun onScanned(barcode: Barcode) {
        if (preferencesService.isSoundEnable) barcodeReader?.playBeep()
        if (preferencesService.isVibrateEnable) vibrate()
        val barCodeValue = barcode.rawValue ?: ""
        val returnIntent = Intent()
        returnIntent.putExtra(BAR_CODE_VALUE, barCodeValue)
        setResult(RESULT_OK, returnIntent)
        overridePendingTransition(R.anim.enter, R.anim.exit)
        finish()
    }

    override fun onScanError(errorMessage: String?) {

    }

    override fun getLayout(): Int {
        return R.layout.activity_qr_bar_scan
    }
}