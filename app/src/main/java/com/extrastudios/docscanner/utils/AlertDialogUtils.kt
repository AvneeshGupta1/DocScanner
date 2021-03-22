package com.extrastudios.docscanner.utils

import android.app.Activity
import android.graphics.Color
import android.text.Editable
import android.text.TextUtils.isEmpty
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.model.PDFFile
import com.extrastudios.docscanner.model.Watermark
import com.github.danielnilsson9.colorpickerview.view.ColorPickerView
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Font
import org.jetbrains.anko.alert
import kotlin.text.isNotEmpty as isNotEmpty1


fun Fragment.openSaveDialog(preFillName: String?, saveMethod: (String) -> Unit) {
    val builder = MaterialDialog.Builder(activity!!).title(R.string.creating_pdf)
        .content(R.string.enter_file_name).positiveText(R.string.ok).negativeText(R.string.cancel)
    builder.input(
        getString(R.string.example),
        preFillName
    ) { dialog: MaterialDialog?, input: CharSequence ->
        if (input.isEmpty()) {
            showSnackbar(R.string.snackbar_name_not_blank)
        } else {
            val filename = input.toString()
            if (!(filename + pdfExtension).isFileExist()) {
                saveMethod(filename)
            } else {
                val builder2 = MaterialDialog.Builder(activity!!).title(R.string.warning)
                    .content(R.string.overwrite_message).positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                builder2.onPositive { _: MaterialDialog?, _: DialogAction? -> saveMethod(filename) }
                    .onNegative { _: MaterialDialog?, which: DialogAction? ->
                        openSaveDialog(preFillName, saveMethod)
                    }.show()
            }
        }
    }.show()
}

fun Fragment.showColorChangeDialog(ttpdf: TextToPDFOptions) {
    val materialDialog = MaterialDialog.Builder(activity!!).title(R.string.page_color)
        .customView(R.layout.dialog_color_chooser, true).positiveText(R.string.ok)
        .negativeText(R.string.cancel).onPositive { dialog: MaterialDialog, which: DialogAction? ->
        val view = dialog.customView
        val colorPickerView: ColorPickerView = view!!.findViewById(R.id.color_picker)
        val defaultCheckbox = view.findViewById<CheckBox>(R.id.set_default)
        val fontColor = colorPickerView.color
        val pageColor: Int = ttpdf.preferencesService.pageColor
        if (colorSimilarCheck(fontColor, pageColor)) {
            showSnackbar(R.string.snackbar_color_too_close)
        }
        if (defaultCheckbox.isChecked) {
            ttpdf.preferencesService.fontColor = fontColor
        }
        ttpdf.fontColor = fontColor

    }.build()

    val colorPickerView: ColorPickerView =
        materialDialog.customView!!.findViewById(R.id.color_picker)
    colorPickerView.color = ttpdf.fontColor
    materialDialog.show()


}

fun Fragment.showFontFamilyDialog(
    ttpdf: TextToPDFOptions,
    forSettings: Boolean = false,
    onFontChange: (String) -> Unit
) {
    val view = layoutInflater.inflate(R.layout.dialog_font_family, null)
    val cbSetDefault = view.findViewById<CheckBox>(R.id.cbSetDefault)
    cbSetDefault.goneIf(forSettings)
    val materialDialog: MaterialDialog = MaterialDialog.Builder(activity!!).title(
        String.format(
            getString(R.string.default_font_family_text),
            ttpdf.preferencesService.fontFamily
        )
    ).customView(view, true).positiveText(R.string.ok).negativeText(R.string.cancel)
        .onPositive { dialog: MaterialDialog, which: DialogAction? ->
            val radioGroup = view!!.findViewById<RadioGroup>(R.id.radio_group_font_family)
            val selectedId = radioGroup.checkedRadioButtonId
            val radioButton = view.findViewById<RadioButton>(selectedId)
            val fontFamily1 = radioButton.text.toString()
            ttpdf.fontFamily = Font.FontFamily.valueOf(fontFamily1)
            if (cbSetDefault.isChecked || forSettings) {
                ttpdf.preferencesService.fontFamily = fontFamily1
            }
            onFontChange(fontFamily1)
        }.build()

    val radioGroup =
        materialDialog.customView!!.findViewById<RadioGroup>(R.id.radio_group_font_family)
    val rb = radioGroup.getChildAt(ttpdf.fontFamily.ordinal) as RadioButton
    rb.isChecked = true
    materialDialog.show()
}

