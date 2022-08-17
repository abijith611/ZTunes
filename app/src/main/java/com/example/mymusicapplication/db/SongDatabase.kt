package com.example.mymusicapplication.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Song::class, Playlist::class, User::class, Favourite::class, SongData::class, UserLog::class], version = 1)
@TypeConverters(SongTypeConvertor::class, LocalDateTimeConverter::class)
abstract class SongDatabase: RoomDatabase() {
    abstract val songDao: SongDao

    companion object{

        fun getInstance(context: Context): SongDatabase{
            synchronized(this){
                var instance: SongDatabase? = null
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SongDatabase::class.java,
                        "song_table"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                }
                return instance
            }
        }
    }
}