package com.extrastudios.docscanner.dagger

import android.os.Environment
import com.extrastudios.docscanner.utils.*
import dagger.Module

@Module
class PreferencesService {
    var isSoundEnable by booleanProperty(default = true)
    var isVibrateEnable by booleanProperty(default = true)
    var isRatingDone by booleanProperty()
    var storageLocation by stringProperty(
        default = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
    )
    var confirmRemove by booleanProperty()
    var ratingTime by longProperty(default = System.currentTimeMillis())
    var fontSize by integerProperty(default = DEFAULT_FONT_SIZE)
    var fontColor by integerProperty(default = DEFAULT_FONT_COLOR)
    var fontFamily by stringProperty(default = DEFAULT_FONT_FAMILY)
    var pageColor by integerProperty(default = DEFAULT_PAGE_COLOR)
    var pageSize by stringProperty(default = DEFAULT_PAGE_SIZE)
    var defaultCompression by integerProperty(default = DEFAULT_COMPRESSION)
    var borderWidth by integerProperty(default = 0)
    var imageScaleType by stringProperty(default = IMAGE_SCALE_TYPE_ASPECT_RATIO)
    var masterPassword by stringProperty(default = appName)
    var pageNumberStyle by stringProperty()
    var removePage by booleanProperty()
    var launchCount by integerProperty()
}