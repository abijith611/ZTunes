package com.example.mymusicapplication.db

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_data")
data class SongData (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "userId")
    var userId: String,

    @ColumnInfo(name = "songRecent")
    var recentSongList: ArrayList<Song>,

    @ColumnInfo(name = "songFrequent")
    var frequentSongList: ArrayList<Song>
    ): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        arrayListOf<Song>().apply{
            parcel.readList(this, Song::class.java.classLoader)
        },
        arrayListOf<Song>().apply{
            parcel.readList(this, Song::class.java.classLoader)
        }


    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeList(recentSongList)
        parcel.writeList(frequentSongList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SongData> {
        override fun createFromParcel(parcel: Parcel): SongData {
            return SongData(parcel)
        }

        override fun newArray(size: Int): Array<SongData?> {
            return arrayOfNulls(size)
        }
    }

}

