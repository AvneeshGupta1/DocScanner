package com.extrastudios.docscanner.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.extrastudios.docscanner.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.itextpdf.text.pdf.PdfReader
import es.dmoral.toasty.Toasty
import org.jetbrains.anko.alert
import org.jetbrains.anko.newTask
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("android:src")
fun ImageView.setImageDrawable(id: Int) {
    Glide.with(context).load(id).apply(requestOptions).into(this)
}

fun Activity.showSettingsDialog() {
    alert(R.string.message_permission, R.string.message_need_permission) {
        positiveButton(R.string.title_go_to_setting) {
            dialog?.cancel()
            openSettings()
        }
    }.show()
}

fun showHideSheet(sheetBehavior: BottomSheetBehavior<*>) {
    if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
    } else {
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
    }
}

fun closeSheet(sheetBehavior: BottomSheetBehavior<*>) {
    if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}

fun File.isPasswordProtected(): Boolean {
    var isEncrypted: Boolean
    var pdfReader: PdfReader? = null
    try {
        pdfReader = PdfReader(absolutePath)
        isEncrypted = pdfReader.isEncrypted
    } catch (e: IOException) {
        isEncrypted = true
    } finally {
        pdfReader?.close()
    }
    return isEncrypted
}

fun Fragment.openAppSettings(requestCode: Int) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", activity!!.packageName, null)
    )
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    startActivityForResult(intent, requestCode)
}

fun Fragment.hasWritePermission(): Boolean {
    val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    val res = activity!!.checkCallingOrSelfPermission(permission)
    return res == PackageManager.PERMISSION_GRANTED
}

fun Fragment.hasCameraPermission(): Boolean {
    val permission = android.Manifest.permission.CAMERA
    val res = activity!!.checkCallingOrSelfPermission(permission)
    return res == PackageManager.PERMISSION_GRANTED
}


fun String.getFileName(): String? {
    val index = this.lastIndexOf(PATH_SEPERATOR)
    return if (index < this.length) this.substring(index + 1) else null
}


fun RecyclerView.setDivider() {
    val divider = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
    val drawable = ContextCompat.getDrawable(this.context, R.drawable.recycler_view_divider)
    drawable?.let {
        divider.setDrawable(it)
        addItemDecoration(divider)
    }
}

fun String.isEmptyWithTrim(): Boolean {
    return trim().isEmpty()
}

fun Activity.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivityForResult(intent, REQUEST_APP_SETTINGS)
}

fun Activity.getUuid(): String {
    return UUID.randomUUID().toString()
}

fun Fragment.getUuid(): String {
    return UUID.randomUUID().toString()
}


fun Context.currentDateTime(): String {
    val msTime = System.currentTimeMillis()
    val curDateTime = Date(msTime)
    val formatter = SimpleDateFormat(CALENDER_FORMAT)
    return formatter.format(curDateTime)
}

fun Fragment.currentDateTime(): String {
    val msTime = System.currentTimeMillis()
    val curDateTime = Date(msTime)
    val formatter = SimpleDateFormat(CALENDER_FORMAT)
    return formatter.format(curDateTime)
}


