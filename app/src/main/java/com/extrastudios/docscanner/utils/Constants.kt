package com.extrastudios.docscanner.utils

import android.Manifest
import android.graphics.Color
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.extrastudios.docscanner.BuildConfig

val requestOptions =
    RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
const val CALENDER_FORMAT = "yyyy-MM-dd hh:mm:ss"
const val APP_INSTALL_FORMAT = "dd-MM-yyyy hh:mm a"
const val REQUEST_APP_SETTINGS = 101
const val BAR_CODE_VALUE = "BarCodeValue"

const val TAKE_PHOTO = 1
const val QR_CODE_BAR_CODE = 2
const val EXCEL_TO_PDF = 3
const val IMAGE_TO_PDF = 4
const val TEXT_TO_PDF = 5

const val VIEW_FILES = 6
const val HISTORY = 7

const val ADD_PASSWORD = 8
const val REMOVE_PASSWORD = 9
const val ADD_TEXT = 10
const val ROTATE_PAGES = 11
const val ADD_WATERMARK = 12
const val ADD_IMAGES = 13

const val MERGE_PDF = 14
const val SPLIT_PDF = 15
const val INVERT_PDF = 16
const val COMPRESS_PDF = 17
const val REMOVE_DUPLICATE_PAGES = 18

const val REMOVE_PAGES = 19
const val REORDER_PAGES = 20
const val EXTRACT_IMAGES = 21
const val PDF_TO_IMAGES = 22
const val EXTRACT_TEXT = 23
const val ZIP_TO_PDF = 24

const val SCAN_QR_CODE = 25
const val SCAN_BAR_CODE = 26

const val SET_PASSWORD = 27

const val PASSWORD_PROTECTED_PDF = 28
const val EDIT_IMAGE = 29
const val IMAGE_COMPRESSION = 30
const val FILTER_IMAGE = 31
const val SET_PAGE_SIZE = 32
const val SET_IMAGE_SCALE_TYPE = 33
const val PREVIEW_PDF = 34
const val BORDER_WIDTH = 35
const val REARRANGE_IMAGES = 36
const val CREATE_GRAY_SCALE_PDF = 37
const val ADD_MARGINS = 38
const val SHOW_PAGE_NUMBERS = 39
const val ADD_WATER_MARK = 40
const val PAGE_COLOR = 41
const val CHANGE_MASTER_PASSWORD = 47

const val FONT_COLOR = 42
const val FONT_FAMILY = 43
const val FONT_SIZE = 44
const val PAGE_SIZE = 45
const val TEXT_TO_PDF_SET_PASSWORD = 46


const val OPERATION_CREATED = 0
const val OPERATION_DELETED = 1
const val OPERATION_RENAME = 2
const val OPERATION_ROTATED = 3
const val OPERATION_PRINTED = 4
const val OPERATION_DECRYPTED = 5
const val OPERATION_ENCRYPTED = 6
const val OPERATION_INVERTED = 7
const val OPERATION_WATER_MARK = 8


const val ACTION_TYPE = "Action"
const val ACTION_ADD_PWD = 1
const val ACTION_REMOVE_PWD = 2


const val ADMOB_BANNER_ID = "ca-app-pub-6249125568831767/1292724192"
const val ADMOB_FULL_SCREEN_ID = "ca-app-pub-6249125568831767/3402553227"
const val ADMOB_NATIVE_ADS = "ca-app-pub-6249125568831767/9909580811"


//Facebook ads  with avneeshgupta1
const val FB_BANNER_ADS = "4148068241886248_4148070088552730"
const val FB_FULL_SCREEN_ID = "4148068241886248_4148075075218898"

