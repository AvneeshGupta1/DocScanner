package com.extrastudios.docscanner.model

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Font
import ja.burhanrashid52.photoeditor.PhotoFilter


open class CommonItem

data class HomeItem(var type: Int, var icon: Int, var name: String) : CommonItem()

data class HomeHeader(var headerText: Int) : CommonItem()


data class BrushItem(val color: Int)

data class FAQItem(var question: String, var answer: String, var isExpanded: Boolean = false)


data class FilterItem(var imageId: Int, var name: String, var filter: PhotoFilter)


data class PreviewImageOptionItem(var optionImageId: Int, var optionName: String)

class Watermark {
    var watermarkText: String? = null
    var rotationAngle = 0
    var textColor: BaseColor? = null
    var textSize = 0
    var fontFamily: Font.FontFamily? = null
    var fontStyle = 0
}