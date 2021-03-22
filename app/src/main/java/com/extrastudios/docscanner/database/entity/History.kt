package com.extrastudios.docscanner.database.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.extrastudios.docscanner.R
import com.extrastudios.docscanner.utils.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "history", indices = [Index(value = ["file_path"], unique = true)])
class History(var icon: Int, var file_path: String = "", var date: Long, var operationType: Int) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @Ignore
    var name = File(file_path).name

    @Ignore
    var formatedDate = getFormattedDate()

    fun getFormattedDate(): String {
        try {
            val simpleDateFormat = SimpleDateFormat("EEE, MMM dd 'at' HH:mm", Locale.getDefault())
            return simpleDateFormat.format(date)
        } catch (e: Exception) {

        }
        return ""
    }

    @Ignore
    var operationText = getOperationDisplayText()
    private fun getOperationDisplayText(): Int {
        when (operationType) {
            OPERATION_PRINTED -> {
                return R.string.printed
            }
            OPERATION_DELETED -> {
                return R.string.deleted
            }
            OPERATION_RENAME -> {
                return R.string.renamed
            }
            OPERATION_ROTATED -> {
                return R.string.rotated
            }
            OPERATION_ENCRYPTED -> {
                return R.string.encrypted
            }
            OPERATION_DECRYPTED -> {
                return R.string.decrypted
            }
            else -> {
                return R.string.created
            }
        }
    }
}
