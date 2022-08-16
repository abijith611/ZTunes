package com.example.mymusicapplication.db

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "favourite_table")
data class Favourite(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "userId")
    var userId: String,

    @ColumnInfo(name = "favArray")
    var favList: ArrayList<Song>

):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        arrayListOf<Song>().apply {
            parcel.readList(this, Song::class.java.classLoader)
        }

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeList(favList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Favourite> {
        override fun createFromParcel(parcel: Parcel): Favourite {
            return Favourite(parcel)
        }

        override fun newArray(size: Int): Array<Favourite?> {
            return arrayOfNulls(size)
        }
    }


}

