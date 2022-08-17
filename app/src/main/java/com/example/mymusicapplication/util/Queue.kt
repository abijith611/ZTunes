package com.example.mymusicapplication.util

import com.example.mymusicapplication.db.Song

class Queue {
    companion object{

        var queue = ArrayList<Song>()
        var played = ArrayList<Song>()

        fun getCount(currentSong:Song): Int{
            var count = 0
            for(song in queue){
                if(song == currentSong){
                    count++
                }
            }
            return count
        }

    }
}