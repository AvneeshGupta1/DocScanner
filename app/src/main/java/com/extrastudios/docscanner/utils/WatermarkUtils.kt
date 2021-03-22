package com.extrastudios.docscanner.utils

import android.app.Activity
import android.text.Editable
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.model.Watermark
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView
import com.itextpdf.text.*
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import java.io.FileOutputStream
import java.io.IOException

class WatermarkUtils(private val mContext: Activity) {
    private val mFileUtils: FileUtils = FileUtils(mContext)
    private var mWatermark: Watermark? = null

    fun setWatermark(path: String, DataSetChanged: (String, Int) -> Unit) {
        val mDialog = MaterialDialog.Builder(mContext)
            .title(com.extrastudios.docscanner.R.string.add_watermark)
            .customView(com.extrastudios.docscanner.R.layout.add_watermark_dialog, true)
            .positiveText(R.string.ok).negativeText(R.string.cancel).build()
        val mPositiveAction: View = mDialog.getActionButton(DialogAction.POSITIVE)
        mWatermark = Watermark()
        val watermarkTextInput =
            mDialog.customView!!.findViewById<EditText>(com.extrastudios.docscanner.R.id.watermarkText)
        val angleInput =
            mDialog.customView!!.findViewById<EditText>(com.extrastudios.docscanner.R.id.watermarkAngle)
        val colorPickerInput: ColorPickerView =
            mDialog.customView!!.findViewById(com.extrastudios.docscanner.R.id.watermarkColor)
        val fontSizeInput =
            mDialog.customView!!.findViewById<EditText>(com.extrastudios.docscanner.R.id.watermarkFontSize)
        val fontFamilyInput =
            mDialog.customView!!.findViewById<Spinner>(com.extrastudios.docscanner.R.id.watermarkFontFamily)
        val styleInput =
            mDialog.customView!!.findViewById<Spinner>(com.extrastudios.docscanner.R.id.watermarkStyle)
        fontFamilyInput.adapter = ArrayAdapter(
            mContext,
            android.R.layout.simple_spinner_dropdown_item,
            Font.FontFamily.values()
        )
        styleInput.adapter = ArrayAdapter(
            mContext,
            android.R.layout.simple_spinner_dropdown_item,
            mContext.resources.getStringArray(com.extrastudios.docscanner.R.array.fontStyles)
        )
        angleInput.setText("0")
        fontSizeInput.setText("50")
        watermarkTextInput.addTextChangedListener(object : DefaultTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mPositiveAction.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(input: Editable) {
                if (input.toString()
                        .isEmpty()
                ) mContext.showSnackbar(com.extrastudios.docscanner.R.string.snackbar_watermark_cannot_be_blank) else {
                    mWatermark!!.watermarkText = input.toString()
                }
            }
        })
        mPositiveAction.isEnabled = false
        mPositiveAction.setOnClickListener { v: View? ->
            try {
                mWatermark!!.watermarkText = watermarkTextInput.text.toString()
                mWatermark!!.fontFamily = fontFamilyInput.selectedItem as Font.FontFamily
                mWatermark!!.fontStyle = getStyleValueFromName(styleInput.selectedItem as String)
                mWatermark!!.rotationAngle = angleInput.text.toString().parseIntOrDefault()
                mWatermark!!.textSize = fontSizeInput.text.toString().parseIntOrDefault(50)

                //colorPickerInput.getColor() returns ans ARGB Color and BaseColor can use that ARGB as parameter
                mWatermark!!.textColor = BaseColor(colorPickerInput.color)
                val filePath = createWatermark(path)
                DataSetChanged(filePath, OPERATION_WATER_MARK)
                mContext.getSnackbarwithAction(com.extrastudios.docscanner.R.string.watermark_added)
                    .setAction(com.extrastudios.docscanner.R.string.snackbar_viewAction) { v1: View? ->
                        mFileUtils.openFile(
                            filePath,
                            FileUtils.FileType.e_PDF
                        )
                    }.show()
            } catch (e: IOException) {
                e.printStackTrace()
                mContext.showSnackbar(com.extrastudios.docscanner.R.string.cannot_add_watermark)
            } catch (e: DocumentException) {
                e.printStackTrace()
                mContext.showSnackbar(com.extrastudios.docscanner.R.string.cannot_add_watermark)
            }
            mDialog.dismiss()
        }
        mDialog.show()
    }

    @Throws(IOException::class, DocumentException::class)
    private fun createWatermark(path: String): String {
        val finalOutputFile = mFileUtils.getUniqueFileName(
            path.replace(
                pdfExtension,
                mContext.getString(R.string.watermarked_file)
            )
        )
        val reader = PdfReader(path)
        val stamper = PdfStamper(reader, FileOutputStream(finalOutputFile))
        val font = Font(
            mWatermark!!.fontFamily,
            mWatermark!!.textSize.toFloat(),
            mWatermark!!.fontStyle,
            mWatermark!!.textColor
        )
        val p = Phrase(mWatermark!!.watermarkText, font)
        var over: PdfContentByte?
        var pageSize: Rectangle
        var x: Float
        var y: Float
        val n = reader.numberOfPages
        for (i in 1..n) {
            pageSize = reader.getPageSizeWithRotation(i)
            x = (pageSize.left + pageSize.right) / 2
            y = (pageSize.top + pageSize.bottom) / 2
            over = stamper.getOverContent(i)
            ColumnText.showTextAligned(
                over,
                Element.ALIGN_CENTER,
                p,
                x,
                y,
                mWatermark!!.rotationAngle.toFloat()
            )
        }
        stamper.close()
        reader.close()
        return finalOutputFile
    }

    companion object {
        fun getStyleValueFromName(name: String?): Int {
            return when (name) {
                "BOLD" -> Font.BOLD
                "ITALIC" -> Font.ITALIC
                "UNDERLINE" -> Font.UNDERLINE
                "STRIKETHRU" -> Font.STRIKETHRU
                "BOLDITALIC" -> Font.BOLDITALIC
                else -> Font.NORMAL
            }
        }

        fun getStyleNameFromFont(font: Int): String {
            return when (font) {
                Font.BOLD -> "BOLD"
                Font.ITALIC -> "ITALIC"
                Font.UNDERLINE -> "UNDERLINE"
                Font.STRIKETHRU -> "STRIKETHRU"
                Font.BOLDITALIC -> "BOLDITALIC"
                else -> "NORMAL"
            }
        }
    }

}