package com.extrastudios.docscanner.utils

import android.app.Activity
import com.extrastudios.docscanner.R
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfImportedPage
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class PDFUtil(val mContext: Activity) {
    private val mFileUtils: FileUtils = FileUtils(mContext)

    fun addImagesToPdf(
        inputPath: String?,
        output: String,
        imagesUri: ArrayList<String>,
        onPdfCreated: (String, Int) -> Unit
    ): Boolean {
        return try {
            val reader = PdfReader(inputPath)
            val document = Document()
            val writer = PdfWriter.getInstance(document, FileOutputStream(output))
            document.open()
            initDoc(reader, document, writer)
            appendImages(document, imagesUri)
            document.close()
            mContext.getSnackbarwithAction(R.string.snackbar_pdfCreated).setAction(
                R.string.snackbar_viewAction,
                { v -> mFileUtils.openFile(output, FileUtils.FileType.e_PDF) }).show()
            onPdfCreated(output, OPERATION_CREATED)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            mContext.showSnackbar(R.string.remove_pages_error)
            false
        } catch (e: DocumentException) {
            e.printStackTrace()
            mContext.showSnackbar(R.string.remove_pages_error)
            false
        }
    }

    @Throws(DocumentException::class, IOException::class)
    private fun appendImages(document: Document, imagesUri: ArrayList<String>) {
        val documentRect = document.pageSize
        for (i in imagesUri.indices) {
            document.newPage()
            val image = Image.getInstance(imagesUri[i])
            image.border = 0
            val pageWidth = document.pageSize.width // - (mMarginLeft + mMarginRight);
            val pageHeight = document.pageSize.height // - (mMarginBottom + mMarginTop);
            image.scaleToFit(pageWidth, pageHeight)
            image.setAbsolutePosition(
                (documentRect.width - image.scaledWidth) / 2,
                (documentRect.height - image.scaledHeight) / 2
            )
            document.add(image)
        }
    }

    fun initDoc(reader: PdfReader, document: Document, writer: PdfWriter) {
        val numOfPages = reader.numberOfPages
        val cb = writer.directContent
        var importedPage: PdfImportedPage?
        for (page in 1..numOfPages) {
            importedPage = writer.getImportedPage(reader, page)
            document.newPage()
            cb.addTemplate(importedPage, 0f, 0f)
        }
    }
}