fun Activity.isNetworkAvailable(): Boolean {
    val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val networkInfo = connMgr!!.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun Context.hasLocationPermission(): Boolean {
    val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    val res = checkCallingOrSelfPermission(permission)
    return res == PackageManager.PERMISSION_GRANTED
}

fun Context.hasStoragePermission(): Boolean {
    val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    val res = checkCallingOrSelfPermission(permission)
    return res == PackageManager.PERMISSION_GRANTED
}

fun Context.hasCameraPermission(): Boolean {
    val permission = android.Manifest.permission.CAMERA
    val res = checkCallingOrSelfPermission(permission)
    return res == PackageManager.PERMISSION_GRANTED
}

fun Context.hasAudioPermission(): Boolean {
    val permission = android.Manifest.permission.RECORD_AUDIO
    val res = checkCallingOrSelfPermission(permission)
    return res == PackageManager.PERMISSION_GRANTED
}

fun File.getFormattedSize(): String? {
    return String.format("%.2f MB", length().toDouble() / (1024 * 1024))
}

var CheckBox.check: Boolean
    get() = isChecked
    set(value) {
        this.isChecked = value
        jumpDrawablesToCurrentState()
    }

var RadioButton.check: Boolean
    get() = isChecked
    set(value) {
        this.isChecked = value
        jumpDrawablesToCurrentState()
    }


var Context.doneColor: Int
    get() = ContextCompat.getColor(this, R.color.login_button_bg_color)
    set(value) {
    }

val Context.doneBitmap: Bitmap
    get() = BitmapFactory.decodeResource(resources, R.drawable.ic_done)

fun Context.getInstallTime(): String {
    val pm = packageManager
    try {
        val pi = pm.getPackageInfo(packageName, 0)
        val sdf = SimpleDateFormat(APP_INSTALL_FORMAT, Locale.getDefault())
        return sdf.format(Date(pi.lastUpdateTime))

    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return "Error"
}

fun HorizontalScrollView.scrollToView(view: View) {
    val childOffset = Point()
    getDeepChildOffsetHorizontal(this, view.parent, view, childOffset)
    smoothScrollTo(0, childOffset.y)
}

private fun getDeepChildOffsetHorizontal(
    mainParent: ViewGroup,
    parent: ViewParent,
    child: View,
    accumulatedOffset: Point
) {
    val parentGroup = parent as ViewGroup
    accumulatedOffset.x += child.left
    accumulatedOffset.y += child.top - 50
    if (parentGroup == mainParent) {
        return
    }
    getDeepChildOffset(mainParent, parentGroup.parent, parentGroup, accumulatedOffset)
}


fun ScrollView.scrollToView(view: View) {
    val childOffset = Point()
    getDeepChildOffset(this, view.parent, view, childOffset)
    smoothScrollTo(0, childOffset.y)
}

private fun getDeepChildOffset(
    mainParent: ViewGroup,
    parent: ViewParent,
    child: View,
    accumulatedOffset: Point
) {
    val parentGroup = parent as ViewGroup
    accumulatedOffset.x += child.left
    accumulatedOffset.y += child.top - 50
    if (parentGroup == mainParent) {
        return
    }
    getDeepChildOffset(mainParent, parentGroup.parent, parentGroup, accumulatedOffset)
}

fun Fragment.isNetworkAvailable(): Boolean {
    val connMgr = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val networkInfo = connMgr!!.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun Activity.isProAppInstall(): Boolean {
    return try {
        val pn = packageName
        packageManager.getPackageInfo("com.katyayini.hidefiles.pro", 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}


fun Activity.getPro() {
    val uri = Uri.parse("market://details?id=com.extrastudios.camscanner.pro")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    try {
        startActivity(goToMarket.newTask())
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=com.extrastudios.camscanner.pro")
            ).newTask()
        )
    }
}

fun Context.toast(messageId: Int, duration: Int = Toast.LENGTH_LONG) {
    Toasty.normal(this, getString(messageId), duration).show()
}

fun Activity.getUriFromFile(file: File): Uri {
    if (Build.VERSION.SDK_INT < 24) {
        return Uri.fromFile(file)
    } else {
        return FileProvider.getUriForFile(this, "$packageName.provider", file)
    }
}

fun Context.toastError(messageId: Int, duration: Int = Toast.LENGTH_LONG) {
    Toasty.error(this, getString(messageId), duration, true).show()
}

fun Fragment.toastError(messageId: Int, duration: Int = Toast.LENGTH_LONG) {
    Toasty.error(activity!!, getString(messageId), duration, true).show()
}

fun Context.toastError(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toasty.error(this, message, duration, true).show()
}

fun Fragment.toastError(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toasty.error(activity!!, message, duration, true).show()
}

fun Context.toastInfo(message: Int, duration: Int = Toast.LENGTH_LONG) {
    Toasty.info(this, getString(message), duration, true).show()
}

fun Fragment.toastInfo(message: Int, duration: Int = Toast.LENGTH_LONG) {
    Toasty.info(activity!!, getString(message), duration, true).show()
}

fun Context.toastInfo(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toasty.info(this, message, duration, true).show()
}

fun androidx.fragment.app.Fragment.toastInfo(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toasty.info(activity!!, message, duration, true).show()
}

fun Context.toastSuccess(messageId: Int, duration: Int = Toast.LENGTH_LONG) {
    Toasty.success(this, getString(messageId, true), duration).show()
}

fun androidx.fragment.app.Fragment.toastSuccess(messageId: Int, duration: Int = Toast.LENGTH_LONG) {
    Toasty.success(activity!!, getString(messageId, true), duration).show()
}

fun Context.toastSuccess(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toasty.success(this, message, duration, true).show()
}

fun androidx.fragment.app.Fragment.toastSuccess(
    message: String,
    duration: Int = Toast.LENGTH_LONG
) {
    Toasty.success(activity!!, message, duration, true).show()
}

fun Context.toast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toasty.normal(this, message, duration).show()
}


fun androidx.fragment.app.Fragment.toast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toasty.normal(activity!!, message, duration).show()
}

fun CharSequence.fromHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this.toString(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this.toString())
    }
}

