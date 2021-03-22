package com.extrastudios.docscanner.utils

import android.app.Activity
import android.util.SparseIntArray
import android.view.View
import android.widget.RadioGroup
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.itextpdf.text.pdf.*
import java.io.FileOutputStream

class PDFRotationUtils(private val mContext: Activity) {
    private val mAngleRadioButton: SparseIntArray = SparseIntArray()
    private val mFileUtils: FileUtils = FileUtils(mContext)

    fun rotatePages(sourceFilePath: String?, dataSetChanged: (String, Int) -> Unit) {
        val builder =
            MaterialDialog.Builder(mContext).title(R.string.rotate_pages).positiveText(R.string.ok)
                .negativeText(R.string.cancel)
        builder.customView(R.layout.dialog_rotate_pdf, true)
            .onPositive { dialog: MaterialDialog, which: DialogAction? ->
                val angleInput = dialog.customView!!.findViewById<RadioGroup>(R.id.rotation_angle)
                val angle = mAngleRadioButton[angleInput.checkedRadioButtonId]
                var destFilePath = FileUtils.getFileDirectoryPath(sourceFilePath!!)
                val fileName = FileUtils.getFileName(sourceFilePath)
                destFilePath += String.format(
                    mContext.getString(R.string.rotated_file_name),
                    fileName!!.substring(0, fileName.lastIndexOf('.')),
                    Integer.toString(angle),
                    pdfExtension
                )
                val result = rotatePDFPages(angle, sourceFilePath, destFilePath)
                if (result) {
                    dataSetChanged(destFilePath, OPERATION_ROTATED)
                }
            }.show()
    }

    private fun rotatePDFPages(angle: Int, sourceFilePath: String?, destFilePath: String): Boolean {
        try {
            val reader = PdfReader(sourceFilePath)
            val n = reader.numberOfPages
            var page: PdfDictionary
            var rotate: PdfNumber?
            for (p in 1..n) {
                page = reader.getPageN(p)
                rotate = page.getAsNumber(PdfName.ROTATE)
                if (rotate == null) page.put(
                    PdfName.ROTATE,
                    PdfNumber(angle)
                ) else page.put(PdfName.ROTATE, PdfNumber((rotate.intValue() + angle) % 360))
            }
            val stamper = PdfStamper(reader, FileOutputStream(destFilePath))
            stamper.close()
            reader.close()
            mContext.getSnackbarwithAction(R.string.snackbar_pdfCreated)
                .setAction(R.string.snackbar_viewAction) { v: View? ->
                    mFileUtils.openFile(
                        destFilePath,
                        FileUtils.FileType.e_PDF
                    )
                }.show()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            mContext.showSnackbar(R.string.encrypted_pdf)
        }
        return false
    }

    init {
        mAngleRadioButton.put(R.id.deg90, 90)
        mAngleRadioButton.put(R.id.deg180, 180)
        mAngleRadioButton.put(R.id.deg270, 270)
    }
}