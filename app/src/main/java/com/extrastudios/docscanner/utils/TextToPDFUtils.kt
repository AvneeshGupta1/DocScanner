package com.extrastudios.docscanner.utils

import android.app.Activity
import android.graphics.Color
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.io.IOException

class TextToPDFUtils(private val mContext: Activity) {
    private val mTextFileReader: TextFileReader = TextFileReader(mContext)
    private val mDocFileReader: DocFileReader = DocFileReader(mContext)
    private val mDocxFileReader: DocxFileReader = DocxFileReader(mContext)

    @Throws(DocumentException::class, IOException::class)
    fun createPdfFromTextFile(mTextToPDFOptions: TextToPDFOptions, fileExtension: String?): String {

        if (mTextToPDFOptions.pageSize == "DEFAULT (A4)") {
            mTextToPDFOptions.pageSize = "A4"
        }

        val masterpwd = mTextToPDFOptions.preferencesService.masterPassword
        val pageSize = Rectangle(PageSize.getRectangle(mTextToPDFOptions.pageSize))
        pageSize.backgroundColor = getBaseColor(mTextToPDFOptions.pageColor)
        val document = Document(pageSize)
        val finalOutput =
            mTextToPDFOptions.preferencesService.storageLocation + pdfDirectory + mTextToPDFOptions.outFileName + ".pdf"
        val writer = PdfWriter.getInstance(document, FileOutputStream(finalOutput))
        writer.setPdfVersion(PdfWriter.VERSION_1_7)
        if (mTextToPDFOptions.isPasswordProtected) {
            writer.setEncryption(
                mTextToPDFOptions.password!!.toByteArray(),
                masterpwd.toByteArray(),
                PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY,
                PdfWriter.ENCRYPTION_AES_128
            )
        }
        document.open()
        val myfont = Font(mTextToPDFOptions.fontFamily)
        myfont.style = Font.NORMAL
        myfont.size = mTextToPDFOptions.fontSize.toFloat()
        myfont.color = getBaseColor(mTextToPDFOptions.fontColor)
        document.add(Paragraph("\n"))
        addContentToDocument(mTextToPDFOptions, fileExtension, document, myfont)
        document.close()
        return finalOutput
    }

    @Throws(DocumentException::class)
    private fun addContentToDocument(
        mTextToPDFOptions: TextToPDFOptions,
        fileExtension: String?,
        document: Document,
        myfont: Font
    ) {
        if (fileExtension == null) throw DocumentException()
        when (fileExtension) {
            docExtension -> mDocFileReader.read(mTextToPDFOptions.inFileUri, document, myfont)
            docxExtension -> mDocxFileReader.read(mTextToPDFOptions.inFileUri, document, myfont)
            else -> mTextFileReader.read(mTextToPDFOptions.inFileUri, document, myfont)
        }
    }

    private fun getBaseColor(color: Int): BaseColor {
        return BaseColor(Color.red(color), Color.green(color), Color.blue(color))
    }

}