package com.extrastudios.docscanner.viewmodel

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.model.HomeHeader
import com.extrastudios.docscanner.model.HomeItem
import com.extrastudios.docscanner.utils.*


class HomeViewModel(application: Application) : AndroidViewModel(application) {

    init {
        (application as DocScannerApplication).getAppComponent().inject(this)
    }

    fun getHomeItems(context: Fragment): MutableLiveData<ArrayList<CommonItem>> {
        val mHomeItemList = MutableLiveData<ArrayList<CommonItem>>()
        val homeItems = arrayListOf<CommonItem>()

        //create new pdf
        homeItems.add(HomeHeader(R.string.create_new_pdfs))
        homeItems.add(
            HomeItem(
                TAKE_PHOTO,
                R.drawable.ic_menu_camera,
                context.getString(R.string.take_photo_and_convert_to_pdf)
            )
        )
        homeItems.add(
            HomeItem(
                QR_CODE_BAR_CODE,
                R.drawable.ic_qrcode_24dp,
                context.getString(R.string.qr_barcode_pdf)
            )
        )
        homeItems.add(
            HomeItem(
                EXCEL_TO_PDF,
                R.drawable.ic_excel,
                context.getString(R.string.excel_to_pdf)
            )
        )
        homeItems.add(
            HomeItem(
                IMAGE_TO_PDF,
                R.drawable.ic_image_gallery,
                context.getString(R.string.images_to_pdf)
            )
        )
        homeItems.add(
            HomeItem(
                TEXT_TO_PDF,
                R.drawable.ic_text_format_black_24dp,
                context.getString(R.string.text_to_pdf)
            )
        )

        //view pdf
        homeItems.add(HomeHeader(R.string.view_pdfs))
        homeItems.add(
            HomeItem(
                VIEW_FILES,
                R.drawable.ic_image_gallery,
                context.getString(R.string.viewFiles)
            )
        )
        homeItems.add(
            HomeItem(
                HISTORY,
                R.drawable.ic_history_black_24dp,
                context.getString(R.string.history)
            )
        )

        //Enhance created PDFs
        homeItems.add(HomeHeader(R.string.enhance_created_pdfs))
        homeItems.add(
            HomeItem(
                ADD_PASSWORD,
                R.drawable.ic_lock_black_24dp,
                context.getString(R.string.add_password)
            )
        )
        homeItems.add(
            HomeItem(
                REMOVE_PASSWORD,
                R.drawable.ic_lock_open_black_24dp,
                context.getString(R.string.remove_password)
            )
        )
        homeItems.add(
            HomeItem(
                ADD_TEXT,
                R.drawable.ic_text_format_black_24dp,
                context.getString(R.string.add_text)
            )
        )
        homeItems.add(
            HomeItem(
                ROTATE_PAGES,
                R.drawable.baseline_crop_rotate_24,
                context.getString(R.string.rotate_pages)
            )
        )
        homeItems.add(
            HomeItem(
                ADD_WATERMARK,
                R.drawable.ic_branding_watermark_black_24dp,
                context.getString(R.string.add_watermark)
            )
        )
        homeItems.add(
            HomeItem(
                ADD_IMAGES,
                R.drawable.ic_add_black_24dp,
                context.getString(R.string.add_images)
            )
        )

        //modify existing PDFs
        homeItems.add(HomeHeader(R.string.modify_existing_pdfs))
        homeItems.add(
            HomeItem(
                MERGE_PDF,
                R.drawable.ic_merge_type_black_24dp,
                context.getString(R.string.merge_pdf)
            )
        )
        homeItems.add(
            HomeItem(
                SPLIT_PDF,
                R.drawable.ic_call_split_black_24dp,
                context.getString(R.string.split_pdf)
            )
        )
        homeItems.add(
            HomeItem(
                INVERT_PDF,
                R.drawable.ic_invert_color_24dp,
                context.getString(R.string.invert_pdf)
            )
        )
        homeItems.add(
            HomeItem(
                COMPRESS_PDF,
                R.drawable.ic_compress_image,
                context.getString(R.string.compress_pdf)
            )
        )
        homeItems.add(
            HomeItem(
                REMOVE_DUPLICATE_PAGES,
                R.drawable.ic_remove_duplicate_square_black,
                context.getString(R.string.remove_duplicate_pages)
            )
        )

        //mode options
        homeItems.add(HomeHeader(R.string.more_options))
        homeItems.add(
            HomeItem(
                REMOVE_PAGES,
                R.drawable.ic_remove_circle_black_24dp,
                context.getString(R.string.remove_pages)
            )
        )
        homeItems.add(
            HomeItem(
                REORDER_PAGES,
                R.drawable.ic_sort,
                context.getString(R.string.reorder_pages)
            )
        )
        homeItems.add(
            HomeItem(
                EXTRACT_IMAGES,
                R.drawable.ic_broken_image_black_24dp,
                context.getString(R.string.extract_images)
            )
        )
        homeItems.add(
            HomeItem(
                PDF_TO_IMAGES,
                R.drawable.ic_image_black_24dp,
                context.getString(R.string.pdf_to_images)
            )
        )
        homeItems.add(
            HomeItem(
                EXTRACT_TEXT,
                R.drawable.ic_text_format_black_24dp,
                context.getString(R.string.extract_text)
            )
        )
        homeItems.add(
            HomeItem(
                ZIP_TO_PDF,
                R.drawable.ic_zip_to_pdf,
                context.getString(R.string.zip_to_pdf)
            )
        )


        mHomeItemList.postValue(homeItems)
        return mHomeItemList
    }
}