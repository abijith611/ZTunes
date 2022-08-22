package com.example.mymusicapplication.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.example.mymusicapplication.R
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.notification.NotificationHandler
import com.example.mymusicapplication.util.Queue


class MusicService: Service(),LifecycleObserver {

    companion object{
        var currentPlaylist: String? = null
        var songChanged = MutableLiveData(false)
        var songState = MutableLiveData<String>()
        var STATE = MutableLiveData<String>()
        var firstSongPlayed = MutableLiveData(false)
        var isShuffleOn = MutableLiveData(false)
        var isLoopOn = MutableLiveData(false)
        var currentSongInstance:Song? = null
        var songList = MutableLiveData<MutableList<Song>>()
        var randomList: List<Song>? = null
        var nextPlayingList: List<Song>? = null
        var mediaPlayerService = MediaPlayer()
        lateinit var mediaSession: MediaSessionCompat
    }
    private var isPaused = false
    private val binder = LocalBinder()
    inner class LocalBinder: Binder()
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaSession = MediaSessionCompat(applicationContext,"Player")
        val position = intent!!.getIntExtra("position",-1)
        songList.value = intent.getParcelableArrayListExtra("songList")!!
        if(songList.value !=null){
            createMediaPlayer(songList.value!![position],applicationContext)
        }

        mediaPlayerService.setOnCompletionListener {
            next(this)
        }

        val handler = Handler(Looper.getMainLooper())
        val delay = 1000L
        handler.postDelayed(object: Runnable{
            override fun run() {
                try{
                    if(mediaPlayerService.currentPosition <= mediaPlayerService.duration) {
                       if(mediaPlayerService.duration - mediaPlayerService.currentPosition < 100){
                            next(this@MusicService)
                        }
                    }

                }
                catch (e:Exception){
                    Log.i("ex", e.printStackTrace().toString())
                }
                handler.postDelayed(this,delay)
            }
        },delay)
        return START_STICKY
    }



    fun createMediaPlayer(song: Song, context: Context?){
        mediaPlayerService = MediaPlayer.create(context,song.song)
        if(mediaPlayerService.isPlaying){
            mediaPlayerService.stop()
            mediaPlayerService.release()
        }
        currentSongInstance = song
        start()
        songChanged.value = true
        STATE.value="PLAY"
        mediaSession.release()
        mediaSession = MediaSessionCompat(context,"Player")
        NotificationHandler(context!!).showNotification(R.drawable.ic_simple_pause_,1F)
    }

    fun start(){
        mediaPlayerService.start()
    }

    fun play(context: Context?){
        if(mediaPlayerService.isPlaying){
            mediaPlayerService.pause()
            isPaused = true
            STATE.value="PAUSE"
            Log.i("M","Pause")
            NotificationHandler(context!!).showNotification(R.drawable.ic_simple_play_,0F)
        }
        else{
            mediaPlayerService.start()
            isPaused = false
            STATE.value="PLAY"
            Log.i("M","Play")
            NotificationHandler(context!!).showNotification(R.drawable.ic_simple_pause_,1F)
        }
    }

    fun next(context: Context?){
        val nextPlayingList = if(isShuffleOn.value == true){
            randomList
        } else{
            songList.value
        }
        var songPosition = getPosition(nextPlayingList?.toMutableList() as ArrayList<Song>)
        val prevSongPosition = songPosition
        mediaPlayerService.stop()
        if(isLoopOn.value==false && nextPlayingList.isNotEmpty()){
            songPosition = (songPosition+ 1)%(nextPlayingList.size)
        }
        if(Queue.queue.isNotEmpty()){
            if(Queue.played.isEmpty()){
                    Queue.played.add(nextPlayingList[prevSongPosition])
            }
            val currentSong = Queue.queue.first()
            createMediaPlayer(currentSong,context)
            mediaPlayerService.start()
            currentSongInstance = currentSong
            Queue.played.add(currentSong)
            Queue.queue.removeFirst()
            firstSongPlayed.value = true

        }
        else {
            if(Queue.played.isNotEmpty()){
                val song= Queue.played[0]
                currentSongInstance = song
                songPosition = getPosition(nextPlayingList as ArrayList<Song>)
                songPosition=(songPosition+ 1)%(nextPlayingList.size)
                Queue.played.clear()
            }
            createMediaPlayer(nextPlayingList[songPosition], context)
            mediaPlayerService.start()
            currentSongInstance = nextPlayingList[songPosition]
        }
        songState.value = "NEXT"

    }

    fun prev(context: Context?){
        val nextPlayingList = if(isShuffleOn.value == true){
            randomList
        } else{
            songList.value
        }
        var songPosition = getPosition(nextPlayingList as ArrayList<Song>)
        mediaPlayerService.stop()
        if(isLoopOn.value==false && nextPlayingList.isNotEmpty()){
            songPosition -= 1
                if(songPosition<0){
                    songPosition += nextPlayingList.size
                }

        }
        if(Queue.played.isNotEmpty()){
            currentSongInstance = Queue.played[0]
            songPosition = getPosition(nextPlayingList)
        }
        mediaPlayerService.stop()
        createMediaPlayer(nextPlayingList[songPosition], context)
        mediaPlayerService.start()
        currentSongInstance = nextPlayingList[songPosition]
        songState.value = "PREV"

    }

    private fun getPosition(songList: ArrayList<Song>): Int{
        for( i in 0 until songList.size){
            if(songList[i].id == currentSongInstance?.id){
                return i
            }
        }
        return -1
    }

}