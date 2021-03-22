package com.extrastudios.docscanner.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.model.BrushItem
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.model.FilterItem
import com.extrastudios.docscanner.model.HomeItem
import com.extrastudios.docscanner.utils.*
import ja.burhanrashid52.photoeditor.PhotoFilter


class ImageToPdfViewModel(application: Application) : AndroidViewModel(application) {

    init {
        (application as DocScannerApplication).getAppComponent().inject(this)
    }


    fun getImageToPdfItems(
        context: Context,
        pdfOptions: ImageToPDFOptions
    ): MutableLiveData<ArrayList<CommonItem>> {
        val mHomeItemList = MutableLiveData<ArrayList<CommonItem>>()
        val homeItems = arrayListOf<CommonItem>()

        //create new pdf
        homeItems.add(
            HomeItem(
                PASSWORD_PROTECTED_PDF,
                if (pdfOptions.isPasswordProtected) R.drawable.baseline_done_24 else R.drawable.baseline_enhanced_encryption_24,
                context.getString(R.string.password_protect_pdf_text)
            )
        )
        homeItems.add(
            HomeItem(
                EDIT_IMAGE,
                R.drawable.baseline_crop_rotate_24,
                context.getString(R.string.edit_images_text)
            )
        )
        homeItems.add(
            HomeItem(
                IMAGE_COMPRESSION,
                R.drawable.ic_compress_image,
                context.getString(R.string.compress_image, pdfOptions.imageCompression.toString())
            )
        )
        homeItems.add(
            HomeItem(
                FILTER_IMAGE,
                R.drawable.ic_photo_filter_black_24dp,
                context.getString(R.string.filter_images_Text)
            )
        )
        homeItems.add(
            HomeItem(
                SET_PAGE_SIZE,
                R.drawable.ic_page_size_24dp,
                context.getString(R.string.set_page_size_text)
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
                PREVIEW_PDF,
                R.drawable.ic_play_circle_outline_black_24dp,
                context.getString(R.string.preview_image_to_pdf)
            )
        )
        homeItems.add(
            HomeItem(
                BORDER_WIDTH,
                R.drawable.ic_border_image_black_24dp,
                context.getString(R.string.border_dialog_title, pdfOptions.borderWidth)
            )
        )
        homeItems.add(
            HomeItem(
                REARRANGE_IMAGES,
                R.drawable.ic_rearrange,
                context.getString(R.string.rearrange_images)
            )
        )
        homeItems.add(
            HomeItem(
                CREATE_GRAY_SCALE_PDF,
                R.drawable.ic_photo_filter_grey_24dp,
                context.getString(R.string.grayscale_images)
            )
        )
        homeItems.add(
            HomeItem(
                ADD_MARGINS,
                R.drawable.ic_page_size_24dp,
                context.getString(R.string.add_margins)
            )
        )
        homeItems.add(
            HomeItem(
                SHOW_PAGE_NUMBERS,
                R.drawable.ic_format_list_numbered_black_24dp,
                context.getString(R.string.show_pg_num)
            )
        )
        homeItems.add(
            HomeItem(
                ADD_WATER_MARK,
                R.drawable.ic_branding_watermark_black_24dp,
                context.getString(R.string.add_watermark)
            )
        )
        homeItems.add(
            HomeItem(
                PAGE_COLOR,
                R.drawable.ic_page_color,
                context.getString(R.string.page_color)
            )
        )
        mHomeItemList.postValue(homeItems)

        return mHomeItemList
    }

    fun getBrushItemList(): ArrayList<BrushItem> {
        val brushItems = ArrayList<BrushItem>()
        brushItems.add(BrushItem(R.color.mb_white))
        brushItems.add(BrushItem(R.color.red))
        brushItems.add(BrushItem(R.color.mb_blue))
        brushItems.add(BrushItem(R.color.mb_green))
        brushItems.add(BrushItem(R.color.colorPrimary))
        brushItems.add(BrushItem(R.color.colorPlate))
        brushItems.add(BrushItem(R.color.light_gray))
        brushItems.add(BrushItem(R.color.black))
        brushItems.add(BrushItem(R.drawable.color_palette))
        return brushItems
    }

