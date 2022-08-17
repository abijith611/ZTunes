package com.example.mymusicapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.R
import com.example.mymusicapplication.notification.NotificationHandler

class AudioBecomingNoisyReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            MusicService.STATE.value = "PAUSE"
            if (MusicService.mediaPlayerService.isPlaying) {
                MusicService.mediaPlayerService.pause()
            }
            NotificationHandler(context!!).showNotification(R.drawable.ic_simple_play_,0F)
        }
    }
}