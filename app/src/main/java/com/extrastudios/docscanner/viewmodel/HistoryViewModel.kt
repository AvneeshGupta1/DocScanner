package com.extrastudios.docscanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.database.AppDatabase
import com.extrastudios.docscanner.database.entity.History
import org.jetbrains.anko.doAsync
import javax.inject.Inject


class HistoryViewModel(application: Application) : AndroidViewModel(application) {


    @Inject
    lateinit var db: AppDatabase

    init {
        (application as DocScannerApplication).getAppComponent().inject(this)
    }

    fun getHistory(filter: ArrayList<Int>): MutableLiveData<List<History>> {
        val historyList = MutableLiveData<List<History>>()
        doAsync {
            historyList.postValue(
                db.getHistoryDao().getHistoryByOperationType(*filter.toIntArray())
            )
        }
        return historyList
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