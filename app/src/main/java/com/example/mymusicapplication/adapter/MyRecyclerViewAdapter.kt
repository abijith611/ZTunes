package com.example.mymusicapplication.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.activity.MainActivity.Companion.snackBarAnchor
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.service.MusicService.Companion.currentSongInstance
import com.example.mymusicapplication.service.MusicService.Companion.mediaPlayerService
import com.example.mymusicapplication.util.Queue
import com.example.mymusicapplication.R
import com.example.mymusicapplication.databinding.ListItemBinding


import com.example.mymusicapplication.db.Favourite

import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.fragments.*
import com.example.mymusicapplication.notification.NotificationHandler
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewholder.MyViewHolder
import com.google.android.material.snackbar.Snackbar
import java.util.ArrayList

class MyRecyclerViewAdapter(private var songList: MutableList<Song>,var songViewModel: SongViewModel,var  isFav: Boolean, var user: User) :
    RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentSong: Song = songList[position]
        holder.imageView.setImageResource(currentSong.img)
        holder.artist.text = currentSong.artist
        holder.title.text = currentSong.title
        holder.title.isSelected = true
        holder.title.setTextColor(Color.WHITE)
        holder.artist.setTextColor(Color.parseColor("#a8a8a8"))

        if (currentSongInstance != null) {
            if (currentSong.id == currentSongInstance!!.id) {
                holder.title.setTextColor(Color.parseColor("#E94650"))
                holder.artist.setTextColor(Color.parseColor("#E94650"))
            }
        }
        val fav = songViewModel.getFavForUser(user.email)

        //if(!isFav){
            isFav(holder,fav,currentSong)
            holder.fav.setOnClickListener {
                if(!isFav(holder, fav, currentSong)){
                    songViewModel.addSongsFav(currentSong, fav[0])
                    (it as ImageView).setImageResource(R.drawable.heart)
                    notifyDataSetChanged()
                }
                else{
                    songViewModel.removeSongsFav(currentSong,fav[0])
                    (it as ImageView).setImageResource(R.drawable.heart_un)
                    if(isFav){
                        val frag = FavouriteFragment()
                        (it.context as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, frag).addToBackStack(null).commit()
                    }
                    notifyDataSetChanged()
                }
                if(mediaPlayerService.isPlaying)
                NotificationHandler(it.context).showNotification(R.drawable.ic_simple_pause_,1F)
            }
//        }
//        else{
//            holder.fav.visibility = View.INVISIBLE
//        }

       // holder.itemView.transitionName = currentSong.title
        holder.itemView.setOnClickListener{
            SearchFragment.itemSelected.value = true
            val bundle = Bundle()
            bundle.putParcelable("song",currentSong)
            bundle.putParcelableArrayList("songList",songList.toMutableList() as ArrayList<Song>)
            bundle.putInt("position",position)
            MusicService.songList.value = songList
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
                //activity.onBackPressed()
                //isMiniPlayerActive.value = true
                if(mediaPlayerService.isPlaying){
                    return@setOnClickListener
                }
//                notificationHandler.release()
//                notificationHandler.showNotification(R.drawable.ic_simple_pause_,1F)

                MusicService().createMediaPlayer(currentSong, it.context)
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.miniPlayerFragmentContainer,miniFrag)
                    .commit()
                notifyDataSetChanged()
            }
            else{
                activity.supportFragmentManager.beginTransaction()
                    //.addSharedElement(holder.itemView, currentSong.title.toString())
                    .add(R.id.fragmentContainer, frag).addToBackStack(null).commit()
            }


        }
        holder.options.setOnClickListener { it ->
            val view = it
            val activity = it.context as AppCompatActivity
            val popupMenu = PopupMenu(it.context, it)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.item1 -> {
                            val dialog = AlbumDialogFragment()
                            dialog.setStyle(DialogFragment.STYLE_NORMAL,R.style.DialogStyle2)
                            val bundle = Bundle()
                            bundle.putString("type", "selection_view")
                            bundle.putParcelable("song", currentSong)
                            dialog.arguments = bundle
                            dialog.show(activity.supportFragmentManager, "albumDialog")
                            return@setOnMenuItemClickListener true
                        }

                        R.id.item2 -> {
                            if(Queue.getCount(currentSong)>=5)
                                Snackbar.make(view,"Song already added 5 times!", Snackbar.LENGTH_SHORT).setAnchorView(snackBarAnchor).show()
                            else {
                                Queue.queue.add(currentSong)
                                Snackbar.make(view, "Song added to queue", Snackbar.LENGTH_SHORT)
                                    .setAnchorView(snackBarAnchor).show()
                            }
                            return@setOnMenuItemClickListener true
                        }

                        R.id.item3 -> {
                            if(Queue.getCount(currentSong)>=5)
                                Snackbar.make(view,"Song already added 5 times!", Snackbar.LENGTH_SHORT).setAnchorView(snackBarAnchor).show()
                            else {
                                Queue.queue.add(0, currentSong)
                                Snackbar.make(view,"Song will play next", Snackbar.LENGTH_SHORT).setAnchorView(snackBarAnchor).show()
                            }
                            return@setOnMenuItemClickListener true
                        }

                        else -> return@setOnMenuItemClickListener false
                    }
                }
                popupMenu.inflate(R.menu.song_item_popup_menu)
            popupMenu.gravity = Gravity.END
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    private fun isFav(holder: MyViewHolder, fav: Array<Favourite>, song: Song):Boolean{
        return if (song in fav[0].favList) {
            holder.fav.setImageResource(R.drawable.heart)
            true
        } else {
            holder.fav.setImageResource(R.drawable.heart_un)
            false
        }
    }




}