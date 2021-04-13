package com.extrastudios.docscanner.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.widget.LinearLayout
import com.extrastudios.docscanner.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.itextpdf.text.pdf.PdfReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.pow
import kotlin.math.sqrt

private const val COLOR_DIFF_THRESHOLD = 30.0


fun showBannerAd(mAdView: AdView, view: LinearLayout?, activity: Context) {
    if (view == null || BuildConfig.DEBUG || !BuildConfig.FREE_VERSION) {
        return
    }
    mAdView.adUnitId = ADMOB_BANNER_ID

    mAdView.adSize = AdSize.SMART_BANNER
    view.addView(mAdView)
    val builder = AdRequest.Builder()
    mAdView.loadAd(builder.build())
    mAdView.adListener = object : AdListener() {
        override fun onAdLoaded() {
            view.show()
        }

        override fun onAdFailedToLoad(errorCode: Int) {
            view.removeView(mAdView)
            view.hide()
        }
    }
}


fun colorSimilarCheck(color1: Int, color2: Int): Boolean {
    val colorDiff = sqrt(
        (Color.red(color1) - Color.red(color2)
            .toDouble()).pow(2.0) + (Color.green(color1) - Color.green(color2)
            .toDouble()).pow(2.0) + (Color.blue(color1) - Color.blue(color2).toDouble()).pow(2.0)
    )
    return colorDiff < COLOR_DIFF_THRESHOLD
}

fun isPDFEncrypted(file: String): Boolean {
    val reader: PdfReader
    val ownerPass = appName
    reader = try {
        PdfReader(file, ownerPass.toByteArray())
    } catch (e: IOException) {
        e.printStackTrace()
        return true
    }
    return reader.isEncrypted
}

fun saveImage(filename: String, finalBitmap: Bitmap?, root: String): String? {
    if (finalBitmap == null || checkIfBitmapIsWhite(finalBitmap)) return null
    val myDir = File(root + pdfDirectory)
    val fileName = "$filename.png"
    val file = File(myDir, fileName)
    if (file.exists()) file.delete()
    try {
        val out = FileOutputStream(file)
        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "$myDir/$fileName"
}

private fun checkIfBitmapIsWhite(bitmap: Bitmap?): Boolean {
    if (bitmap == null) return true
    val whiteBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    val canvas = Canvas(whiteBitmap)
    canvas.drawColor(Color.WHITE)
    return bitmap.sameAs(whiteBitmap)
}