fun Fragment.showFontSizeDialog(
    ttpdf: TextToPDFOptions,
    forSettings: Boolean = false,
    onFontSizeChange: (Int) -> Unit
) {
    val view = layoutInflater.inflate(R.layout.dialog_font_size, null)
    val cbSetDefault = view.findViewById<CheckBox>(R.id.cbSetFontDefault)
    cbSetDefault.goneIf(forSettings)

    MaterialDialog.Builder(activity!!)
        .title(String.format(getString(R.string.edit_font_size), ttpdf.fontSize))
        .customView(view, true).positiveText(R.string.ok).negativeText(R.string.cancel)
        .onPositive { dialog: MaterialDialog, which: DialogAction? ->
            hideKeyboard()
            val fontInput = view.findViewById<EditText>(R.id.fontInput)
            try {
                val check = fontInput.text.toString().toInt()
                if (check > 1000 || check < 0) {
                    showSnackbar(R.string.invalid_entry)
                } else {
                    ttpdf.fontSize = check
                    showSnackbar(R.string.font_size_changed)
                    if (cbSetDefault.isChecked || forSettings) {
                        ttpdf.preferencesService.fontSize = check
                    }
                    onFontSizeChange(check)
                }
            } catch (e: NumberFormatException) {
                showSnackbar(R.string.invalid_entry)
            }
        }.show()
}

fun Fragment.showPageColorDialog(ttpdf: PDFOptions) {
    val view = layoutInflater.inflate(R.layout.dialog_color_chooser, null)
    val materialDialog =
        MaterialDialog.Builder(activity!!).title(R.string.font_color).customView(view, true)
            .positiveText(R.string.ok).negativeText(R.string.cancel)
            .onPositive { dialog: MaterialDialog, which: DialogAction? ->
                val colorPickerView: ColorPickerView = view.findViewById(R.id.color_picker)
                val defaultCheckbox = view.findViewById<CheckBox>(R.id.set_default)
                val fontColor = colorPickerView.color
                val pageColor: Int = ttpdf.preferencesService.fontColor
                if (colorSimilarCheck(fontColor, pageColor)) {
                    showSnackbar(R.string.snackbar_color_too_close)
                }
                if (defaultCheckbox.isChecked) {
                    ttpdf.preferencesService.pageColor = fontColor
                }
                ttpdf.pageColor = fontColor
            }.build()

    val colorPickerView: ColorPickerView = view.findViewById(R.id.color_picker)
    colorPickerView.color = ttpdf.pageColor
    materialDialog?.show()
}

