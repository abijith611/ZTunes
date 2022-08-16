package com.example.mymusicapplication.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.service.MusicService.Companion.mediaSession
import com.example.mymusicapplication.R
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.fragments.DetailFragment
import com.example.mymusicapplication.receiver.NotificationReceiver
import kotlinx.coroutines.runBlocking

class NotificationHandler(var context: Context) {
    private var notification:NotificationCompat.Builder? = null
    companion object{
        var notificationManager:NotificationManager? = null

    }
    val mainActivity = MainActivity()
    val dao = SongDatabase.getInstance(context).songDao
    val repository = SongRepository(dao)
    val user = runBlocking{ repository.getUsers() }.last()
    private val fav = runBlocking { user.email?.let { repository.getFavForUser(it) } }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showNotification(playPauseButton: Int, playbackSpeed:Float){
        Log.i("mediaSession","$mediaSession ${mediaSession.sessionToken.toString()}")
        val intent = Intent(context, MainActivity::class.java).also {
            it.putExtra("user",user)
            it.putExtra("fragment","detail")
            it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        val prevIntent = Intent(context, NotificationReceiver::class.java).setAction(
            NotificationReceiver.ACTION_PREV)
        val prevPendingIntent = PendingIntent.getBroadcast(context, 0, prevIntent, PendingIntent.FLAG_MUTABLE)
        val playIntent = Intent(context, NotificationReceiver::class.java).setAction(
            NotificationReceiver.ACTION_PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_MUTABLE)
        val nextIntent = Intent(context, NotificationReceiver::class.java).setAction(
            NotificationReceiver.ACTION_NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_MUTABLE)
        val favIntent = Intent(context, NotificationReceiver::class.java).setAction(
            NotificationReceiver.ACTION_FAV)
        val favPendingIntent = PendingIntent.getBroadcast(context, 0, favIntent, PendingIntent.FLAG_MUTABLE)
        val dismissIntent = Intent(context, NotificationReceiver::class.java).setAction(
            NotificationReceiver.ACTION_DISMISS)
        val dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_MUTABLE)
        val picture = MusicService.currentSongInstance?.let { BitmapFactory.decodeResource(context.resources, it.img) }
        var favIcon = -1
        favIcon = if(MusicService.currentSongInstance in fav!![0].favList){
            R.drawable.heart
        } else{
            R.drawable.heart_un
        }
        notification = MusicService.currentSongInstance?.let {
            NotificationCompat.Builder(context, DetailFragment.CHANNEL_ID_2)
                .setSmallIcon(it.img)
                .setLargeIcon(picture)
                .setContentTitle(it.title)
                .setContentText(it.artist)
                .addAction(favIcon,"fav",favPendingIntent)
                .addAction(R.drawable.ic_simple_prev,"previous", prevPendingIntent)
                .addAction(playPauseButton,"play", playPendingIntent)
                .addAction(R.drawable.ic_simple_skip,"next", nextPendingIntent)
                //.setProgress(100, 50,false)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)


        }

        mediaSession?.setMetadata(
            MediaMetadataCompat.Builder()
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, MusicService.mediaPlayerService.duration.toLong())
            .build())
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING, MusicService.mediaPlayerService.currentPosition.toLong(),playbackSpeed)
            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
            .build()
        )
        mediaSession?.setCallback(object: MediaSessionCompat.Callback(){
            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                Log.i("NotificationHandler","onSeek")
                MusicService.mediaPlayerService.seekTo(pos.toInt())
                mediaSession?.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            MusicService.mediaPlayerService.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                )
            }

        })

        Log.i("notification", notification.toString())
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(DetailFragment.CHANNEL_ID_2, DetailFragment.CHANNEL_ID_2, NotificationManager.IMPORTANCE_HIGH)
        notificationManager?.createNotificationChannel(notificationChannel)
        notification?.setChannelId(DetailFragment.CHANNEL_ID_2)
        notificationManager?.notify(0,notification?.build())
    }

    fun release(){
        Log.i("MediaSession",mediaSession?.isActive.toString())
        mediaSession?.release()
        Log.i("MediaSession",mediaSession?.isActive.toString())
    }



}