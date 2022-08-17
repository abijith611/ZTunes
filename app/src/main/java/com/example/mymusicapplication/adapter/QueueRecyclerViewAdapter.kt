package com.example.mymusicapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.R
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.databinding.ListItemBinding
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.fragments.DetailFragment
import com.example.mymusicapplication.fragments.MiniPlayerFragment
import com.example.mymusicapplication.fragments.QueueFragment
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.service.MusicService.Companion.currentSongInstance
import com.example.mymusicapplication.util.Queue
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewholder.MyViewHolder

class QueueRecyclerViewAdapter(private var songList: ArrayList<Song>, var songViewModel: SongViewModel, var user: User?, var context: Context, var activity: MainActivity):
RecyclerView.Adapter<MyViewHolder>(), LifecycleObserver{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(layoutInflater,parent,false)
        return MyViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.layout.setBackgroundResource(R.color.black)
        val currentSong = songList[position]
        holder.imageView.setImageResource(currentSong.img)
        holder.title.isSelected = true
        holder.artist.text = currentSong.artist
        holder.title.text = currentSong.title

        holder.options.setOnClickListener {
            val activity = it.context.scanForActivity()
            val popupMenu = PopupMenu(activity,it)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.item1 -> {
                        Queue.queue.remove(currentSong)
                        notifyItemRemoved(position)
                        QueueFragment.queueItemClick.value = true
                        return@setOnMenuItemClickListener true
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }
            popupMenu.inflate(R.menu.queue_menu)
            popupMenu.gravity = Gravity.END
            popupMenu.show()
        }


        holder.itemView.setOnClickListener{
            currentSongInstance?.let { it1 -> Queue.played.add(it1) }
            val bundle = Bundle()
            bundle.putParcelable("song",currentSong)
            bundle.putParcelableArrayList("songList",songList.toMutableList() as java.util.ArrayList<Song>)
            bundle.putInt("position",position)
            val frag = DetailFragment()
            val miniFrag = MiniPlayerFragment()
            frag.arguments = bundle
            miniFrag.arguments = bundle
            if(MusicService.mediaPlayerService.isPlaying || MusicService.STATE.value=="PAUSE"){
                if(currentSongInstance?.song != currentSong.song){
                    MusicService.mediaPlayerService.stop()
                }
            }

            val activity = it.context as AppCompatActivity

            if(MainActivity.isMiniPlayerActive.value==true){
                if(MusicService.mediaPlayerService.isPlaying){
                    return@setOnClickListener
                }

                MusicService().createMediaPlayer(currentSong, it.context)
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.miniPlayerFragmentContainer,miniFrag)
                    .commit()
                notifyDataSetChanged()
            }
            else{
                activity.supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, frag).addToBackStack(null).commit()
            }

        Queue.queue.remove(currentSong)
        Queue.played.add(currentSong)
        notifyItemRemoved(position)
        if(currentSongInstance!=null){
            QueueFragment.queueItemClick.value = true
        }
        }

    }

    override fun getItemCount(): Int {
       return songList.size
    }

    private fun Context.scanForActivity(): AppCompatActivity? {
        return when (this) {
            is AppCompatActivity -> this
            is ContextWrapper -> baseContext.scanForActivity()
            else -> null
        }
    }


}