fun Fragment.showPageSizeDialog(ttpdf: PDFOptions, forSettings: Boolean = false) {
    val mPageSizeToString = HashMap<Int, Int>()
    mPageSizeToString[R.id.page_size_default] = R.string.a4
    mPageSizeToString[R.id.page_size_legal] = R.string.legal
    mPageSizeToString[R.id.page_size_executive] = R.string.executive
    mPageSizeToString[R.id.page_size_ledger] = R.string.ledger
    mPageSizeToString[R.id.page_size_tabloid] = R.string.tabloid
    mPageSizeToString[R.id.page_size_letter] = R.string.letter
    val view = layoutInflater.inflate(R.layout.set_page_size_dialog, null)
    val materialDialog = getPageSizeDialog(view, this, mPageSizeToString, ttpdf, forSettings)
    val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group_page_size)
    val spinnerA = view.findViewById<Spinner>(R.id.spinner_page_size_a0_a10)
    val spinnerB = view.findViewById<Spinner>(R.id.spinner_page_size_b0_b10)
    if (ttpdf.pageSize.startsWith("A")) {
        radioGroup?.check(R.id.page_size_a0_a10)
        spinnerA?.setSelection(ttpdf.pageSize.substring(1).toInt())
    } else if (ttpdf.pageSize.startsWith("B")) {
        radioGroup?.check(R.id.page_size_b0_b10)
        spinnerB?.setSelection(ttpdf.pageSize.substring(1).toInt())
    } else {
        val key = getKey(this, mPageSizeToString, ttpdf.pageSize)
        if (key != null) radioGroup?.check(key)
    }
    materialDialog.show()
}

private fun getPageSizeDialog(
    view: View,
    fragment: Fragment,
    mPageSizeToString: HashMap<Int, Int>,
    ttpdf: PDFOptions,
    forSettings: Boolean
): MaterialDialog {
    val builder: MaterialDialog.Builder =
        MaterialDialog.Builder(fragment.activity!!).title(R.string.set_page_size_text)
            .customView(view, true).positiveText(R.string.ok).negativeText(R.string.cancel)
    val mSetAsDefault = view.findViewById<CheckBox>(R.id.set_as_default)
    mSetAsDefault.goneIf(forSettings)

    return builder.customView(view, true)
        .onPositive { dialog1: MaterialDialog, which: DialogAction? ->
            val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group_page_size)
            val selectedId = radioGroup.checkedRadioButtonId
            val spinnerA = view.findViewById<Spinner>(R.id.spinner_page_size_a0_a10)
            val spinnerB = view.findViewById<Spinner>(R.id.spinner_page_size_b0_b10)
            val pageSize = getPageSize(
                fragment,
                mPageSizeToString,
                selectedId,
                spinnerA.selectedItem.toString(),
                spinnerB.selectedItem.toString()
            )

            if (mSetAsDefault.isChecked || forSettings) {
                ttpdf.preferencesService.pageSize = pageSize
            }
            ttpdf.pageSize = pageSize

        }.build()
}

private fun getKey(fragment: Fragment, map: HashMap<Int, Int>, value: String): Int? {
    for ((key, value1) in map) {
        if (value == fragment.getString(value1)) {
            return key
        }
    }
    return null
}

private fun getPageSize(
    fragment: Fragment,
    mPageSizeToString: HashMap<Int, Int>,
    selectionId: Int,
    spinnerAValue: String,
    spinnerBValue: String
): String {
    val stringPageSize: String
    var pageSize = ""
    when (selectionId) {
        R.id.page_size_a0_a10 -> {
            stringPageSize = spinnerAValue
            pageSize = stringPageSize.substring(0, stringPageSize.indexOf(" "))
        }
        R.id.page_size_b0_b10 -> {
            stringPageSize = spinnerBValue
            pageSize = stringPageSize.substring(0, stringPageSize.indexOf(" "))
        }
        else -> pageSize = fragment.getString(mPageSizeToString.get(selectionId)!!)
    }
    return pageSize
}

