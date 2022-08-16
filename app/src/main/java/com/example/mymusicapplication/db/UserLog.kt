package com.example.mymusicapplication.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDateTime

@Entity(tableName = "user_log")
data class UserLog(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="entryId")
    var entryId:Int,

    @ColumnInfo(name = "email")
    var email:String,

    @ColumnInfo(name = "isLoggedIn")
    var isLoggedIn: Boolean,

    @ColumnInfo(name = "time")
    var loginTime: LocalDateTime,

)

class LocalDateTimeConverter {
    @TypeConverter
    fun toDate(dateString: String?): LocalDateTime? {
        return if (dateString == null) {
            null
        } else {
            LocalDateTime.parse(dateString)
        }
    }

    @TypeConverter
    fun toDateString(date: LocalDateTime?): String? {
        return date?.toString()
    }
}