const val TYPE_CAPTURE_PHOTO = 1
const val TYPE_BAR_CODE = 2
const val TITLE = "TITLE"
const val SORTING_INDEX = "SORTING_INDEX"
const val IMAGE_EDITOR_KEY = "first"
const val CROP_IMAGE_KEY = "cropImage"
const val DEFAULT_FONT_SIZE_TEXT = "DefaultFontSize"
const val DEFAULT_FONT_SIZE = 11
const val DEFAULT_COMPRESSION = 30
const val PREVIEW_IMAGES = "preview_images"
const val DATABASE_NAME = "ImagesToPdfDB.db"
const val DEFAULT_FONT_FAMILY_TEXT = "DefaultFontFamily"
const val DEFAULT_FONT_FAMILY = "TIMES_ROMAN"
const val DEFAULT_FONT_COLOR_TEXT = "DefaultFontColor"
const val DEFAULT_FONT_COLOR = -16777216

// key for text to pdf (TTP) page color
const val DEFAULT_PAGE_COLOR_TTP = "DefaultPageColorTTP"

// key for images to pdf (ITP) page color
const val DEFAULT_PAGE_COLOR_ITP = "DefaultPageColorITP"
const val DEFAULT_PAGE_COLOR = Color.WHITE
const val DEFAULT_THEME_TEXT = "DefaultTheme"
const val DEFAULT_THEME = "White"
const val DEFAULT_IMAGE_BORDER_TEXT = "Image_border_text"
const val RESULT = "result"
const val SAME_FILE = "SameFile"
const val DEFAULT_PAGE_SIZE_TEXT = "DefaultPageSize"
const val DEFAULT_PAGE_SIZE = "DEFAULT (A4)"
const val CHOICE_REMOVE_IMAGE = "CHOICE_REMOVE_IMAGE"
const val DEFAULT_QUALITY_VALUE = 30
const val DEFAULT_BORDER_WIDTH = 0
const val STORAGE_LOCATION = "storage_location"
const val DEFAULT_IMAGE_SCALE_TYPE_TEXT = "image_scale_type"
const val IMAGE_SCALE_TYPE_STRETCH = "stretch_image"
const val IMAGE_SCALE_TYPE_ASPECT_RATIO = "maintain_aspect_ratio"
const val PG_NUM_STYLE_PAGE_X_OF_N = "pg_num_style_page_x_of_n"
const val PG_NUM_STYLE_X_OF_N = "pg_num_style_x_of_n"
const val PG_NUM_STYLE_X = "pg_num_style_x"
const val MASTER_PWD_STRING = "master_password"
const val BUNDLE_DATA = "bundle_data"
const val LAUNCH_COUNT = "launch_count"
const val pdfDirectory = "/"
const val pdfExtension = ".pdf"
const val appName = "PDF Converter"
const val PATH_SEPERATOR = "/"
const val textExtension = ".txt"
const val excelExtension = ".xls"
const val excelWorkbookExtension = ".xlsx"
const val docExtension = ".doc"
const val docxExtension = ".docx"
const val tempDirectory = "temp"
const val AUTHORITY_APP = BuildConfig.APPLICATION_ID + ".provider"
const val ACTION_SELECT_IMAGES = "android.intent.action.SELECT_IMAGES"
const val ACTION_VIEW_FILES = "android.intent.action.VIEW_FILES"
const val ACTION_TEXT_TO_PDF = "android.intent.action.TEXT_TO_PDF"
const val ACTION_MERGE_PDF = "android.intent.action.MERGE_PDF"
const val OPEN_SELECT_IMAGES = "open_select_images"
const val THEME_WHITE = "White"
const val THEME_BLACK = "Black"
const val THEME_DARK = "Dark"
const val IS_WELCOME_ACTIVITY_SHOWN = "is_Welcome_activity_shown"
const val SHOW_WELCOME_ACT = "show_welcome_activity"
const val VERSION_NAME = "VERSION_NAME"
const val PREF_PAGE_STYLE = "pref_page_number_style"
const val PREF_PAGE_STYLE_ID = "pref_page_number_style_rb_id"
val READ_WRITE_PERMISSIONS =
    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
val READ_WRITE_CAMERA_PERMISSIONS = arrayOf(
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.CAMERA
)
const val PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE_RESULT = 1
const val MODIFY_STORAGE_LOCATION_CODE = 1
const val RECENT_PREF = "Recent"