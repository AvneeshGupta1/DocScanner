package com.extrastudios.docscanner.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.AsyncTask
import android.os.ParcelFileDescriptor
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.interfaces.ExtractImagesListener
import com.extrastudios.docscanner.utils.FileUtils.Companion.getFileNameWithoutExtension
import java.io.File
import java.io.IOException
import java.util.*

class PdfToImages(
    val context: Activity,
    private val mPassword: String?,
    private val mPath: String?,
    private val mUri: Uri?,
    private val mExtractImagesListener: ExtractImagesListener,
    val preferencesService: PreferencesService
) : AsyncTask<Void, Void, Void>() {
    private var mImagesCount = 0
    private var mOutputFilePaths = ArrayList<String>()
    private var mPDFEncryptionUtility: PDFEncryptionUtility? = null
    private var mDecryptedPath: String? = null
    override fun onPreExecute() {
        super.onPreExecute()
        mPDFEncryptionUtility = PDFEncryptionUtility(context, preferencesService)
        mExtractImagesListener.extractionStarted()
    }

    protected override fun doInBackground(vararg voids: Void): Void? {
        if (!mPassword.isNullOrEmpty()) {
            mDecryptedPath = mPDFEncryptionUtility!!.removeDefPasswordForImages(mPath!!, mPassword)
        }
        mOutputFilePaths = ArrayList()
        mImagesCount = 0

        // Render pdf pages as bitmap
        var fileDescriptor: ParcelFileDescriptor? = null
        try {
            if (mDecryptedPath != null) fileDescriptor = ParcelFileDescriptor.open(
                File(mDecryptedPath),
                ParcelFileDescriptor.MODE_READ_ONLY
            ) else {
                if (mUri != null) {
                    // resolve pdf file path based on uri
                    fileDescriptor = context.contentResolver.openFileDescriptor(mUri, "r")
                } else if (mPath != null) {
                    // resolve pdf file path based on relative path
                    fileDescriptor =
                        ParcelFileDescriptor.open(File(mPath), ParcelFileDescriptor.MODE_READ_ONLY)
                }
            }
            if (fileDescriptor != null) {
                val renderer = PdfRenderer(fileDescriptor)
                val pageCount = renderer.pageCount
                for (i in 0 until pageCount) {
                    val page = renderer.openPage(i)
                    // generate bitmaps for individual pdf pages
                    val bitmap =
                        Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                    // say we render for showing on the screen
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    // close the page
                    page.close()

                    // generate numbered image file names
                    val filename: String =
                        getFileNameWithoutExtension(mPath).toString() + "_" + (i + 1)
                    val path = saveImage(filename, bitmap, preferencesService.storageLocation)
                    if (path != null) {
                        mOutputFilePaths.add(path)
                        mImagesCount++
                    }
                }

                // close the renderer
                renderer.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        mExtractImagesListener.updateView(mImagesCount, mOutputFilePaths)
        if (mDecryptedPath != null) File(mDecryptedPath).delete()
    }
}