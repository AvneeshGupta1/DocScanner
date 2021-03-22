package com.extrastudios.docscanner.utils

import java.io.File
import java.util.Collections.sort

class ImageSortUtils {

    fun performSortOperation(option: Int, images: ArrayList<String>) {
        require(!(option < 0 || option > 3)) { "Invalid sort option. " + "Sort option must be in [0; 3] range!" }
        when (option) {
            NAME_ASC -> sortByNameAsc(images)
            NAME_DESC -> sortByNameDesc(images)
            DATE_ASC -> sortByDateAsc(images)
            DATE_DESC -> sortByDateDesc(images)
        }
    }

    private fun sortByNameAsc(imagePaths: List<String>) {
        sort(imagePaths) { path1: String, path2: String ->
            path1.substring(path1.lastIndexOf('/'))
                .compareTo(path2.substring(path2.lastIndexOf('/')))
        }
    }

    /**
     * Sorts the given list in descending alphabetical  order
     *
     * @param imagePaths list of image paths to be sorted
     */
    private fun sortByNameDesc(imagePaths: List<String>) {
        sort(imagePaths) { path1: String, path2: String ->
            path2.substring(path2.lastIndexOf('/'))
                .compareTo(path1.substring(path1.lastIndexOf('/')))
        }
    }

    /**
     * Sorts the given list in ascending  date  order
     *
     * @param imagePaths list of image paths to be sorted
     */
    private fun sortByDateAsc(imagePaths: List<String>) {
        sort(imagePaths) { path1: String?, path2: String? ->
            File(path2).lastModified().compareTo(File(path1).lastModified())
        }
    }

    /**
     * Sorts the given list in descending date  order
     *
     * @param imagePaths list of image paths to be sorted
     */
    private fun sortByDateDesc(imagePaths: List<String>) {
        sort(imagePaths) { path1: String?, path2: String? ->
            File(path1).lastModified().compareTo(File(path2).lastModified())
        }
    }

    private object SingletonHolder {
        val INSTANCE = ImageSortUtils()
    }

    companion object {
        private const val NAME_ASC = 0
        private const val NAME_DESC = 1
        private const val DATE_ASC = 2
        private const val DATE_DESC = 3
        val instance: ImageSortUtils
            get() = SingletonHolder.INSTANCE
    }
}