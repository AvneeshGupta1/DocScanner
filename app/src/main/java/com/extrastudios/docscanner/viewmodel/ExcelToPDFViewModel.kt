package com.extrastudios.docscanner.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.aspose.cells.FileFormatType
import com.aspose.cells.PdfSaveOptions
import com.aspose.cells.PdfSecurityOptions
import com.aspose.cells.Workbook
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.model.HomeItem
import com.extrastudios.docscanner.utils.SET_PASSWORD
import com.extrastudios.docscanner.utils.excelExtension
import com.extrastudios.docscanner.utils.excelWorkbookExtension
import com.extrastudios.docscanner.utils.pdfExtension
import org.jetbrains.anko.doAsync
import java.io.File
import javax.inject.Inject


class ExcelToPDFViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var preferencesService: PreferencesService

    init {
        (application as DocScannerApplication).getAppComponent().inject(this)
    }

    private val mFilePaths = ArrayList<String>()

    fun getExcelToPdfItems(context: Context): MutableLiveData<ArrayList<CommonItem>> {
        val mHomeItemList = MutableLiveData<ArrayList<CommonItem>>()
        val homeItems = arrayListOf<CommonItem>()

        homeItems.add(
            HomeItem(
                SET_PASSWORD,
                R.drawable.baseline_enhanced_encryption_24,
                context.getString(R.string.set_password)
            )
        )
        mHomeItemList.postValue(homeItems)

        return mHomeItemList
    }

    fun loadAllExcelFiles(): MutableLiveData<ArrayList<String>> {
        val response = MutableLiveData<ArrayList<String>>()
        mFilePaths.clear()
        doAsync {
            try {
                walkDir(
                    Environment.getExternalStorageDirectory(),
                    listOf(excelExtension, excelWorkbookExtension)
                )
                response.postValue(mFilePaths)
            } catch (e: Exception) {
            }
        }
        return response
    }

    private fun walkDir(dir: File, extensions: List<String> = listOf(pdfExtension)) {
        val listFile = dir.listFiles()
        if (listFile != null) {
            for (aListFile in listFile) {
                if (aListFile.isDirectory) {
                    walkDir(aListFile, extensions)
                } else {
                    for (extension in extensions) {
                        if (aListFile.name.endsWith(extension)) {
                            mFilePaths.add(aListFile.absolutePath)
                        }
                    }
                }
            }
        }
    }

    fun convertToPdf(
        mRealPath: String,
        mDestPath: String,
        mIsPasswordProtected: Boolean,
        mPassword: String?
    ): MutableLiveData<Boolean> {
        val response = MutableLiveData<Boolean>()
        doAsync {
            try {
                val workbook = Workbook(mRealPath)
                if (mIsPasswordProtected) {
                    val saveOption = PdfSaveOptions()
                    saveOption.securityOptions = PdfSecurityOptions()
                    saveOption.securityOptions.userPassword = mPassword
                    saveOption.securityOptions.ownerPassword = mPassword
                    saveOption.securityOptions.extractContentPermission = false
                    saveOption.securityOptions.printPermission = false
                    workbook.save(mDestPath, saveOption)
                    response.postValue(true)
                } else {
                    workbook.save(mDestPath, FileFormatType.PDF)
                    response.postValue(true)
                }
            } catch (e: Exception) {
                response.postValue(false)
            }
        }
        return response
    }
}