    fun getFilterItemsList(context: Context): ArrayList<FilterItem> {
        val items = ArrayList<FilterItem>()
        items.add(
            FilterItem(
                R.drawable.none,
                context.getString(R.string.filter_none),
                PhotoFilter.NONE
            )
        )
        items.add(
            FilterItem(
                R.drawable.brush,
                context.getString(R.string.filter_brush),
                PhotoFilter.NONE
            )
        )
        items.add(
            FilterItem(
                R.drawable.auto_fix,
                context.getString(R.string.filter_autofix),
                PhotoFilter.AUTO_FIX
            )
        )
        items.add(
            FilterItem(
                R.drawable.black,
                context.getString(R.string.filter_grayscale),
                PhotoFilter.GRAY_SCALE
            )
        )
        items.add(
            FilterItem(
                R.drawable.brightness,
                context.getString(R.string.filter_brightness),
                PhotoFilter.BRIGHTNESS
            )
        )
        items.add(
            FilterItem(
                R.drawable.contrast,
                context.getString(R.string.filter_contrast),
                PhotoFilter.CONTRAST
            )
        )
        items.add(
            FilterItem(
                R.drawable.cross_process,
                context.getString(R.string.filter_cross),
                PhotoFilter.CROSS_PROCESS
            )
        )
        items.add(
            FilterItem(
                R.drawable.documentary,
                context.getString(R.string.filter_documentary),
                PhotoFilter.DOCUMENTARY
            )
        )
        items.add(
            FilterItem(
                R.drawable.due_tone,
                context.getString(R.string.filter_duetone),
                PhotoFilter.DUE_TONE
            )
        )
        items.add(
            FilterItem(
                R.drawable.fill_light,
                context.getString(R.string.filter_filllight),
                PhotoFilter.FILL_LIGHT
            )
        )
        items.add(
            FilterItem(
                R.drawable.flip_vertical,
                context.getString(R.string.filter_filpver),
                PhotoFilter.FLIP_VERTICAL
            )
        )
        items.add(
            FilterItem(
                R.drawable.flip_horizontal,
                context.getString(R.string.filter_fliphor),
                PhotoFilter.FLIP_HORIZONTAL
            )
        )
        items.add(
            FilterItem(
                R.drawable.grain,
                context.getString(R.string.filter_grain),
                PhotoFilter.GRAIN
            )
        )
        items.add(
            FilterItem(
                R.drawable.lomish,
                context.getString(R.string.filter_lomish),
                PhotoFilter.LOMISH
            )
        )
        items.add(
            FilterItem(
                R.drawable.negative,
                context.getString(R.string.filter_negative),
                PhotoFilter.NEGATIVE
            )
        )
        items.add(
            FilterItem(
                R.drawable.poster,
                context.getString(R.string.filter_poster),
                PhotoFilter.POSTERIZE
            )
        )
        items.add(
            FilterItem(
                R.drawable.rotate,
                context.getString(R.string.filter_rotate),
                PhotoFilter.ROTATE
            )
        )
        items.add(
            FilterItem(
                R.drawable.saturate,
                context.getString(R.string.filter_saturate),
                PhotoFilter.SATURATE
            )
        )
        items.add(
            FilterItem(
                R.drawable.sepia,
                context.getString(R.string.filter_sepia),
                PhotoFilter.SEPIA
            )
        )
        items.add(
            FilterItem(
                R.drawable.sharpen,
                context.getString(R.string.filter_sharpen),
                PhotoFilter.SHARPEN
            )
        )
        items.add(
            FilterItem(
                R.drawable.temp,
                context.getString(R.string.filter_temp),
                PhotoFilter.TEMPERATURE
            )
        )
        items.add(
            FilterItem(
                R.drawable.tint,
                context.getString(R.string.filter_tint),
                PhotoFilter.TINT
            )
        )
        items.add(
            FilterItem(
                R.drawable.vignette,
                context.getString(R.string.filter_vig),
                PhotoFilter.VIGNETTE
            )
        )
        return items
    }

}