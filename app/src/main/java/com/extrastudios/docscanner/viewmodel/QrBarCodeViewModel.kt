package com.extrastudios.docscanner.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.database.AppDatabase
import com.extrastudios.docscanner.database.entity.History
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.model.HomeHeader
import com.extrastudios.docscanner.model.HomeItem
import com.extrastudios.docscanner.utils.*
import org.jetbrains.anko.doAsync
import javax.inject.Inject


class QrBarCodeViewModel(application: Application) : AndroidViewModel(application) {


    @Inject
    lateinit var db: AppDatabase

    init {
        (application as DocScannerApplication).getAppComponent().inject(this)
    }

    fun getQrCodeItems(context: Context): MutableLiveData<ArrayList<CommonItem>> {
        val mHomeItemList = MutableLiveData<ArrayList<CommonItem>>()
        val homeItems = arrayListOf<CommonItem>()

        //create new pdf
        homeItems.add(HomeHeader(R.string.qrbarcodes_to_pdf))
        homeItems.add(
            HomeItem(
                SCAN_QR_CODE,
                R.drawable.ic_qrcode_24dp,
                context.getString(R.string.scan_qrcode)
            )
        )
        homeItems.add(
            HomeItem(
                SCAN_BAR_CODE,
                R.drawable.ic_barcode_24dp,
                context.getString(R.string.scan_barcode)
            )
        )
        mHomeItemList.postValue(homeItems)

        return mHomeItemList
    }

    fun saveHistory(finalOutput: String, operationType: Int): MutableLiveData<Boolean> {
        val response = MutableLiveData<Boolean>()
        doAsync {
            try {
                var icon = R.drawable.ic_insert_drive_file_black_24dp
                when (operationType) {
                    OPERATION_PRINTED -> {
                        icon = R.drawable.ic_print_black_24dp
                    }
                    OPERATION_DELETED -> {
                        icon = R.drawable.baseline_delete_24
                    }
                    OPERATION_RENAME -> {
                        icon = R.drawable.ic_create_black_24dp
                    }
                    OPERATION_ROTATED -> {
                        icon = R.drawable.baseline_crop_rotate_24
                    }
                    OPERATION_ENCRYPTED -> {
                        icon = R.drawable.ic_lock_black_24dp
                    }
                    OPERATION_DECRYPTED -> {
                        icon = R.drawable.ic_lock_open_black_24dp
                    }
                }
                db.getHistoryDao().insertHistoryRecord(
                    History(
                        icon,
                        finalOutput,
                        System.currentTimeMillis(),
                        operationType
                    )
                )
                response.postValue(true)
            } catch (e: Exception) {

            }
        }
        return response
    }
}