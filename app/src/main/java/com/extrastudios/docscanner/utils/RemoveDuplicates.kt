package com.extrastudios.docscanner.utils

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.AsyncTask
import android.os.ParcelFileDescriptor
import com.extrastudios.docscanner.interfaces.OnPDFCreatedInterface
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class RemoveDuplicates(private var mPath: String?, onPDFCreatedInterface: OnPDFCreatedInterface) :
    AsyncTask<Void, Void, Void>() {
    private val mOnPDFCreatedInterface: OnPDFCreatedInterface = onPDFCreatedInterface
    private val mBitmaps: ArrayList<Bitmap> = ArrayList()
    private val mSequence: StringBuilder = StringBuilder()
    private var mIsNewPDFCreated: Boolean? = null
    override fun onPreExecute() {
        super.onPreExecute()
        mOnPDFCreatedInterface.onPDFCreationStarted()
        mIsNewPDFCreated = false
    }

    protected override fun doInBackground(vararg voids: Void): Void? {
        var fileDescriptor: ParcelFileDescriptor? = null
        try {
            if (mPath != null) // resolve pdf file path based on relative path
                fileDescriptor =
                    ParcelFileDescriptor.open(File(mPath), ParcelFileDescriptor.MODE_READ_ONLY)
            if (fileDescriptor != null) {
                val renderer = PdfRenderer(fileDescriptor)
                val pageCount = renderer.pageCount
                for (i in 0 until pageCount) {
                    val page = renderer.openPage(i)
                    // generate bitmaps for individual pdf pages
                    val currentBitmap =
                        Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    // say we render for showing on the screen
                    page.render(currentBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    // close the page
                    page.close()

                    //Adding bitmap to arrayList if not same
                    checkAndAddIfBitmapExists(currentBitmap, i)
                }

                // close the renderer
                renderer.close()
                if (mBitmaps.size == pageCount) {
                    //No repetition found
                    return null
                } else {
                    val mPages = mSequence.toString()
                    val outputPath = mPath!!.replace(".pdf", "_edited_$mPages.pdf")
                    if (createPDF(mPath, outputPath, mPages)) {
                        mPath = outputPath
                        mIsNewPDFCreated = true
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            mIsNewPDFCreated = false
        } catch (e: SecurityException) {
            e.printStackTrace()
            mIsNewPDFCreated = false
        }
        return null
    }

    private fun checkAndAddIfBitmapExists(bitmap: Bitmap, position: Int) {
        var add = true
        for (b in mBitmaps) {
            if (b.sameAs(bitmap)) add = false
        }
        if (add) {
            mBitmaps.add(bitmap)
            mSequence.append(position).append(",")
        }
    }

    override fun onPostExecute(avoid: Void?) {
        // execution of result of Long time consuming operation
        super.onPostExecute(avoid)
        mOnPDFCreatedInterface.onPDFCreated(mIsNewPDFCreated!!, mPath)
    }

    private fun createPDF(inputPath: String?, output: String, pages: String): Boolean {
        return try {
            val reader = PdfReader(inputPath)
            reader.selectPages(pages)
            val pdfStamper = PdfStamper(reader, FileOutputStream(output))
            pdfStamper.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: DocumentException) {
            e.printStackTrace()
            false
        }
    }

}