fun Fragment.showImageCompressionDialog(
    mPdfOptions: ImageToPDFOptions,
    forSettings: Boolean = false,
    imageCompressionChange: () -> Unit
) {
    val view = layoutInflater.inflate(R.layout.compress_image_dialog, null)
    val qualityInput: EditText = view.findViewById(R.id.quality)
    qualityInput.setText(mPdfOptions.imageCompression.toString())

    val cbSetDefault = view.findViewById<CheckBox>(R.id.cbSetDefault)
    cbSetDefault.goneIf(forSettings)

    val dialog: MaterialDialog =
        MaterialDialog.Builder(activity!!).title(R.string.compression_image_edit)
            .positiveText(android.R.string.ok).negativeText(android.R.string.cancel)
            .customView(view, true).onPositive { dialog1, which ->
            val check: Int
            try {
                check = qualityInput.text.toString().toInt()
                if (check > 100 || check < 0) {
                    showSnackbar(R.string.invalid_entry)
                } else {
                    mPdfOptions.imageCompression = check
                    if (cbSetDefault.isChecked) {
                        mPdfOptions.preferencesService.defaultCompression = check
                    }
                    if (forSettings) {
                        mPdfOptions.preferencesService.defaultCompression = check
                    }
                    imageCompressionChange()
                }
            } catch (e: java.lang.NumberFormatException) {
                showSnackbar(R.string.invalid_entry)
            }

        }.build()
    dialog.show()
}

fun Fragment.showSetPasswordDialog(ttpdf: PDFOptions, passwordChange: () -> Unit) {

    val dialog = MaterialDialog.Builder(activity!!).title(R.string.set_password)
        .customView(R.layout.custom_dialog, true).positiveText(android.R.string.ok)
        .negativeText(android.R.string.cancel).neutralText(R.string.remove_dialog).build()
    val positiveAction: View = dialog.getActionButton(DialogAction.POSITIVE)
    val neutralAction: View = dialog.getActionButton(DialogAction.NEUTRAL)
    val passwordInput = dialog.customView!!.findViewById<EditText>(R.id.password)
    passwordInput.setText(ttpdf.password)
    passwordInput.addTextChangedListener(object : DefaultTextWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            positiveAction.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty1()
        }
    })

    positiveAction.setOnClickListener { v: View? ->
        if (isEmpty(passwordInput.text)) {
            showSnackbar(R.string.snackbar_password_cannot_be_blank)
        } else {
            hideKeyboard()
            ttpdf.password = (passwordInput.text.toString())
            ttpdf.isPasswordProtected = true
            passwordChange()
            dialog.dismiss()
        }
    }

    if (!ttpdf.password.isNullOrEmpty()) {
        neutralAction.setOnClickListener { v: View? ->
            hideKeyboard()
            ttpdf.password = null
            ttpdf.isPasswordProtected = false
            dialog.dismiss()
            passwordChange()
            showSnackbar(R.string.password_remove)
        }
    }
    dialog.show()
    positiveAction.isEnabled = false
    neutralAction.isEnabled = !ttpdf.password.isNullOrEmpty()
}

fun Fragment.showBorderDialog(mPdfOptions: ImageToPDFOptions, onBorderWidthChange: () -> Unit) {
    val view = layoutInflater.inflate(R.layout.dialog_border_image, null)
    val input = view!!.findViewById<EditText>(R.id.border_width)
    input.setText(mPdfOptions.borderWidth.toString())

    MaterialDialog.Builder(activity!!).title(R.string.border).positiveText(android.R.string.ok)
        .negativeText(android.R.string.cancel).customView(view, true).onPositive { dialog1, which ->
        var value = 0
        try {
            value = input.text.toString().toInt()
            if (value > 200 || value < 0) {
                showSnackbar(R.string.invalid_entry)
            } else {
                mPdfOptions.borderWidth = value
            }
        } catch (e: java.lang.NumberFormatException) {
            showSnackbar(R.string.invalid_entry)
        }
        val cbSetDefault = view.findViewById<CheckBox>(R.id.cbSetDefault)
        if (cbSetDefault.isChecked) {
            mPdfOptions.preferencesService.borderWidth = value
        }
        onBorderWidthChange()
    }.build().show()

}

