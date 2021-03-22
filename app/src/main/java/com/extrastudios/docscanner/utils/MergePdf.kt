package com.extrastudios.docscanner.utils

import android.os.AsyncTask
import com.extrastudios.docscanner.interfaces.MergeFilesListener
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream

class MergePdf(
    private var mFilename: String,
    private var mFinPath: String,
    private val mIsPasswordProtected: Boolean,
    private val mPassword: String?,
    private val mMergeFilesListener: MergeFilesListener,
    private val mMasterPwd: String
) : AsyncTask<String?, Void?, Void?>() {
    private var mIsPDFMerged: Boolean? = null
    override fun onPreExecute() {
        super.onPreExecute()
        mIsPDFMerged = false
        mMergeFilesListener.mergeStarted()
    }

    override fun doInBackground(vararg pdfpaths: String?): Void? {
        try {
            var pdfreader: PdfReader
            val document = Document()
            mFilename += pdfExtension
            mFinPath += mFilename
            val copy = PdfCopy(document, FileOutputStream(mFinPath))
            if (mIsPasswordProtected) {
                copy.setEncryption(
                    mPassword?.toByteArray(),
                    mMasterPwd.toByteArray(),
                    PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY,
                    PdfWriter.ENCRYPTION_AES_128
                )
            }
            document.open()
            var numOfPages: Int
            for (pdfPath in pdfpaths) {
                pdfreader = PdfReader(pdfPath)
                numOfPages = pdfreader.numberOfPages
                for (page in 1..numOfPages) copy.addPage(copy.getImportedPage(pdfreader, page))
            }
            mIsPDFMerged = true
            document.close()
        } catch (e: Exception) {
            mIsPDFMerged = false
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        mMergeFilesListener.resetValues(mIsPDFMerged!!, mFinPath)
    }


}