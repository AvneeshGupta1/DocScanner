package com.extrastudios.docscanner.utils

import android.content.Context
import android.net.Uri
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class TextFileReader(context: Context?) : FileReader(context!!) {
    @Throws(Exception::class)
    override fun createDocumentFromStream(
        uri: Uri?,
        document: Document?,
        myfont: Font?,
        inputStream: InputStream?
    ) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String
        while (reader.readLine().also { line = it } != null) {
            line += "\n"
            val para = Paragraph(line, myfont)
            para.alignment = Element.ALIGN_JUSTIFIED
            document!!.add(para)
        }
        reader.close()
    }
}

class DocFileReader(context: Context?) : FileReader(context!!) {
    @Throws(Exception::class)
    override fun createDocumentFromStream(
        uri: Uri?,
        document: Document?,
        myfont: Font?,
        inputStream: InputStream?
    ) {
        val doc = HWPFDocument(inputStream)
        val extractor = WordExtractor(doc)
        val fileData = extractor.text + "\n"
        val documentParagraph = Paragraph(fileData, myfont)
        documentParagraph.alignment = Element.ALIGN_JUSTIFIED
        document!!.add(documentParagraph)
    }
}

class DocxFileReader(context: Context?) : FileReader(context!!) {
    @Throws(Exception::class)
    override fun createDocumentFromStream(
        uri: Uri?,
        document: Document?,
        myfont: Font?,
        inputStream: InputStream?
    ) {
        val doc = XWPFDocument(inputStream)
        val extractor = XWPFWordExtractor(doc)
        val fileData = extractor.text + "\n"
        val documentParagraph = Paragraph(fileData, myfont)
        documentParagraph.alignment = Element.ALIGN_JUSTIFIED
        document!!.add(documentParagraph)
    }
}

abstract class FileReader(var mContext: Context) {
    fun read(uri: Uri?, document: Document?, myfont: Font?) {
        try {
            val inputStream: InputStream = mContext.contentResolver.openInputStream(uri!!) ?: return
            createDocumentFromStream(uri, document, myfont, inputStream)
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    protected abstract fun createDocumentFromStream(
        uri: Uri?,
        document: Document?,
        myfont: Font?,
        inputStream: InputStream?
    )

}