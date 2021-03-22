package com.extrastudios.docscanner.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.AsyncTask
import com.extrastudios.docscanner.interfaces.OnPDFCompressedInterface
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.parser.PdfImageObject
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException

class CompressPdfAsync(
    private val inputPath: String,
    private val outputPath: String,
    private val quality: Int,
    private val mPDFCompressedInterface: OnPDFCompressedInterface
) : AsyncTask<String, String, String>() {

    var success = false
    override fun onPreExecute() {
        super.onPreExecute()
        mPDFCompressedInterface.pdfCompressionStarted()
    }

    protected override fun doInBackground(vararg strings: String): String? {
        success = try {
            val reader = PdfReader(inputPath)
            compressReader(reader)
            saveReader(reader)
            reader.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: DocumentException) {
            e.printStackTrace()
            false
        }
        return null
    }

    @Throws(IOException::class)
    private fun compressReader(reader: PdfReader) {
        val n = reader.xrefSize
        var `object`: PdfObject?
        var stream: PRStream
        for (i in 0 until n) {
            `object` = reader.getPdfObject(i)
            if (`object` == null || !`object`.isStream) continue
            stream = `object` as PRStream
            compressStream(stream)
        }
        reader.removeUnusedObjects()
    }

    @Throws(IOException::class)
    private fun compressStream(stream: PRStream) {
        val pdfSubType = stream[PdfName.SUBTYPE]
        println(stream.type())
        if (pdfSubType != null && pdfSubType.toString() == PdfName.IMAGE.toString()) {
            val image = PdfImageObject(stream)
            val imageBytes = image.imageAsBytes
            val bmp: Bitmap?
            bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            if (bmp == null) return
            val width = bmp.width
            val height = bmp.height
            val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val outCanvas = Canvas(outBitmap)
            outCanvas.drawBitmap(bmp, 0f, 0f, null)
            val imgBytes = ByteArrayOutputStream()
            outBitmap.compress(Bitmap.CompressFormat.JPEG, quality, imgBytes)
            stream.clear()
            stream.setData(imgBytes.toByteArray(), false, PRStream.BEST_COMPRESSION)
            stream.put(PdfName.TYPE, PdfName.XOBJECT)
            stream.put(PdfName.SUBTYPE, PdfName.IMAGE)
            stream.put(PdfName.FILTER, PdfName.DCTDECODE)
            stream.put(PdfName.WIDTH, PdfNumber(width))
            stream.put(PdfName.HEIGHT, PdfNumber(height))
            stream.put(PdfName.BITSPERCOMPONENT, PdfNumber(8))
            stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB)
        }
    }

    @Throws(DocumentException::class, IOException::class)
    private fun saveReader(reader: PdfReader) {
        val stamper = PdfStamper(reader, FileOutputStream(outputPath))
        stamper.setFullCompression()
        stamper.close()
    }

    override fun onPostExecute(s: String?) {
        super.onPostExecute(s)
        mPDFCompressedInterface.pdfCompressionEnded(outputPath, success)
    }

}