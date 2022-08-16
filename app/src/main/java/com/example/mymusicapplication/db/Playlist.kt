package com.example.mymusicapplication.db

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "playlist_table")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "playlistId")
    var id: Int,

    @ColumnInfo(name = "playlistName")
    var name: String?,

    @ColumnInfo(name = "userId")
    var userId: String?,

    @ColumnInfo(name = "playlistArray")
    var playlist: ArrayList<Song>

):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        arrayListOf<Song>().apply {
            parcel.readList(this, Song::class.java.classLoader)
        }




    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(userId)
        parcel.writeList(playlist)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Playlist> {
        override fun createFromParcel(parcel: Parcel): Playlist {
            return Playlist(parcel)
        }

        override fun newArray(size: Int): Array<Playlist?> {
            return arrayOfNulls(size)
        }
    }

}

class SongTypeConvertor{
    @TypeConverter
    fun fromSong(value: String?): ArrayList<Song>{
        val listType = object: TypeToken<ArrayList<Song>>(){}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<Song>): String{
        return  Gson().toJson(list)
    }
}