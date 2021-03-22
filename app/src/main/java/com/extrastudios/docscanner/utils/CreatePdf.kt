package com.extrastudios.docscanner.utils

import android.graphics.Color
import android.os.AsyncTask
import com.extrastudios.docscanner.interfaces.OnPDFCreatedInterface
import com.extrastudios.docscanner.model.Watermark
import com.itextpdf.text.*
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.util.*

class CreatePdf(
    mImageToPDFOptions: ImageToPDFOptions,
    parentPath: String,
    onPDFCreated: OnPDFCreatedInterface
) : AsyncTask<String, String, String>() {
    private val mFileName = mImageToPDFOptions.outFileName
    private val mPassword: String? = mImageToPDFOptions.password
    private val mQualityString: Int = mImageToPDFOptions.imageCompression
    private val mImagesUri: ArrayList<String>? = mImageToPDFOptions.imagesUri
    private val mBorderWidth: Int
    private val mOnPDFCreatedInterface: OnPDFCreatedInterface = onPDFCreated
    private val mPasswordProtected: Boolean
    private val mWatermarkAdded: Boolean
    private val mWatermark: Watermark?
    private val mMarginTop: Int
    private val mMarginBottom: Int
    private val mMarginRight: Int
    private val mMarginLeft: Int
    private val mImageScaleType: String
    private val mPageNumStyle: String?
    private val mMasterPwd: String
    private val mPageColor: Int
    private var mPageSize: String? = null
    private var mSuccess = false
    private var mPath: String
    override fun onPreExecute() {
        super.onPreExecute()
        mSuccess = true
        mOnPDFCreatedInterface.onPDFCreationStarted()
    }

    private fun setFilePath() {
        val folder = File(mPath)
        if (!folder.exists()) folder.mkdir()
        mPath = mPath + mFileName + pdfExtension
    }

    protected override fun doInBackground(vararg params: String): String? {
        setFilePath()
        val pageSize = Rectangle(PageSize.getRectangle(mPageSize))
        pageSize.backgroundColor = getBaseColor(mPageColor)
        val document = Document(
            pageSize,
            mMarginLeft.toFloat(),
            mMarginRight.toFloat(),
            mMarginTop.toFloat(),
            mMarginBottom.toFloat()
        )
        document.setMargins(
            mMarginLeft.toFloat(),
            mMarginRight.toFloat(),
            mMarginTop.toFloat(),
            mMarginBottom.toFloat()
        )
        val documentRect = document.pageSize
        try {
            val writer = PdfWriter.getInstance(document, FileOutputStream(mPath))
            if (mPasswordProtected) {
                writer.setEncryption(
                    mPassword!!.toByteArray(),
                    mMasterPwd.toByteArray(),
                    PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY,
                    PdfWriter.ENCRYPTION_AES_128
                )
            }
            if (mWatermarkAdded) {
                val watermarkPageEvent = WatermarkPageEvent()
                watermarkPageEvent.watermark = mWatermark
                writer.pageEvent = watermarkPageEvent
            }
            document.open()
            for (i in mImagesUri!!.indices) {
                var quality: Int
                quality = 30
                if (mQualityString != 0) {
                    quality = mQualityString
                }
                val image = Image.getInstance(mImagesUri[i])
                // compressionLevel is a value between 0 (best speed) and 9 (best compression)
                val qualityMod = quality * 0.09
                image.compressionLevel = qualityMod.toInt()
                image.border = Rectangle.BOX
                image.borderWidth = mBorderWidth.toFloat()
                val pageWidth = document.pageSize.width - (mMarginLeft + mMarginRight)
                val pageHeight = document.pageSize.height - (mMarginBottom + mMarginTop)
                if (mImageScaleType == IMAGE_SCALE_TYPE_ASPECT_RATIO) image.scaleToFit(
                    pageWidth,
                    pageHeight
                ) else image.scaleAbsolute(pageWidth, pageHeight)
                image.setAbsolutePosition(
                    (documentRect.width - image.scaledWidth) / 2,
                    (documentRect.height - image.scaledHeight) / 2
                )
                addPageNumber(documentRect, writer)
                document.add(image)
                document.newPage()
            }
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
            mSuccess = false
        }
        return null
    }

    private fun addPageNumber(documentRect: Rectangle, writer: PdfWriter) {
        if (mPageNumStyle != null) {
            ColumnText.showTextAligned(
                writer.directContent,
                Element.ALIGN_BOTTOM,
                getPhrase(writer, mPageNumStyle, mImagesUri!!.size),
                (documentRect.right + documentRect.left) / 2,
                documentRect.bottom + 25,
                0f
            )
        }
    }

    private fun getPhrase(writer: PdfWriter, pageNumStyle: String, size: Int): Phrase {
        return when (pageNumStyle) {
            PG_NUM_STYLE_PAGE_X_OF_N -> Phrase(
                String.format(
                    "Page %d of %d",
                    writer.pageNumber,
                    size
                )
            )
            PG_NUM_STYLE_X_OF_N -> Phrase(String.format("%d of %d", writer.pageNumber, size))
            else -> Phrase(String.format("%d", writer.pageNumber))
        }
    }

    override fun onPostExecute(s: String?) {
        super.onPostExecute(s)
        mOnPDFCreatedInterface.onPDFCreated(mSuccess, mPath)
    }

    private fun getBaseColor(color: Int): BaseColor {
        return BaseColor(Color.red(color), Color.green(color), Color.blue(color))
    }

    init {
        if (mImageToPDFOptions.pageSize == "DEFAULT (A4)") {
            mPageSize = "A4"
        } else {
            mPageSize = mImageToPDFOptions.pageSize
        }
        mPasswordProtected = mImageToPDFOptions.isPasswordProtected
        mBorderWidth = mImageToPDFOptions.borderWidth
        mWatermarkAdded = mImageToPDFOptions.isWatermarkAdded
        mWatermark = mImageToPDFOptions.watermark
        mMarginTop = mImageToPDFOptions.marginTop
        mMarginBottom = mImageToPDFOptions.marginBottom
        mMarginRight = mImageToPDFOptions.marginRight
        mMarginLeft = mImageToPDFOptions.marginLeft
        mImageScaleType = mImageToPDFOptions.imageScaleType
        mPageNumStyle = mImageToPDFOptions.pageNumStyle
        mMasterPwd = mImageToPDFOptions.masterPwd
        mPageColor = mImageToPDFOptions.pageColor
        mPath = parentPath
    }
}