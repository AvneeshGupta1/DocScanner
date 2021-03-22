package com.extrastudios.docscanner.utils

import android.app.Activity
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.dagger.PreferencesService
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class SplitPDFUtils(
    private val mContext: Activity,
    private val preferencesService: PreferencesService
) {

    fun splitPDFByConfig(path: String?, splitDetail: String): ArrayList<String> {
        val splitConfig = splitDetail.replace("\\s+".toRegex(), "")
        val outputPaths = ArrayList<String>()
        val delims = ","
        val split = splitConfig.split(delims).filter { it != "" }
        val ranges = split.toTypedArray()
        // if input is invalid then return empty arraylist
        if (path == null || !isInputValid(path, ranges)) return outputPaths
        try {
            val folderPath = preferencesService.storageLocation + pdfDirectory
            val reader = PdfReader(path)
            var copy: PdfCopy
            var document: Document
            for (range in ranges) {
                var startPage: Int
                var endPage: Int
                var fileName = folderPath + FileUtils.getFileName(path)

                /*
                 * If the pdf is single page only then convert whole range into int
                 * else break the range on "-",where startpage will be substring
                 * from first letter to "-" and endpage will be from "-" till last letter.
                 *
                 */if (reader.numberOfPages > 1) {
                    if (!range.contains("-")) {
                        startPage = range.toInt()
                        document = Document()
                        fileName = fileName.replace(pdfExtension, "_$startPage$pdfExtension")
                        copy = PdfCopy(document, FileOutputStream(fileName))
                        document.open()
                        copy.addPage(copy.getImportedPage(reader, startPage))
                        document.close()
                        outputPaths.add(fileName)
                        //DatabaseHelper(mContext).insertRecord(fileName, mContext.getString(R.string.created))
                    } else {
                        startPage = range.substring(0, range.indexOf("-")).toInt()
                        endPage = range.substring(range.indexOf("-") + 1).toInt()
                        if (reader.numberOfPages == endPage - startPage + 1) {
                            mContext.showSnackbar(R.string.split_range_alert)
                        } else {
                            document = Document()
                            fileName =
                                fileName.replace(pdfExtension, "_$startPage-$endPage$pdfExtension")
                            copy = PdfCopy(document, FileOutputStream(fileName))
                            document.open()
                            for (page in startPage..endPage) {
                                copy.addPage(copy.getImportedPage(reader, page))
                            }
                            document.close()
                            //DatabaseHelper(mContext).insertRecord(fileName, mContext.getString(R.string.created))
                            outputPaths.add(fileName)
                        }
                    }
                } else {
                    mContext.showSnackbar(R.string.split_one_page_pdf_alert)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            mContext.showSnackbar(R.string.file_access_error)
        } catch (e: DocumentException) {
            e.printStackTrace()
            mContext.showSnackbar(R.string.file_access_error)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            mContext.showSnackbar(R.string.file_access_error)
        }
        return outputPaths
    }

    /**
     * checks if the user entered split ranges are valid or not
     *
     * @param path   the input pdf path
     * @param ranges string array that contain page range, can be a single integer or range separated by dash like 2-5
     * @return true if input is valid, otherwise false
     */
    private fun isInputValid(path: String, ranges: Array<String>): Boolean {
        try {
            val reader = PdfReader(path)
            val numOfPages = reader.numberOfPages
            val result = checkRangeValidity(numOfPages, ranges)
            when (result) {
                ERROR_PAGE_NUMBER -> mContext.showSnackbar(R.string.error_page_number)
                ERROR_PAGE_RANGE -> mContext.showSnackbar(R.string.error_page_range)
                ERROR_INVALID_INPUT -> mContext.showSnackbar(R.string.error_invalid_input)
                else -> return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    companion object {
        private const val NO_ERROR = 0
        private const val ERROR_PAGE_NUMBER = 1
        private const val ERROR_PAGE_RANGE = 2
        private const val ERROR_INVALID_INPUT = 3

        /**
         * checks if the user entered split ranges are valid or not
         * the returnValue is initialized with NO_ERROR
         * if no range is given, ERROR_INVALID_INPUT is returned
         * for all the given ranges, if single page (starting page) is only given then we fetch the starting page
         * if starting page is not a number then exception is caught and ERROR_INVALID_INPUT is returned
         * if the starting page is greater than number of pages or is 0 then ERROR_PAGE_NUMBER is returned
         * for hyphenated ranges, e.g 4-8, the start and end page are read
         * if the start or end page are not valid numbers then ERROR_INVALID_INPUT is returned
         * if the start and end page are out of range then ERROR_PAGE_NUMBER is returned
         * if the start page is greater than end page then the range is invalid so ERROR_PAGE_RANGE is returned
         *
         * @param numOfPages total number of pages of pdf
         * @param ranges     string array that contain page range,
         * can be a single integer or range separated by dash like 2-5
         * @return 0 if all ranges are valid
         * ERROR_PAGE_NUMBER    if range greater than max number of pages
         * ERROR_PAGE_RANGE     if range is invalid like 9-4
         * ERROR_INVALID_INPUT  if input is invalid like -3 or 3--4 or 3,,4
         */
        fun checkRangeValidity(numOfPages: Int, ranges: Array<String>): Int {
            var startPage: Int
            var endPage: Int
            var returnValue = NO_ERROR
            if (ranges.isEmpty()) returnValue = ERROR_INVALID_INPUT else {
                for (range in ranges) {
                    if (!range.contains("-")) {
                        try {
                            startPage = range.toInt()
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                            returnValue = ERROR_INVALID_INPUT
                            break
                        }
                        if (startPage > numOfPages || startPage == 0) {
                            returnValue = ERROR_PAGE_NUMBER
                            break
                        }
                    } else {
                        try {
                            startPage = range.substring(0, range.indexOf("-")).toInt()
                            endPage = range.substring(range.indexOf("-") + 1).toInt()
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                            returnValue = ERROR_INVALID_INPUT
                            break
                        } catch (e: StringIndexOutOfBoundsException) {
                            e.printStackTrace()
                            returnValue = ERROR_INVALID_INPUT
                            break
                        }
                        if (startPage > numOfPages || endPage > numOfPages || startPage == 0 || endPage == 0) {
                            returnValue = ERROR_PAGE_NUMBER
                            break
                        } else if (startPage >= endPage) {
                            returnValue = ERROR_PAGE_RANGE
                            break
                        }
                    }
                }
            }
            return returnValue
        }
    }

}