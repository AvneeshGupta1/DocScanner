package com.extrastudios.docscanner.utils

import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.extrastudios.docscanner.interfaces.ExtractImagesListener
import com.extrastudios.docscanner.utils.FileUtils.Companion.getFileNameWithoutExtension
import com.itextpdf.text.pdf.PRStream
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfImageObject
import java.io.IOException
import java.util.*

class ExtractImages(
    private val mPath: String,
    private val mExtractImagesListener: ExtractImagesListener,
    val root: String
) : AsyncTask<Void, Void, Void>() {
    private var mImagesCount = 0
    private var mOutputFilePaths: ArrayList<String>
    override fun onPreExecute() {
        super.onPreExecute()
        mExtractImagesListener.extractionStarted()
    }

    protected override fun doInBackground(vararg voids: Void): Void? {
        mOutputFilePaths = ArrayList()
        mImagesCount = 0
        try {
            val reader = PdfReader(mPath)
            var obj: PdfObject?
            for (i in 1..reader.xrefSize) {
                obj = reader.getPdfObject(i)
                if (obj != null && obj.isStream) {
                    val stream = obj as PRStream
                    val type = stream[PdfName.SUBTYPE] //get the object type
                    if (type != null && type.toString() == PdfName.IMAGE.toString()) {
                        val pio = PdfImageObject(stream)
                        val image = pio.imageAsBytes
                        val bmp = BitmapFactory.decodeByteArray(image, 0, image.size)
                        val filename: String =
                            getFileNameWithoutExtension(mPath).toString() + "_" + (mImagesCount + 1)
                        val path: String? = saveImage(filename, bmp, root)
                        if (path != null) {
                            mOutputFilePaths.add(path)
                            mImagesCount++
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        mExtractImagesListener.updateView(mImagesCount, mOutputFilePaths)
    }

    init {
        mOutputFilePaths = ArrayList()
    }
}