package com.extrastudios.docscanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.database.AppDatabase
import com.extrastudios.docscanner.model.PDFFile
import com.extrastudios.docscanner.utils.isPasswordProtected
import com.extrastudios.docscanner.utils.pdfDirectory
import com.extrastudios.docscanner.utils.pdfExtension
import org.jetbrains.anko.doAsync
import java.io.File
import javax.inject.Inject


class ViewFilesViewModel(application: Application) : AndroidViewModel(application) {


    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var preferencesService: PreferencesService

    init {
        (application as DocScannerApplication).getAppComponent().inject(this)
    }

    fun getAllFilesFromFolder(): MutableLiveData<ArrayList<PDFFile>> {
        val response = MutableLiveData<ArrayList<PDFFile>>()
        val mFilePaths = ArrayList<PDFFile>()
        doAsync {
            try {
                val mStorePath: String = preferencesService.storageLocation + pdfDirectory
                val folder = File(mStorePath)
                if (folder.exists()) {
                    val files = folder.listFiles()
                    if (!files.isNullOrEmpty()) {
                        for (file in files) {
                            if (file.exists() && file.nameWithoutExtension.isNotEmpty() && file.name.endsWith(
                                    pdfExtension
                                )
                            ) {
                                val pdfFile = PDFFile(file, file.isPasswordProtected())
                                mFilePaths.add(pdfFile)
                            }
                        }
                    }
                }
                response.postValue(mFilePaths)
            } catch (e: Exception) {
            }
        }
        return response
    }


    fun deleteHistory(): MutableLiveData<Boolean> {
        val response = MutableLiveData<Boolean>()
        doAsync {
            try {
                db.getHistoryDao().deleteHistory()
                response.postValue(true)
            } catch (e: Exception) {
                response.postValue(false)
            }
        }
        return response
    }

}