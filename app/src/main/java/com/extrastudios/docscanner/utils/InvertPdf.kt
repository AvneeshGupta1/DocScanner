package com.extrastudios.docscanner.utils

import android.os.AsyncTask
import android.os.ParcelFileDescriptor
import com.extrastudios.docscanner.interfaces.OnPDFCreatedInterface
import com.itextpdf.text.pdf.GrayColor
import com.itextpdf.text.pdf.PdfGState
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class InvertPdf(
    private var mPath: String,
    private val mOnPDFCreatedInterface: OnPDFCreatedInterface
) : AsyncTask<Void, Void, Void>() {
    private var mIsNewPDFCreated: Boolean? = null
    override fun onPreExecute() {
        super.onPreExecute()
        mOnPDFCreatedInterface.onPDFCreationStarted()
        mIsNewPDFCreated = false
    }

    override fun doInBackground(vararg voids: Void): Void? {
        var fileDescriptor: ParcelFileDescriptor? = null
        try {
            fileDescriptor =
                ParcelFileDescriptor.open(File(mPath), ParcelFileDescriptor.MODE_READ_ONLY)
            if (fileDescriptor != null) {
                val outputPath = mPath.replace(".pdf", "_inverted$pdfExtension")
                if (createPDF(mPath, outputPath)) {
                    mPath = outputPath
                    mIsNewPDFCreated = true
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

    private fun createPDF(mPath: String?, outputPath: String): Boolean {
        return try {
            val reader = PdfReader(mPath)
            val os: OutputStream = FileOutputStream(outputPath)
            val stamper = PdfStamper(reader, os)
            invert(stamper)
            stamper.close()
            os.close()
            true
        } catch (er: Exception) {
            er.printStackTrace()
            false
        }
    }

    private fun invert(stamper: PdfStamper) {
        for (i in stamper.reader.numberOfPages downTo 1) {
            invertPage(stamper, i)
        }
    }

    private fun invertPage(stamper: PdfStamper, page: Int) {
        val rect = stamper.reader.getPageSize(page)
        var cb = stamper.getOverContent(page)
        val gs = PdfGState()
        gs.setBlendMode(PdfGState.BM_DIFFERENCE)
        cb.setGState(gs)
        cb.setColorFill(GrayColor(1.0f))
        cb.rectangle(rect.left, rect.bottom, rect.width, rect.height)
        cb.fill()
        cb = stamper.getUnderContent(page)
        cb.setColorFill(GrayColor(1.0f))
        cb.rectangle(rect.left, rect.bottom, rect.width, rect.height)
        cb.fill()
    }

    override fun onPostExecute(avoid: Void?) {
        super.onPostExecute(avoid)
        mOnPDFCreatedInterface.onPDFCreated(mIsNewPDFCreated!!, mPath)
    }

}