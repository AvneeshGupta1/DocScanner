package com.extrastudios.docscanner.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.model.PDFFile
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_home.*
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class PDFEncryptionUtility(
    private val mContext: Activity,
    private val preferencesService: PreferencesService
) {
    private val mFileUtils: FileUtils = FileUtils(mContext)
    private val mDialog: MaterialDialog =
        MaterialDialog.Builder(mContext).customView(R.layout.custom_dialog, true)
            .positiveText(android.R.string.ok).negativeText(android.R.string.cancel).build()
    private var mPassword: String? = null


    fun setPassword(filePath: String, dataSetChanged: (String, Int) -> Unit) {
        mDialog.setTitle(R.string.set_password)
        val mPositiveAction: View = mDialog.getActionButton(DialogAction.POSITIVE)
        assert(mDialog.customView != null)
        val mPasswordInput = mDialog.customView!!.findViewById<EditText>(R.id.password)
        mPasswordInput.addTextChangedListener(object : DefaultTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mPositiveAction.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(input: Editable) {
                if (input.toString()
                        .isEmpty()
                ) mContext.showSnackbar(R.string.snackbar_password_cannot_be_blank) else mPassword =
                    input.toString()
            }
        })
        mDialog.show()
        mPositiveAction.isEnabled = false
        mPositiveAction.setOnClickListener { v: View? ->
            try {
                val path = doEncryption(filePath, mPassword)
                mContext.getSnackbarwithAction(R.string.snackbar_pdfCreated)
                    .setAction(R.string.snackbar_viewAction) { v2: View? ->
                        mFileUtils.openFile(
                            path,
                            FileUtils.FileType.e_PDF
                        )
                    }.show()
                dataSetChanged(path, OPERATION_ENCRYPTED)
            } catch (e: IOException) {
                e.printStackTrace()
                mContext.showSnackbar(R.string.cannot_add_password)
            } catch (e: DocumentException) {
                e.printStackTrace()
                mContext.showSnackbar(R.string.cannot_add_password)
            }
            mDialog.dismiss()
        }
    }


    @Throws(IOException::class, DocumentException::class)
    private fun doEncryption(path: String, password: String?): String {
        val masterpwd = preferencesService.masterPassword
        val finalOutputFile = mFileUtils.getUniqueFileName(
            path.replace(
                pdfExtension,
                mContext.getString(R.string.encrypted_file)
            )
        )
        val reader = PdfReader(path)
        val stamper = PdfStamper(reader, FileOutputStream(finalOutputFile))
        stamper.setEncryption(
            password!!.toByteArray(),
            masterpwd.toByteArray(),
            PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY,
            PdfWriter.ENCRYPTION_AES_128
        )
        stamper.close()
        reader.close()
        return finalOutputFile
    }


    private fun isPDFEncrypted(file: String): Boolean {
        val reader: PdfReader
        val ownerPass = mContext.getString(R.string.app_name)
        reader = try {
            PdfReader(file, ownerPass.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
            return true
        }
        //Check if PDF is encrypted or not.
        if (!reader.isEncrypted) {
            mContext.showSnackbar(R.string.not_encrypted)
            return false
        }
        return true
    }


    fun removePassword(file: String, dataSetChanged: (String, Int) -> Unit) {
        if (!isPDFEncrypted(file)) return
        val input_password = arrayOfNulls<String>(1)
        mDialog.setTitle(R.string.enter_password)
        val mPositiveAction: View = mDialog.getActionButton(DialogAction.POSITIVE)
        val mPasswordInput =
            Objects.requireNonNull(mDialog.customView)?.findViewById<EditText>(R.id.password)
        val text = mDialog.customView!!.findViewById<TextView>(R.id.enter_password)
        text.setText(R.string.decrypt_message)
        mPasswordInput?.addTextChangedListener(object : DefaultTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mPositiveAction.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(input: Editable) {
                input_password[0] = input.toString()
            }
        })
        mDialog.show()
        mPositiveAction.isEnabled = false
        mPositiveAction.setOnClickListener { v: View? ->

            if (!removePasswordUsingDefMasterPassword(file, dataSetChanged, input_password)) {
                if (!removePasswordUsingInputMasterPassword(file, dataSetChanged, input_password)) {
                    mContext.showSnackbar(R.string.master_password_changed)
                }
            }
            mDialog.dismiss()
        }
    }


    fun removeDefPasswordForImages(file: String, inputPassword: String): String? {
        val finalOutputFile: String
        try {
            val masterPwd = preferencesService.masterPassword
            val reader = PdfReader(file, masterPwd.toByteArray())
            val password: ByteArray
            finalOutputFile = mFileUtils.getUniqueFileName(
                file.replace(
                    pdfExtension,
                    mContext.getString(R.string.decrypted_file)
                )
            )
            password = reader.computeUserPassword()
            val input = inputPassword.toByteArray()
            if (input.contentEquals(password)) {
                val stamper = PdfStamper(reader, FileOutputStream(finalOutputFile))
                stamper.close()
                reader.close()
                return finalOutputFile
            }
        } catch (e: DocumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun reorderRemovePDF(
        inputPath: String,
        output: String,
        pages: String,
        onRemoveSuccess: (String) -> Unit
    ): Boolean {
        return try {
            val reader = PdfReader(inputPath)
            reader.selectPages(pages)
            if (reader.numberOfPages == 0) {
                mContext.showSnackbar(R.string.remove_pages_error)
                return false
            }
            //if (reader.getNumberOfPages() )
            val pdfStamper = PdfStamper(reader, FileOutputStream(output))
            pdfStamper.close()
            onRemoveSuccess(output)
            getSnackbarwithAction(
                mContext.content,
                R.string.snackbar_pdfCreated
            ).setAction(R.string.snackbar_viewAction) { v ->
                mFileUtils.openFile(
                    output,
                    FileUtils.FileType.e_PDF
                )
            }.show()
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

    private fun removePasswordUsingDefMasterPassword(
        file: String,
        dataSetChanged: (String, Int) -> Unit,
        inputPassword: Array<String?>
    ): Boolean {
        val finalOutputFile: String
        try {
            val masterPwd = preferencesService.masterPassword
            val reader = PdfReader(file, masterPwd.toByteArray())
            val password: ByteArray
            finalOutputFile = mFileUtils.getUniqueFileName(
                file.replace(
                    pdfExtension,
                    mContext.getString(R.string.decrypted_file)
                )
            )
            password = reader.computeUserPassword()
            val input = inputPassword[0]!!.toByteArray()
            if (Arrays.equals(input, password)) {
                val stamper = PdfStamper(reader, FileOutputStream(finalOutputFile))
                stamper.close()
                reader.close()
                dataSetChanged(finalOutputFile, OPERATION_DECRYPTED)
                mContext.getSnackbarwithAction(R.string.snackbar_pdfCreated)
                    .setAction(R.string.snackbar_viewAction) { v2: View? ->
                        mFileUtils.openFile(
                            finalOutputFile,
                            FileUtils.FileType.e_PDF
                        )
                    }.show()
                return true
            }
        } catch (e: DocumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }


    private fun removePasswordUsingInputMasterPassword(
        file: String,
        dataSetChanged: (String, Int) -> Unit,
        inputPassword: Array<String?>
    ): Boolean {
        val finalOutputFile: String
        try {
            val reader = PdfReader(file, inputPassword[0]!!.toByteArray())
            finalOutputFile = mFileUtils.getUniqueFileName(
                file.replace(
                    pdfExtension,
                    mContext.getString(R.string.decrypted_file)
                )
            )
            val stamper = PdfStamper(reader, FileOutputStream(finalOutputFile))
            stamper.close()
            reader.close()
            dataSetChanged(finalOutputFile, OPERATION_DECRYPTED)
            mContext.getSnackbarwithAction(R.string.snackbar_pdfCreated)
                .setAction(R.string.snackbar_viewAction) { v2: View? ->
                    mFileUtils.openFile(
                        finalOutputFile,
                        FileUtils.FileType.e_PDF
                    )
                }.show()
            return true
        } catch (e: DocumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }


    fun showDetails(pdfFile: PDFFile) {
        val name = pdfFile.pdfFile.name
        val path = pdfFile.pdfFile.path
        val size: String = pdfFile.formattedSize
        val lastModDate: String = pdfFile.getFormattedDate()
        val message = TextView(mContext)
        val title = TextView(mContext)
        message.text = String.format(
            mContext.resources.getString(R.string.file_info),
            name,
            path,
            size,
            lastModDate
        )
        message.setTextIsSelectable(true)
        title.setText(R.string.details)

        title.setPaddingRelative(50, 10, 10, 10)
        message.setPaddingRelative(50, 10, 10, 10)
        title.textSize = 30f

        title.setTextColor(mContext.resources.getColor(R.color.black))
        val builder = AlertDialog.Builder(mContext)
        val dialog = builder.create()

        builder.setView(message)
        builder.setCustomTitle(title)
        builder.setPositiveButton(mContext.resources.getString(R.string.ok)) { _: DialogInterface?, i: Int -> dialog.dismiss() }
        builder.create()
        builder.show()
    }

}