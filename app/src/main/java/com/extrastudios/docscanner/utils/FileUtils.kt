package com.extrastudios.docscanner.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.utils.FileUriUtils.Companion.instance
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class FileUtils(private val mContext: Activity) {
    private val mSharedPreferences: SharedPreferences

    /**
     * Prints a file
     *
     * @param file the file to be printed
     */


    fun printFile(file: File, onPrint: (String, Int) -> Unit) {
        val mPrintDocumentAdapter: PrintDocumentAdapter = PrintDocumentAdapterHelper(file)
        val printManager = mContext.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = mContext.getString(R.string.app_name) + " Document"
        printManager.print(jobName, mPrintDocumentAdapter, null)
        onPrint(file.absolutePath, OPERATION_PRINTED)
    }


    fun shareFile(file: File?) {
        val uri = FileProvider.getUriForFile(mContext, AUTHORITY_APP, file!!)
        val uris = ArrayList<Uri>()
        uris.add(uri)
        shareFile(uris)
    }

    /**
     * Share the desired PDFs using application of choice by user
     *
     * @param files - the list of files to be shared
     */
    fun shareMultipleFiles(files: ArrayList<File>) {
        val uris = ArrayList<Uri>()
        for (file in files) {
            val uri = FileProvider.getUriForFile(mContext, AUTHORITY_APP, file!!)
            uris.add(uri)
        }
        shareFile(uris)
    }

    /**
     * Emails the desired PDF using application of choice by user
     *
     * @param uris - list of uris to be shared
     */
    private fun shareFile(uris: ArrayList<Uri>) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND_MULTIPLE
        intent.putExtra(
            Intent.EXTRA_TEXT,
            mContext.getString(R.string.i_have_attached_pdfs_to_this_message)
        )
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "application/pdf"
        mContext.startActivity(
            Intent.createChooser(
                intent,
                mContext.resources.getString(R.string.share_chooser)
            )
        )
    }

    /**
     * opens a file in appropriate application
     *
     * @param path - path of the file to be opened
     */
    fun openFile(path: String?, fileType: FileType) {
        if (path == null) {
            mContext.showSnackbar(R.string.error_path_not_found)
            return
        }
        openFileInternal(path, if (fileType == FileType.e_PDF) "application/pdf" else "text/*")
    }

    /**
     * This function is used to open the created file
     * applications on the device.
     *
     * @param path - file path
     */
    private fun openFileInternal(path: String, dataType: String) {
        val file = File(path)
        val target = Intent(Intent.ACTION_VIEW)
        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        try {
            val uri = FileProvider.getUriForFile(mContext, AUTHORITY_APP, file)
            target.setDataAndType(uri, dataType)
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            openIntent(Intent.createChooser(target, mContext.getString(R.string.open_file)))
        } catch (e: Exception) {
            mContext.showSnackbar(R.string.error_open_file)
        }
    }

    /**
     * Checks if the new file already exists.
     *
     * @param finalOutputFile Path of pdf file to check
     * @param mFile           File List of all PDFs
     * @return Number to be added finally in the name to avoid overwrite
     */
    private fun checkRepeat(finalOutputFile: String, mFile: List<File>): Int {
        var flag = true
        var append = 0
        while (flag) {
            append++
            val name = finalOutputFile.replace(pdfExtension, append.toString() + pdfExtension)
            flag = mFile.contains(File(name))
        }
        return append
    }

    /**
     * Get real image path from uri.
     *
     * @param uri - uri of the image
     * @return - real path of the image file on device
     */
    fun getUriRealPath(uri: Uri?): String? {
        return if (uri == null || instance.isWhatsappImage(uri.authority!!)) null else instance.getUriRealPathAboveKitkat(
            mContext,
            uri
        )
    }

    /***
     * Check if file already exists in pdf_dir
     * @param mFileName - Name of the file
     * @return true if file exists else false
     */
    fun isFileExist(mFileName: String): Boolean {
        val path =
            mSharedPreferences.getString(STORAGE_LOCATION, defaultStorageLocation) + mFileName
        val file = File(path)
        return file.exists()
    }

    /**
     * Extracts file name from the URI
     *
     * @param uri - file uri
     * @return - extracted filename
     */
    fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val scheme = uri.scheme ?: return null
        if (scheme == "file") {
            return uri.lastPathSegment
        } else if (scheme == "content") {
            val cursor = mContext.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                if (cursor.count != 0) {
                    val columnIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    fileName = cursor.getString(columnIndex)
                }
                cursor.close()
            }
        }
        return fileName
    }

    /**
     * Returns name of the last file with "_pdf" suffix.
     *
     * @param filesPath - ArrayList of image paths
     * @return fileName with _pdf suffix
     */
    fun getLastFileName(filesPath: ArrayList<String>): String {
        if (filesPath.size == 0) return ""
        val lastSelectedFilePath = filesPath[filesPath.size - 1]
        val nameWithoutExt = stripExtension(getFileNameWithoutExtension(lastSelectedFilePath))
        return nameWithoutExt + mContext.getString(R.string.pdf_suffix)
    }

    /**
     * Returns the filename without its extension
     *
     * @param fileNameWithExt fileName with extension. Ex: androidDev.jpg
     * @return fileName without extension. Ex: androidDev
     */
    fun stripExtension(fileNameWithExt: String?): String? {
        // Handle null case specially.
        if (fileNameWithExt == null) return null

        // Get position of last '.'.
        val pos = fileNameWithExt.lastIndexOf(".")

        // If there wasn't any '.' just return the string as is.
        return if (pos == -1) fileNameWithExt else fileNameWithExt.substring(0, pos)

        // Otherwise return the string, up to the dot.
    }

    /**
     * Opens image in a gallery application
     *
     * @param path - image path
     */
    fun openImage(path: String?) {
        val file = File(path)
        val target = Intent(Intent.ACTION_VIEW)
        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        val uri = FileProvider.getUriForFile(mContext, AUTHORITY_APP, file)
        target.setDataAndType(uri, "image/*")
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        openIntent(Intent.createChooser(target, mContext.getString(R.string.open_file)))
    }

    /**
     * Opens the targeted intent (if possible), otherwise show a snackbar
     *
     * @param intent - input intent
     */
    private fun openIntent(intent: Intent) {
        try {
            mContext.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            mContext.showSnackbar(R.string.snackbar_no_pdf_app)
        }
    }

    /**
     * Returns file chooser intent
     *
     * @return - intent
     */
    val fileChooser: Intent
        get() {
            val folderPath = Environment.getExternalStorageDirectory().toString() + "/"
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            val myUri = Uri.parse(folderPath)
            intent.setDataAndType(myUri, "application/pdf")
            return Intent.createChooser(intent, mContext.getString(R.string.merge_file_select))
        }

    fun getUniqueFileName(fileName: String): String {
        var outputFileName = fileName
        val file = File(outputFileName)
        if (!isFileExist(file.name)) return outputFileName
        val parentFile = file.parentFile
        if (parentFile != null) {
            val listFiles = parentFile.listFiles()
            if (listFiles != null) {
                val append = checkRepeat(outputFileName, Arrays.asList(*listFiles))
                outputFileName =
                    outputFileName.replace(pdfExtension, append.toString() + pdfExtension)
            }
        }
        return outputFileName
    }

    enum class FileType {
        e_PDF, e_TXT
    }

    companion object {
        /**
         * Extracts file name from the path
         *
         * @param path - file path
         * @return - extracted filename
         */
        fun getFileName(path: String?): String? {
            if (path == null) return null
            val index = path.lastIndexOf(PATH_SEPERATOR)
            return if (index < path.length) path.substring(index + 1) else null
        }

        /**
         * Extracts file name from the URI
         *
         * @param path - file path
         * @return - extracted filename without extension
         */
        fun getFileNameWithoutExtension(path: String?): String? {
            if (path == null || path.lastIndexOf(PATH_SEPERATOR) == -1) return path
            var filename = path.substring(path.lastIndexOf(PATH_SEPERATOR) + 1)
            filename = filename.replace(pdfExtension, "")
            return filename
        }

        /**
         * Extracts directory path from full file path
         *
         * @param path absolute path of the file
         * @return absolute path of file directory
         */
        fun getFileDirectoryPath(path: String): String {
            return path.substring(0, path.lastIndexOf(PATH_SEPERATOR) + 1)
        }
    }

    init {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
    }
}