fun Fragment.showMarginDialog(mPdfOptions: ImageToPDFOptions) {
    val view = layoutInflater.inflate(R.layout.add_margins_dialog, null)
    val top = view!!.findViewById<EditText>(R.id.topMarginEditText)
    val bottom = view.findViewById<EditText>(R.id.bottomMarginEditText)
    val right = view.findViewById<EditText>(R.id.rightMarginEditText)
    val left = view.findViewById<EditText>(R.id.leftMarginEditText)
    top.setText(mPdfOptions.marginTop.toString())
    bottom.setText(mPdfOptions.marginBottom.toString())
    right.setText(mPdfOptions.marginRight.toString())
    left.setText(mPdfOptions.marginLeft.toString())
    activity?.alert {
        title(R.string.add_margins)
        customView(view)
        positiveButton(R.string.ok) {
            val mMarginTop = top.text.toString().parseIntOrDefault()
            val mMarginBottom = bottom.text.toString().parseIntOrDefault()
            val mMarginRight = right.text.toString().parseIntOrDefault()
            val mMarginLeft = left.text.toString().parseIntOrDefault()
            mPdfOptions.setMargins(mMarginTop, mMarginBottom, mMarginRight, mMarginLeft)
        }
        negativeButton(R.string.cancel)
    }?.show()
}

fun Fragment.showPageNumberStyleDialog(
    mPdfOptions: ImageToPDFOptions,
    forSettings: Boolean = false
) {

    val view = layoutInflater.inflate(R.layout.add_pgnum_dialog, null)
    val rbOpt1 = view.findViewById<RadioButton>(R.id.page_num_opt1)
    val rbOpt2 = view.findViewById<RadioButton>(R.id.page_num_opt2)
    val rbOpt3 = view.findViewById<RadioButton>(R.id.page_num_opt3)

    rbOpt1.isChecked = mPdfOptions.pageNumStyle == PG_NUM_STYLE_PAGE_X_OF_N
    rbOpt2.isChecked = mPdfOptions.pageNumStyle == PG_NUM_STYLE_X_OF_N
    rbOpt3.isChecked = mPdfOptions.pageNumStyle == PG_NUM_STYLE_X

    val rg = view.findViewById<RadioGroup>(R.id.radioGroup)
    val cbDefault = view.findViewById<CheckBox>(R.id.set_as_default)
    cbDefault.goneIf(forSettings)
    var mPageNumStyle = ""


    val alert = activity?.alert {
        title(R.string.choose_page_number_style)
        customView(view)
        positiveButton(R.string.ok) {
            when (rg.checkedRadioButtonId) {
                rbOpt1.id -> {
                    mPageNumStyle = PG_NUM_STYLE_PAGE_X_OF_N
                }
                rbOpt2.id -> {
                    mPageNumStyle = PG_NUM_STYLE_X_OF_N
                }
                rbOpt3.id -> {
                    mPageNumStyle = PG_NUM_STYLE_X
                }
            }
            if (cbDefault.isChecked || forSettings) {
                mPdfOptions.preferencesService.pageNumberStyle = mPageNumStyle
            } else {
                mPdfOptions.preferencesService.pageNumberStyle = ""
            }
            mPdfOptions.pageNumStyle = mPageNumStyle
        }
        negativeButton(R.string.cancel)
        neutralButton(R.string.remove_dialog) {
            mPdfOptions.pageNumStyle = ""
        }
    }
    alert?.show()
    val button = alert?.dialog?.getButton(AlertDialog.BUTTON_NEUTRAL)
    button?.isEnabled = mPdfOptions.pageNumStyle.isNotEmpty1()
}

