package com.example.mymusicapplication.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.example.mymusicapplication.util.Queue
import com.example.mymusicapplication.R

import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.fragments.DetailFragment
import com.example.mymusicapplication.notification.NotificationHandler
import kotlin.random.Random


class MusicService: Service(),LifecycleObserver {
    //var position:Int = -1

    companion object{
        //var recentlyPlayed = ArrayList<Song>()
        var currentPlaylist: String? = null
        var songChanged = MutableLiveData(false)
        var songState = MutableLiveData<String>()
        var STATE = MutableLiveData<String>()
        var firstSongPlayed = MutableLiveData(false)
        var isShuffleOn = MutableLiveData(false)
        var isLoopOn = MutableLiveData(false)
        var currentSongInstance:Song? = null
        var songList = MutableLiveData<MutableList<Song>>(DetailFragment.songList)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaSession = MediaSessionCompat(applicationContext,"Player")
        val position = intent!!.getIntExtra("position",-1)
        songList.value = intent.getParcelableArrayListExtra("songList")!!
        Log.i("MSsongList", songList.value.toString())
        if(songList.value !=null){
            createMediaPlayer(songList.value!![position],applicationContext)
        }

        mediaPlayerService.setOnCompletionListener {
            Log.i("MusicService","onCompletionListener")
            next(this)
        }

        val handler = Handler(Looper.getMainLooper())
        val delay = 1000L
        handler.postDelayed(object: Runnable{
            override fun run() {
                try{
                    if(mediaPlayerService.currentPosition <= mediaPlayerService.duration) {
//                        if((mediaPlayerService.duration - mediaPlayerService.currentPosition) < 10){
//                            next(this@MusicService)
//                        }
                       if(mediaPlayerService.duration - mediaPlayerService.currentPosition < 100){
                            next(this@MusicService)
                        }
                    }

                }
                catch (e:Exception){
                    Log.i("ex", e.printStackTrace().toString())                }

                handler.postDelayed(this,delay)
            }
        },delay)



//        NotificationReceiver.STATE.observe(this){
//            when(it){
//                DetailFragment.ACTION_PLAY ->{}
//                DetailFragment.ACTION_NEXT ->{
//                    Log.i("obs","next")
//                    mediaPlayerService.stop()
//                    position = (position+ 1)%(songList!!.size)
//                    createMediaPlayer(songList!![position], application)
//                    mediaPlayerService.start()
//                }
//                DetailFragment.ACTION_PREV ->{}
//            }
//        }
        //createMediaPlayer(songList[position],application)

        return START_STICKY
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun createMediaPlayer(song: Song, context: Context?){
        //val notificationHandler = NotificationHandler(context!!)
        mediaPlayerService = MediaPlayer.create(context,song.song)
        if(mediaPlayerService.isPlaying){
            mediaPlayerService.stop()
            mediaPlayerService.release()
        }
        currentSongInstance = song
        start()
        songChanged.value = true
        STATE.value="PLAY"
        //recentlyPlayed.add(song)
        mediaSession.release()
        mediaSession = MediaSessionCompat(context,"Player")
        Log.i("MusicService","first notification")
        NotificationHandler(context!!).showNotification(R.drawable.ic_simple_pause_,1F)

    }

    fun start(){
        mediaPlayerService.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun play(context: Context?){
        if(mediaPlayerService.isPlaying){
            mediaPlayerService.pause()
            isPaused = true
            STATE.value="PAUSE"
            NotificationHandler(context!!).showNotification(R.drawable.ic_simple_play_,0F)


//            val audioBecomingNoisy = AudioBecomingNoisy()
//            //val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
//            unregisterReceiver(audioBecomingNoisy)
        }
        else{
//            val audioBecomingNoisy = AudioBecomingNoisy()
//            val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
//            registerReceiver(audioBecomingNoisy,noisyIntentFilter)
            mediaPlayerService.start()
            isPaused = false
            STATE.value="PLAY"
            NotificationHandler(context!!).showNotification(R.drawable.ic_simple_pause_,1F)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun next(context: Context?){
        val nextPlayingList = if(isShuffleOn.value == true){
            randomList
        } else{
            songList.value
        }
        for(song in nextPlayingList!!){
            Log.i("MusicServiceNextList", song.title.toString())
        }
        Log.i("MusicServicenextList", nextPlayingList.toString())
        //val notificationHandler:NotificationHandler = NotificationHandler(context!!)
        Log.i("MusicService","next")
        //Log.i("MusicService", "${Queue.queue}------ ${Queue.played}---- $currentSongInstance")
        //songList = ListFragment.songs
        var songPosition = getPosition(nextPlayingList!!.toMutableList() as ArrayList<Song>)
        Log.i("MusicService",songPosition.toString())
        val prevSongPosition = songPosition
        mediaPlayerService.stop()
        if(isLoopOn.value==false && nextPlayingList!!.isNotEmpty()){
            songPosition = (songPosition+ 1)%(nextPlayingList!!.size)
        }
        if(Queue.queue.isNotEmpty()){
            if(Queue.played.isEmpty()){
                    Queue.played.add(nextPlayingList!![prevSongPosition])

            }
            val currentSong = Queue.queue.first()
            createMediaPlayer(currentSong,context)
            mediaPlayerService.start()
            currentSongInstance = currentSong
            Queue.played.add(currentSong)
            Queue.queue.removeFirst()
//            val dFrag = MainActivity().supportFragmentManager.findFragmentByTag("queueDialog") as DialogFragment
//            if(Queue.queue.isEmpty()){
//                val tv=dFrag.view?.findViewById<TextView>(R.id.tvNoSongs)
//                tv?.visibility = View.VISIBLE
//
//            }
            firstSongPlayed.value = true

        }
        else {
            if(Queue.played.isNotEmpty()){
                val song= Queue.played[0]
                currentSongInstance = song
                songPosition = getPosition(nextPlayingList!! as ArrayList<Song>)
                songPosition=(songPosition+ 1)%(nextPlayingList!!.size)
                Queue.played.clear()
            }
            Log.i("next service", songPosition.toString())
            createMediaPlayer(nextPlayingList!![songPosition], context)
            mediaPlayerService.start()
            currentSongInstance = nextPlayingList!![songPosition]
            //MyRecyclerViewAdapter().notifyDataSetChanged()
                //NotificationHandler(context!!).release()
                //NotificationHandler(context!!).showNotification(R.drawable.ic_simple_pause_, 1F)


        }
        Log.i("MusicService", currentSongInstance?.title.toString())
        songState.value = "NEXT"

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun prev(context: Context?){
        val nextPlayingList = if(isShuffleOn.value == true){
            randomList
        } else{
            songList.value
        }
        Log.i("MusicService","prev ${Queue.played.toList()}")
        //songList = ListFragment.songs
        var songPosition = getPosition(nextPlayingList as ArrayList<Song>)
        mediaPlayerService.stop()
        if(isLoopOn.value==false && nextPlayingList!!.isNotEmpty()){
            songPosition -= 1
                if(songPosition<0){
                    songPosition += nextPlayingList!!.size
                }

        }
        if(Queue.played.isNotEmpty()){
            currentSongInstance = Queue.played[0]
            songPosition = getPosition(nextPlayingList as ArrayList<Song>)
        }
        mediaPlayerService.stop()
        createMediaPlayer(nextPlayingList!![songPosition], context)
        mediaPlayerService.start()
        currentSongInstance = nextPlayingList!![songPosition]
        //MyRecyclerViewAdapter().notifyDataSetChanged()
        Log.i("MusicService", currentSongInstance?.title.toString())
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

    private fun getRandom(pos: Int): Int{
        var r = Random.nextInt(0, songList.value!!.size)
        if(r==pos){
            r = getRandom(pos)
        }
        return r
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.i("MusicService","onTaskRemoved")
        super.onTaskRemoved(rootIntent)
    }




//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onDestroy() {
//        Log.i("MusicService","onDestroy")
//        mediaPlayerService.stop()
//        super.onDestroy()
//    }

//    override fun getLifecycle(): Lifecycle {
//        return
//    }

}