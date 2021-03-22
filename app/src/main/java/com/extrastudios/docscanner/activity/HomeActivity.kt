package com.extrastudios.docscanner.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.extrastudios.docscanner.BuildConfig
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.fragment.*
import com.extrastudios.docscanner.utils.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File


class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            toolbar,
            R.string.app_name,
            R.string.app_name
        ) {
            override fun onDrawerClosed(view: View) {
                invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                invalidateOptionsMenu()
            }
        }
        mDrawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        val menu = nav_view.menu.getItem(0)
        onNavigationItemSelected(menu)
        nav_view.menu.findItem(R.id.nav_remove_ads).isVisible = BuildConfig.FREE_VERSION
    }


    fun updateDownloadCompleted() {
        Snackbar.make(activity_main_layout, R.string.update_downloaded, Snackbar.LENGTH_INDEFINITE)
            .apply {
                setAction(R.string.restart) { appUpdateManager?.completeUpdate() }
                show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        makeAndClearTemp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        nav_view.setCheckedItem(item.itemId)
        onItemSelected(item.itemId)
        return false
    }


    fun onItemSelected(id: Int): Boolean {
        var fragment: Fragment? = HomeFragment()
        when (id) {
            R.id.nav_home -> {
                setToolbarTitle(R.string.app_name)
                fragment = HomeFragment()
                nav_view.setCheckedItem(R.id.nav_home)
            }
            R.id.nav_camera, IMAGE_TO_PDF -> {
                setToolbarTitle(R.string.images_to_pdf)
                fragment = ImageToPdfFragment()
                nav_view.setCheckedItem(R.id.nav_camera)
            }
            R.id.nav_text_to_pdf, TEXT_TO_PDF -> {
                setToolbarTitle(R.string.text_to_pdf)
                fragment = TextToPdfFragment()
                nav_view.setCheckedItem(R.id.nav_text_to_pdf)
            }
            R.id.nav_qrcode, QR_CODE_BAR_CODE -> {
                setToolbarTitle(R.string.qr_barcode_pdf)
                fragment = QRBarFragment()
                nav_view.setCheckedItem(R.id.nav_qrcode)
            }
            R.id.nav_excel_to_pdf, EXCEL_TO_PDF -> {
                setToolbarTitle(R.string.excel_to_pdf)
                fragment = ExcelToPdfFragment()
                nav_view.setCheckedItem(R.id.nav_excel_to_pdf)
            }
            R.id.nav_gallery, VIEW_FILES -> {
                setToolbarTitle(R.string.viewFiles)
                nav_view.setCheckedItem(R.id.nav_gallery)
                fragment = ViewFilesFragment()
            }
            R.id.nav_history, HISTORY -> {
                setToolbarTitle(R.string.history)
                nav_view.setCheckedItem(R.id.nav_history)
                fragment = HistoryFragment()
            }
            R.id.nav_add_password, ADD_PASSWORD -> {
                setToolbarTitle(R.string.add_password)
                nav_view.setCheckedItem(R.id.nav_add_password)
                val bundle = Bundle()
                bundle.putInt(ACTION_TYPE, ACTION_ADD_PWD)
                fragment = AddRemovePasswordFragment()
                fragment.arguments = bundle
            }
            R.id.nav_remove_password, REMOVE_PASSWORD -> {
                setToolbarTitle(R.string.remove_password)
                nav_view.setCheckedItem(R.id.nav_remove_password)
                val bundle = Bundle()
                bundle.putInt(ACTION_TYPE, ACTION_REMOVE_PWD)
                fragment = AddRemovePasswordFragment()
                fragment.arguments = bundle
            }
            R.id.nav_add_text, ADD_TEXT -> {
                setToolbarTitle(R.string.add_text)
                nav_view.setCheckedItem(R.id.nav_add_text)
                fragment = AddTextFragment()
            }
            R.id.nav_add_images, ADD_IMAGES -> {
                setToolbarTitle(R.string.add_images)
                nav_view.setCheckedItem(R.id.nav_add_images)
                fragment = AddImageFragment()
            }
            R.id.nav_add_watermark, ADD_WATERMARK -> {
                setToolbarTitle(R.string.add_watermark)
                nav_view.setCheckedItem(R.id.nav_add_watermark)
                fragment = ViewFilesFragment()
                val bundle = Bundle()
                bundle.putInt(BUNDLE_DATA, ADD_WATERMARK)
                fragment.setArguments(bundle)
            }
            R.id.nav_rotate_pages, ROTATE_PAGES -> {
                setToolbarTitle(R.string.rotate_pages)
                nav_view.setCheckedItem(R.id.nav_rotate_pages)
                fragment = ViewFilesFragment()
                val bundle = Bundle()
                bundle.putInt(BUNDLE_DATA, ROTATE_PAGES)
                fragment.setArguments(bundle)
            }
            R.id.nav_merge, MERGE_PDF -> {
                setToolbarTitle(R.string.merge_pdf)
                nav_view.setCheckedItem(R.id.nav_merge)
                fragment = MergeFilesFragment()
            }
            R.id.nav_split, SPLIT_PDF -> {
                setToolbarTitle(R.string.split_pdf)
                nav_view.setCheckedItem(R.id.nav_split)
                fragment = SplitPdfFragment()
            }
            R.id.nav_remove_duplicate_pages, REMOVE_DUPLICATE_PAGES -> {
                setToolbarTitle(R.string.remove_duplicate_pages)
                nav_view.setCheckedItem(R.id.nav_remove_duplicate_pages)
                fragment = RemoveDuplicatePagesFragment()
            }
            R.id.nav_compress_pdf, COMPRESS_PDF -> {
                setToolbarTitle(R.string.compress_pdf)
                nav_view.setCheckedItem(R.id.nav_compress_pdf)
                fragment = RemovePagesFragment()
                val bundle = Bundle()
                bundle.putInt(BUNDLE_DATA, COMPRESS_PDF)
                fragment.setArguments(bundle)
            }

            R.id.nav_invert_pdf, INVERT_PDF -> {
                setToolbarTitle(R.string.invert_pdf)
                nav_view.setCheckedItem(R.id.nav_invert_pdf)
                fragment = InvertPdfFragment()
            }
            R.id.nav_text_extract, EXTRACT_TEXT -> {
                setToolbarTitle(R.string.extract_text)
                nav_view.setCheckedItem(R.id.nav_text_extract)
                fragment = ExtractTextFragment()
            }
            R.id.nav_remove_pages, REMOVE_PAGES -> {
                setToolbarTitle(R.string.remove_pages)
                nav_view.setCheckedItem(R.id.nav_remove_pages)
                val bundle = Bundle()
                fragment = RemovePagesFragment()
                bundle.putInt(BUNDLE_DATA, REMOVE_PAGES)
                fragment.setArguments(bundle)
            }
            R.id.nav_rearrange_pages, REORDER_PAGES -> {
                setToolbarTitle(R.string.reorder_pages)
                nav_view.setCheckedItem(R.id.nav_rearrange_pages)
                val bundle = Bundle()
                fragment = RemovePagesFragment()
                bundle.putInt(BUNDLE_DATA, REORDER_PAGES)
                fragment.setArguments(bundle)

            }
            R.id.nav_extract_images, EXTRACT_IMAGES -> {
                setToolbarTitle(R.string.extract_images)
                nav_view.setCheckedItem(R.id.nav_extract_images)
                val bundle = Bundle()
                fragment = PdfToImageFragment()
                bundle.putInt(BUNDLE_DATA, EXTRACT_IMAGES)
                fragment.setArguments(bundle)
            }
            R.id.nav_pdf_to_images, PDF_TO_IMAGES -> {
                setToolbarTitle(R.string.pdf_to_images)
                nav_view.setCheckedItem(R.id.nav_pdf_to_images)
                val bundle = Bundle()
                fragment = PdfToImageFragment()
                bundle.putInt(BUNDLE_DATA, PDF_TO_IMAGES)
                fragment.setArguments(bundle)
            }

            R.id.nav_zip_to_pdf, ZIP_TO_PDF -> {
                setToolbarTitle(R.string.zip_to_pdf)
                nav_view.setCheckedItem(R.id.nav_zip_to_pdf)
                fragment = ZipToPdfFragment()
            }
            R.id.nav_faq -> {
                setToolbarTitle(R.string.faqs)
                nav_view.setCheckedItem(R.id.nav_faq)
                fragment = FAQFragment()
            }
            R.id.nav_settings -> {
                setToolbarTitle(R.string.settings)
                fragment = SettingsFragment()
            }

            R.id.nav_remove_ads -> {
                getPro()
                nav_view.setCheckedItem(R.id.nav_home)
            }

            R.id.nav_rateus -> {
                openWebPage("https://play.google.com/store/apps/details?id=$packageName")
                nav_view.setCheckedItem(R.id.nav_home)
            }
            R.id.nav_share -> {
                shareApplication()
                nav_view.setCheckedItem(R.id.nav_home)
            }
        }
        supportFragmentManager.beginTransaction().replace(R.id.content, fragment!!).commit()
        overridePendingTransition(R.anim.enter, R.anim.exit)
        closeDrawer()
        return false
    }

    // do not delete this
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun setToolbarTitle(id: Int) {
        supportActionBar?.setTitle(id)
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment: Fragment? = supportFragmentManager.findFragmentById(R.id.content)
            if (currentFragment is HomeFragment) {
                if (!isNativeAdsLoaded || BuildConfig.DEBUG || !BuildConfig.FREE_VERSION) {
                    if (doubleBackToExitPressedOnce) {
                        super.onBackPressed()
                        return
                    }
                    this.doubleBackToExitPressedOnce = true
                    toastInfo(R.string.toast_press_again_to_exit)
                    Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                } else showExitPopup()
            } else {
                val shouldExit: Boolean = handleBackPressed()
                if (!shouldExit) onItemSelected(R.id.nav_home)
            }
        }
    }

    private fun handleBackPressed(): Boolean {
        val currentFragment: Fragment = supportFragmentManager.findFragmentById(R.id.content)!!
        return handleFragmentBottomSheetBehavior(currentFragment)
    }

    private fun handleFragmentBottomSheetBehavior(fragment: Fragment): Boolean {
        var bottomSheetBehaviour = false
        if (fragment is ExcelToPdfFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        }
        if (fragment is AddRemovePasswordFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        }
        if (fragment is AddTextFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        }
        if (fragment is AddImageFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        }
        if (fragment is InvertPdfFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        } else if (fragment is MergeFilesFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        } else if (fragment is RemoveDuplicatePagesFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        } else if (fragment is RemovePagesFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        } else if (fragment is AddImageFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        } else if (fragment is PdfToImageFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        } else if (fragment is SplitPdfFragment) {
            bottomSheetBehaviour = fragment.checkSheetBehaviour()
            if (bottomSheetBehaviour) fragment.closeBottomSheet()
        }
        return bottomSheetBehaviour
    }

    fun convertImagesToPdf(imageUris: ArrayList<Uri>) {
        setToolbarTitle(R.string.images_to_pdf)
        val fragment: Fragment = ImageToPdfFragment()
        val bundle = Bundle()
        bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.content, fragment).commit()
    }

    private fun closeDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
            return
        }
    }

    fun makeAndClearTemp() {
        val dest = preferencesService.storageLocation + pdfDirectory + tempDirectory
        val folder = File(dest)
        val result = folder.mkdir()
        if (result && folder.isDirectory) {
            val children = folder.list()
            for (child in children) {
                File(folder, child).delete()
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_home
    }
}