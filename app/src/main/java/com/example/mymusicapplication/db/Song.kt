package com.example.mymusicapplication.db

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "song_table")
data class Song(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "songId")
    var id: Int,

    @ColumnInfo(name = "songTitle")
    var title: String?,

    @ColumnInfo(name = "songAlbum")
    var album: String?,

    @ColumnInfo(name = "songArtist")
    var artist: String?,

    @ColumnInfo(name = "songMedia")
    var song: Int,

    @ColumnInfo(name = "songImg")
    var img: Int,

    @ColumnInfo(name = "genre")
    var genre: String?

): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeString(title)
        dest?.writeString(album)
        dest?.writeString(artist)
        dest?.writeInt(song)
        dest?.writeInt(img)
        dest?.writeString(genre)

    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }

}
