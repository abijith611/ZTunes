package com.example.mymusicapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.R
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.fragments.DetailFragment
import com.example.mymusicapplication.notification.NotificationHandler
import kotlinx.coroutines.runBlocking

class NotificationReceiver: BroadcastReceiver() {
    companion object{
        var STATE = MutableLiveData<String>()
       var ACTION_NEXT = "NEXT"
        var ACTION_PREV = "PREVIOUS"
        var ACTION_PLAY = "PLAY"
        var ACTION_FAV = "FAV"
        var ACTION_DISMISS = "DISMISS"
    }

     private val mService = MusicService()
    override fun onReceive(context: Context?, intent: Intent?) {
        DetailFragment()
        if(intent?.action!=null){
            when(intent.action){
                ACTION_PLAY ->{
                    mService.play(context)
                    STATE.value = ACTION_PLAY

                }
                ACTION_PREV ->{
                    mService.prev(context)
                    STATE.value = ACTION_PREV

                }
                ACTION_NEXT ->{
                    mService.next(context)
                    STATE.value = ACTION_NEXT
                }
                ACTION_FAV ->{
                    if(context!=null){
                        val dao = SongDatabase.getInstance(context).songDao
                        val repository = SongRepository(dao)
                        val user = runBlocking { repository.getUsers() }.last()
                        val fav = runBlocking { user.email?.let { repository.getFavForUser(it) } }
                        if (MusicService.currentSongInstance in fav!![0].favList) {
                            fav[0].favList.remove(MusicService.currentSongInstance!!)
                            runBlocking { repository.updateFavourite(fav[0]) }
                        } else {
                            fav[0].favList.add(MusicService.currentSongInstance!!)
                            runBlocking { repository.updateFavourite(fav[0]) }
                        }
                        if(MusicService.STATE.value == "PLAY")
                            NotificationHandler(context).showNotification(R.drawable.ic_simple_pause_, 1F)
                        else if(MusicService.STATE.value == "PAUSE")
                            NotificationHandler(context).showNotification(R.drawable.ic_simple_play_, 0F)
                    }
                    STATE.value = ACTION_FAV
                }
            }
        }
    }
}