fun Fragment.addWatermark(mPdfOptions: ImageToPDFOptions) {

    val dialog: MaterialDialog = MaterialDialog.Builder(activity!!).title(R.string.add_watermark)
        .customView(R.layout.add_watermark_dialog, true).positiveText(android.R.string.ok)
        .negativeText(android.R.string.cancel).neutralText(R.string.remove_dialog).build()

    val positiveAction: View = dialog.getActionButton(DialogAction.POSITIVE)
    val neutralAction: View = dialog.getActionButton(DialogAction.NEUTRAL)
    val watermark = Watermark()
    val watermarkTextInput = dialog.customView!!.findViewById<EditText>(R.id.watermarkText)
    val angleInput = dialog.customView!!.findViewById<EditText>(R.id.watermarkAngle)
    val colorPickerInput: ColorPickerView = dialog.customView!!.findViewById(R.id.watermarkColor)
    val fontSizeInput = dialog.customView!!.findViewById<EditText>(R.id.watermarkFontSize)
    val fontFamilyInput = dialog.customView!!.findViewById<Spinner>(R.id.watermarkFontFamily)
    val styleInput = dialog.customView!!.findViewById<Spinner>(R.id.watermarkStyle)
    val fontFamilyAdapter = ArrayAdapter(
        activity!!,
        android.R.layout.simple_spinner_dropdown_item,
        Font.FontFamily.values()
    )
    fontFamilyInput.adapter = fontFamilyAdapter
    val styleAdapter: ArrayAdapter<String> = ArrayAdapter(
        activity!!,
        android.R.layout.simple_spinner_dropdown_item,
        resources.getStringArray(R.array.fontStyles)
    )
    styleInput.adapter = styleAdapter
    if (mPdfOptions.isWatermarkAdded && mPdfOptions.watermark != null) {
        watermarkTextInput.setText(mPdfOptions.watermark!!.watermarkText)
        angleInput.setText(mPdfOptions.watermark!!.rotationAngle.toString())
        fontSizeInput.setText(mPdfOptions.watermark!!.textSize.toString())
        val color: BaseColor? = mPdfOptions.watermark!!.textColor
        colorPickerInput.color = color!!.rgb
        fontFamilyInput.setSelection(fontFamilyAdapter.getPosition(mPdfOptions.watermark!!.fontFamily))
        styleInput.setSelection(styleAdapter.getPosition(getStyleNameFromFont(mPdfOptions.watermark!!.fontStyle)))
    } else {
        angleInput.setText("0")
        fontSizeInput.setText("50")
    }
    watermarkTextInput.addTextChangedListener(object : DefaultTextWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            positiveAction.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty1()
        }

        override fun afterTextChanged(s: Editable) {
            if (s.isEmpty()) {
                showSnackbar(R.string.snackbar_watermark_cannot_be_blank)
            } else {
                watermark.watermarkText = s.toString()
            }
        }
    })
    neutralAction.isEnabled = mPdfOptions.isWatermarkAdded
    positiveAction.isEnabled = mPdfOptions.isWatermarkAdded

    neutralAction.setOnClickListener { v: View? ->
        mPdfOptions.isWatermarkAdded = false
        dialog.dismiss()
        showSnackbar(R.string.watermark_remove)
    }
    positiveAction.setOnClickListener { v: View? ->
        watermark.watermarkText = watermarkTextInput.text.toString()
        watermark.fontFamily = fontFamilyInput.selectedItem as Font.FontFamily
        watermark.fontStyle = getStyleValueFromName(styleInput.selectedItem as String)
        watermark.rotationAngle = angleInput.text.toString().parseIntOrDefault()
        watermark.textSize = fontSizeInput.text.toString().parseIntOrDefault(50)
        watermark.textColor = (BaseColor(
            Color.red(colorPickerInput.color),
            Color.green(colorPickerInput.color),
            Color.blue(colorPickerInput.color),
            Color.alpha(colorPickerInput.color)
        ))
        mPdfOptions.watermark = watermark
        mPdfOptions.isWatermarkAdded = true
        dialog.dismiss()
        showSnackbar(R.string.watermark_added)
    }
    dialog.show()
}

private fun getStyleValueFromName(name: String?): Int {
    return when (name) {
        "BOLD" -> Font.BOLD
        "ITALIC" -> Font.ITALIC
        "UNDERLINE" -> Font.UNDERLINE
        "STRIKETHRU" -> Font.STRIKETHRU
        "BOLDITALIC" -> Font.BOLDITALIC
        else -> Font.NORMAL
    }
}

