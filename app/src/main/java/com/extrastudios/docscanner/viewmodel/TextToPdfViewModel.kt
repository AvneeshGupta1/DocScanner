package com.extrastudios.docscanner.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.model.HomeItem
import com.extrastudios.docscanner.utils.*
import javax.inject.Inject


class TextToPdfViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var preferencesService: PreferencesService

    init {
        (application as DocScannerApplication).getAppComponent().inject(this)
    }

    fun getTextToPdfItems(
        context: Context,
        pdfOptions: TextToPDFOptions
    ): MutableLiveData<ArrayList<CommonItem>> {
        val mHomeItemList = MutableLiveData<ArrayList<CommonItem>>()
        val homeItems = arrayListOf<CommonItem>()

        homeItems.add(
            HomeItem(
                FONT_COLOR,
                R.drawable.ic_color,
                context.getString(R.string.font_color)
            )
        )
        homeItems.add(
            HomeItem(
                FONT_FAMILY,
                R.drawable.ic_font_family_24dp,
                String.format(
                    context.getString(
                        R.string.default_font_family_text,
                        pdfOptions.fontFamily
                    )
                )
            )
        )
        homeItems.add(
            HomeItem(
                FONT_SIZE,
                R.drawable.ic_font_black_24dp,
                String.format(context.getString(R.string.edit_font_size), pdfOptions.fontSize)
            )
        )
        homeItems.add(
            HomeItem(
                PAGE_COLOR,
                R.drawable.ic_page_color,
                context.getString(R.string.page_color)
            )
        )
        homeItems.add(
            HomeItem(
                PAGE_SIZE,
                R.drawable.ic_page_size_24dp,
                context.getString(R.string.set_page_size_text)
            )
        )
        homeItems.add(
            HomeItem(
                TEXT_TO_PDF_SET_PASSWORD,
                if (pdfOptions.isPasswordProtected) R.drawable.baseline_done_24 else R.drawable.baseline_enhanced_encryption_24,
                context.getString(R.string.set_password)
            )
        )

        mHomeItemList.postValue(homeItems)

        return mHomeItemList
    }
}