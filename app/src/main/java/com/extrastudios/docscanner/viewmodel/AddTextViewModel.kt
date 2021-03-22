package com.extrastudios.docscanner.viewmodel

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.extrastudios.docscanner.DocScannerApplication
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.dagger.PreferencesService
import com.extrastudios.docscanner.model.CommonItem
import com.extrastudios.docscanner.model.HomeItem
import com.extrastudios.docscanner.utils.*
import com.itextpdf.text.Document
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import org.jetbrains.anko.doAsync
import java.io.*
import java.io.FileReader
import javax.inject.Inject


class AddTextViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var preferencesService: PreferencesService

    init {
        (application as DocScannerApplication).getAppComponent().inject(this)
    }

    fun getAddTextItems(
        context: Fragment,
        textToPDFOptions: TextToPDFOptions
    ): MutableLiveData<ArrayList<CommonItem>> {
        val mHomeItemList = MutableLiveData<ArrayList<CommonItem>>()
        val homeItems = arrayListOf<CommonItem>()

        homeItems.add(
            HomeItem(
                FONT_SIZE,
                R.drawable.ic_font_black_24dp,
                context.getString(R.string.font_size_value_def, textToPDFOptions.fontSize)
            )
        )
        homeItems.add(
            HomeItem(
                FONT_FAMILY,
                R.drawable.ic_font_family_24dp,
                context.getString(R.string.font_family_value_def, textToPDFOptions.fontFamily)
            )
        )
        mHomeItemList.postValue(homeItems)

        return mHomeItemList
    }

    fun addText(
        fileName: String,
        mTextPath: String,
        mPdfpath: String,
        textToPDFOptions: TextToPDFOptions
    ): MutableLiveData<String> {
        val response = MutableLiveData<String>()
        val mPath = preferencesService.storageLocation + pdfDirectory + fileName + pdfExtension
        doAsync {
            try {
                val text = StringBuilder()
                val br = BufferedReader(FileReader(mTextPath))
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    text.append(line)
                    text.append('\n')
                }
                br.close()
                val fos: OutputStream = FileOutputStream(File(mPath))
                val pdfReader = PdfReader(mPdfpath)
                val document = Document(pdfReader.getPageSize(1))
                val pdfWriter = PdfWriter.getInstance(document, fos)
                document.open()
                val cb = pdfWriter.directContent
                for (i in 1..pdfReader.numberOfPages) {
                    val page = pdfWriter.getImportedPage(pdfReader, i)
                    document.newPage()
                    cb.addTemplate(page, 0f, 0f)
                }
                document.pageSize = pdfReader.getPageSize(1)
                document.newPage()
                document.add(
                    Paragraph(
                        Paragraph(
                            text.toString(),
                            FontFactory.getFont(
                                textToPDFOptions.fontFamily.name,
                                textToPDFOptions.fontSize.toFloat()
                            )
                        )
                    )
                )
                document.close()
                response.postValue(mPath)
            } catch (e: Exception) {
                response.postValue("")
            }
        }
        return response
    }


}