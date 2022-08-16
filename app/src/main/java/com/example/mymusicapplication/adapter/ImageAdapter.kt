package com.example.mymusicapplication.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mymusicapplication.activity.MainActivity.Companion.isMiniPlayerActive
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.service.MusicService.Companion.mediaPlayerService
import com.example.mymusicapplication.R
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.fragments.DetailFragment
import com.example.mymusicapplication.fragments.MiniPlayerFragment

class ImageAdapter(private val imageList: ArrayList<Song>, private val viewPager2: ViewPager2): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView = itemView.findViewById(R.id.tvInfo) as TextView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.image_container, parent,false)
        return ImageViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Log.i("on bind", imageList.toString())
        when(imageList[position].title){
            imageList[1].title->holder.imageView.setImageResource(R.mipmap.shape_of_you_banner)
            imageList[2].title->holder.imageView.setImageResource(R.mipmap.love_banner)
            imageList[3].title->holder.imageView.setImageResource(R.mipmap.roar_banner_3)
            else->holder.imageView.setImageResource(imageList[position].img)
        }
       // holder.imageView.setImageResource(imageList[position].img)
        val currentSong: Song = imageList[position]
        if (position == imageList.size - 1) {
            viewPager2.post(runnable)
        }
        holder.textView.text= imageList[position].title

        //holder.itemView.transitionName = currentSong.title
        holder.itemView.setOnClickListener {
            var imageList1 = ArrayList<Song>()
            for(i in 0..3){
                imageList1.add(imageList[i])
            }
            Log.i("CLICK", "clicked")
            Log.i("imageList", "position $position")
            var position1 = position % 4
            val bundle = Bundle()
            bundle.putParcelable("song", currentSong)
            bundle.putParcelableArrayList("songList", imageList1)
            bundle.putInt("position", position1)
            val frag = DetailFragment()
            val miniFrag = MiniPlayerFragment()
            frag.arguments = bundle
            miniFrag.arguments = bundle
            if (MusicService.mediaPlayerService.isPlaying || MusicService.STATE.value == "PAUSE") {
                if (MusicService.currentSongInstance?.song != currentSong.song) {
                    MusicService.mediaPlayerService.stop()
                } else {
                    Log.i("mediarec", "same song")
                }
            }
            val activity = it.context as AppCompatActivity
            if(isMiniPlayerActive.value==true){
                if(mediaPlayerService.isPlaying){
                    return@setOnClickListener
                }
                MusicService().createMediaPlayer(currentSong, it.context)
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.miniPlayerFragmentContainer,miniFrag)
                    .commit()
            }
            else{
                activity.supportFragmentManager.beginTransaction()
                    //.addSharedElement(holder.itemView, currentSong.title.toString())
                    .add(R.id.fragmentContainer, frag).addToBackStack(null).commit()
            }
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    private val runnable = Runnable{
        imageList.addAll(imageList)
        notifyDataSetChanged()
    }
}