package com.example.mymusicapplication.db

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "userId")
    var userId: Int,

    @ColumnInfo(name = "email")
    var email:String?,

    @ColumnInfo(name = "pwd")
    var pwd: String?,

    @ColumnInfo(name = "name")
    var name: String?,

    @ColumnInfo(name = "mobileNumber")
    var mobileNumber: String?,

    @ColumnInfo(name = "isLoggedIn")
    var isLoggedIn: Boolean
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(userId)
        parcel.writeString(email)
        parcel.writeString(pwd)
        parcel.writeString(name)
        parcel.writeString(mobileNumber)
        parcel.writeByte(if (isLoggedIn) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}