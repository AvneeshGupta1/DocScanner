package com.extrastudios.docscanner.utils

import android.net.Uri
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.model.Watermark
import com.itextpdf.text.Font
import java.util.*

class ImageToPDFOptions(ps: PreferencesService) : PDFOptions(ps) {
    override var password: String? = ""
    override var isPasswordProtected = false
    var imageCompression: Int = preferencesService.defaultCompression
    override var pageSize: String = ps.pageSize
    var imageScaleType: String = preferencesService.imageScaleType
    override var borderWidth: Int = preferencesService.borderWidth
    var marginLeft = 0
    var marginRight = 0
    var marginTop = 0
    var marginBottom = 0
    var pageNumStyle: String = preferencesService.pageNumberStyle
    var isWatermarkAdded: Boolean = false
    var watermark: Watermark? = null
    override var pageColor = ps.pageColor
    var imagesUri: ArrayList<String>? = null
    var masterPwd: String = ""

    fun setMargins(top: Int, bottom: Int, right: Int, left: Int) {
        marginTop = top
        marginBottom = bottom
        marginRight = right
        marginLeft = left
    }

    fun reset() {
        password = null
        isPasswordProtected = false
        imageCompression = preferencesService.defaultCompression
        pageSize = preferencesService.pageSize
        imageScaleType = preferencesService.imageScaleType
        borderWidth = preferencesService.borderWidth
        marginLeft = 0
        marginRight = 0
        marginTop = 0
        marginBottom = 0
        pageNumStyle = preferencesService.pageNumberStyle
        isWatermarkAdded = false
        watermark = null
        pageColor = preferencesService.pageColor
    }
}


class TextToPDFOptions(ps: PreferencesService) : PDFOptions(ps) {
    var fontSize: Int = ps.fontSize
    var fontColor: Int = ps.fontColor
    var fontFamily: Font.FontFamily = Font.FontFamily.valueOf(ps.fontFamily)
    override var pageColor = ps.pageColor
    override var pageSize: String = ps.pageSize
    override var password: String? = ""
    override var isPasswordProtected = false
    var inFileUri: Uri? = null

    fun reset() {
        password = null
        inFileUri = null
        isPasswordProtected = false
        pageSize = preferencesService.pageSize
        borderWidth = preferencesService.borderWidth
        pageColor = preferencesService.pageColor
        fontSize = preferencesService.fontSize
        fontColor = preferencesService.fontColor
        fontFamily = Font.FontFamily.valueOf(preferencesService.fontFamily)
    }

}

open class PDFOptions(val preferencesService: PreferencesService) {
    open var outFileName: String? = ""
    open var isPasswordProtected: Boolean = false
    open var password: String? = ""
    open var pageSize: String = ""
    open var borderWidth: Int = 0
    open var pageColor: Int = 0
}