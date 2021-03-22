package com.extrastudios.docscanner.viewmodel

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.model.HomeItem
import com.extrastudios.docscanner.utils.*


class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    init {
        (application as DocScannerApplication).getAppComponent().inject(this)
    }


    fun getImageToPdfItems(
        context: Fragment,
        preferencesService: PreferencesService
    ): MutableLiveData<ArrayList<CommonItem>> {
        val settingsList = MutableLiveData<ArrayList<CommonItem>>()
        val homeItems = arrayListOf<CommonItem>()
        homeItems.add(
            HomeItem(
                IMAGE_COMPRESSION,
                R.drawable.ic_compress_image,
                context.getString(
                    R.string.image_compression_value_default,
                    preferencesService.defaultCompression
                )
            )
        )
        homeItems.add(
            HomeItem(
                SET_PAGE_SIZE,
                R.drawable.ic_page_size_24dp,
                context.getString(R.string.page_size_value_def, preferencesService.pageSize)
            )
        )
        homeItems.add(
            HomeItem(
                FONT_SIZE,
                R.drawable.ic_font_black_24dp,
                context.getString(R.string.font_size_value_def, preferencesService.fontSize)
            )
        )
        homeItems.add(
            HomeItem(
                FONT_FAMILY,
                R.drawable.ic_font_family_24dp,
                context.getString(R.string.font_family_value_def, preferencesService.fontFamily)
            )
        )
        homeItems.add(
            HomeItem(
                SET_IMAGE_SCALE_TYPE,
                R.drawable.ic_aspect_ratio_black_24dp,
                context.getString(R.string.image_scale_type)
            )
        )
        homeItems.add(
            HomeItem(
                CHANGE_MASTER_PASSWORD,
                R.drawable.baseline_enhanced_encryption_24,
                context.getString(R.string.change_master_pwd)
            )
        )
        homeItems.add(
            HomeItem(
                SHOW_PAGE_NUMBERS,
                R.drawable.ic_format_list_numbered_black_24dp,
                context.getString(R.string.show_pg_num)
            )
        )
        settingsList.postValue(homeItems)
        return settingsList
    }
}