fun String.fromHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

fun String.isNumberValid(): Boolean {
    if (isEmpty() || length < 10) return false
    if (startsWith("+91")) {
        return substring(3, length).length == 10
    }
    return (length == 10 && (startsWith("0") || startsWith("5") || startsWith("6") || startsWith("7") || startsWith(
        "8"
    ) || startsWith("9")))
}

fun String.getValidPhoneNumber(): String {
    if (length >= 10) {
        if (startsWith("+91")) {
            return "+91" + substring(3, length)
        }
        return "+91" + substring(length - 10, length)
    }
    return this
}

fun String.isEmailValid(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches();
}

fun String.isValidPinCode(): Boolean {
    return length == 6
}

infix fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun View.visibleIf(condition: Boolean, otherwise: Int = View.GONE) {
    visibility = if (condition) View.VISIBLE else otherwise
}

infix fun View.goneIf(condition: Boolean) {
    visibility = if (condition) View.GONE else View.VISIBLE
}

fun Activity.vibrate() {
    val v: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    v.vibrate(300)
}

fun String.isFileExist(): Boolean {
    val path: String = defaultStorageLocation + this
    val file = File(path)
    return file.exists()
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun Activity.hideKeyboard() {
    if (currentFocus == null) View(this).hideKeyboard() else currentFocus!!.hideKeyboard()
}

fun Fragment.hideKeyboard() {
    val view: View = activity!!.findViewById(android.R.id.content)
    if (view != null) {
        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }
}

fun View.actionbarHeight(): Int {
    val tv = TypedValue()
    context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
    return resources.getDimensionPixelSize(tv.resourceId)
}

fun <T : ViewDataBinding> ViewGroup.inflate(layoutId: Int): T {
    return DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, this, false)
}

fun RecyclerView.ViewHolder.loadImage(target: ImageView, path: String) {
    Glide.with(target.context).load(path).apply(requestOptions).into(target)
}

fun EditText.showKeyboard() {
    if (requestFocus()) {
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, 0)
    }
}

fun String.dateInFormat(format: String): Date? {
    val dateFormat = SimpleDateFormat(format, Locale.US)
    var parsedDate: Date? = null
    try {
        parsedDate = dateFormat.parse(this)
    } catch (ignored: ParseException) {
        ignored.printStackTrace()
    }
    return parsedDate
}

fun Date.isSame(to: Date): Boolean {
    val sdf = SimpleDateFormat("yyyMMdd", Locale.getDefault())
    return sdf.format(this) == sdf.format(to)
}

fun getClickableSpan(color: Int, action: (view: View) -> Unit): ClickableSpan {
    return object : ClickableSpan() {
        override fun onClick(view: View) {
            action
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = color
        }
    }
}

fun Editable.replaceAll(newValue: String) {
    replace(0, length, newValue)
}

fun Boolean.toInt() = if (this) 1 else 0


fun Activity.shareApplication() {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.rate_us_text))
    openMailIntent(intent, this)
}

private fun openMailIntent(intent: Intent?, mContext: Activity) {
    try {
        mContext.startActivity(
            Intent.createChooser(
                intent,
                mContext.getString(R.string.share_chooser)
            )
        )
    } catch (ex: ActivityNotFoundException) {
        mContext.showSnackbar(R.string.snackbar_no_share_app)
    }
}


fun Activity.openWebPage(url: String?) {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (intent.resolveActivity(packageManager) != null) startActivity(intent)
}

fun Context.share(text: String, subject: String = "") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    startActivity(Intent.createChooser(intent, null).newTask())
}