private fun getStyleNameFromFont(font: Int): String? {
    return when (font) {
        Font.BOLD -> "BOLD"
        Font.ITALIC -> "ITALIC"
        Font.UNDERLINE -> "UNDERLINE"
        Font.STRIKETHRU -> "STRIKETHRU"
        Font.BOLDITALIC -> "BOLDITALIC"
        else -> "NORMAL"
    }
}

fun Fragment.showImageScaleTypeDialog(
    mPdfOptions: ImageToPDFOptions,
    forSettings: Boolean = false
) {
    val view = layoutInflater.inflate(R.layout.image_scale_type_dialog, null)
    val mSetAsDefault = view.findViewById<CheckBox>(R.id.cbSetDefault)
    mSetAsDefault.goneIf(forSettings)
    activity?.alert {
        title(R.string.image_scale_type)
        customView(view)
        positiveButton(R.string.ok) {
            val radioGroup = view!!.findViewById<RadioGroup>(R.id.scale_type)
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == R.id.aspect_ratio) mPdfOptions.imageScaleType =
                IMAGE_SCALE_TYPE_ASPECT_RATIO else mPdfOptions.imageScaleType =
                IMAGE_SCALE_TYPE_STRETCH
            if (mSetAsDefault.isChecked || forSettings) {
                mPdfOptions.preferencesService.imageScaleType = mPdfOptions.imageScaleType
            }
        }
        negativeButton(R.string.cancel)
    }?.show()
}

fun Fragment.showMasterPasswordDialog(mPdfOptions: ImageToPDFOptions) {
    val builder: MaterialDialog.Builder =
        MaterialDialog.Builder(activity!!).title(R.string.change_master_pwd)
            .positiveText(R.string.ok).negativeText(R.string.cancel)
    val materialDialog: MaterialDialog = builder.customView(R.layout.dialog_change_master_pwd, true)
        .onPositive(SingleButtonCallback { dialog1: MaterialDialog, which: DialogAction? ->
            val view = dialog1.customView
            val et = view!!.findViewById<EditText>(R.id.value)
            val value = et.text.toString()
            if (value.isNotEmpty1()) mPdfOptions.preferencesService.masterPassword =
                value else showSnackbar(R.string.invalid_entry)
        }).build()
    val view = materialDialog.customView
    val tv = view!!.findViewById<TextView>(R.id.content)
    tv.text = String.format(
        getString(R.string.current_master_pwd),
        mPdfOptions.preferencesService.masterPassword
    )
    materialDialog.show()
}

fun Fragment.deleteFiles(files: ArrayList<PDFFile>, onOkClick: () -> Unit) {
    val messageAlert: Int
    if (files.size > 1) {
        messageAlert = R.string.delete_alert_selected
    } else {
        messageAlert = R.string.delete_alert_singular
    }
    val dialogAlert = AlertDialog.Builder(activity!!).setCancelable(true)
        .setNegativeButton(R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        .setTitle(messageAlert).setPositiveButton(R.string.yes) { dialog, which ->
        onOkClick()
    }
    dialogAlert.create().show()
}

fun Activity.showFilesInfoDialog(dialogId: Int) {
    var stringId = R.string.viewfiles_rotatepages
    when (dialogId) {
        ROTATE_PAGES -> stringId = R.string.viewfiles_rotatepages
        REMOVE_PASSWORD -> stringId = R.string.viewfiles_removepassword
        ADD_PASSWORD -> stringId = R.string.viewfiles_addpassword
        ADD_WATERMARK -> stringId = R.string.viewfiles_addwatermark
    }
    MaterialDialog.Builder(this).title(R.string.app_name).content(stringId)
        .positiveText(R.string.ok).build().show()
}