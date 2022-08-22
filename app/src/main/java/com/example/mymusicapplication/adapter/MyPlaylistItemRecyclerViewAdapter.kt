package com.example.mymusicapplication.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.postponeEnterTransition
import androidx.core.app.ActivityCompat.startPostponedEnterTransition
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.activity.MainActivity.Companion.isMiniPlayerActive
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.service.MusicService.Companion.currentSongInstance
import com.example.mymusicapplication.service.MusicService.Companion.mediaPlayerService
import com.example.mymusicapplication.util.Queue
import com.example.mymusicapplication.R
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.databinding.ListItemBinding
import com.example.mymusicapplication.db.Favourite
import com.example.mymusicapplication.db.Playlist
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.fragments.*
import com.example.mymusicapplication.service.MusicService.Companion.nextPlayingList
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewholder.MyViewHolder
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar


class MyPlaylistItemRecyclerViewAdapter(private var songList: MutableList<Song>, private var type: String, private var songViewModel: SongViewModel, private var playlist: Playlist?,var user: User?) :
    RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }


    @SuppressLint("CutPasteId", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var snackBarAnchor: View?
        val currentSong: Song = songList[position]
        holder.imageView.setImageResource(currentSong.img)
        holder.artist.text = currentSong.artist
        holder.title.text = currentSong.title
        holder.title.isSelected = true
        holder.title.setTextColor(Color.WHITE)
        holder.artist.setTextColor(Color.parseColor("#a8a8a8"))
        if(type=="playlist"){
            if(currentSong == currentSongInstance)
                holder.options.visibility = View.INVISIBLE
            else
                holder.options.visibility = View.VISIBLE
            holder.layout.setBackgroundResource(R.color.black)}

            if (currentSongInstance != null) {
                if (currentSong.id == currentSongInstance!!.id) {
                    Log.i("current song", "current")
                    holder.title.setTextColor(Color.parseColor("#E94650"))
                    holder.artist.setTextColor(Color.parseColor("#E94650"))
                }
            }



        val fav = songViewModel.getFavForUser(user?.email)
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
                notifyDataSetChanged()
            }
        }


        holder.itemView.setOnClickListener{
            if(type=="playlist"){
                MusicService.currentPlaylist = playlist!!.name
            }
            else{
                MusicService.currentPlaylist = null
            }
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
            if(mediaPlayerService.isPlaying || MusicService.STATE.value=="PAUSE"){
                if(currentSongInstance?.song != currentSong.song){
                    mediaPlayerService.stop()
                }
            }

            val activity = it.context as AppCompatActivity
            if(isMiniPlayerActive.value==true){
                if(mediaPlayerService.isPlaying){
                    return@setOnClickListener
                }
                MusicService().createMediaPlayer(currentSong,it.context)
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.miniPlayerFragmentContainer,miniFrag)
                    .commit()
                notifyDataSetChanged()

            }
            else{
                activity.supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, frag).addToBackStack(null).commit()
            }
        }

        holder.options.setOnClickListener { it ->
            val view = it
            val activity = it.context as AppCompatActivity
            snackBarAnchor = if(isMiniPlayerActive.value == true){
                activity.findViewById(R.id.miniPlayerFragmentContainer) as FragmentContainerView
            } else{
                activity.findViewById(R.id.menu) as BottomNavigationView
            }
            val popupMenu = PopupMenu(it.context, it)
            if(type != "playlist"){
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.item1 -> {
                            val bundle = Bundle()
                            bundle.putString("type", "selection_view")
                            bundle.putParcelable("song", currentSong)
                            val dialog = AlbumDialogFragment()
                            dialog.setStyle(DialogFragment.STYLE_NORMAL,R.style.DialogStyle2)
                            dialog.arguments = bundle
                            dialog.activity?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            dialog.show(activity.supportFragmentManager, "albumDialog")
                            return@setOnMenuItemClickListener true
                        }
                        R.id.item2 -> {
                            if(Queue.getCount(currentSong)>=5)
                                Snackbar.make(view,"Song already added 5 times!", Snackbar.LENGTH_SHORT).setAnchorView(snackBarAnchor).show()
                            else {
                                Queue.queue.add(currentSong)
                                activity.findViewById<FragmentContainerView>(R.id.miniPlayerFragmentContainer)
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
                popupMenu.gravity = Gravity.END
                popupMenu.inflate(R.menu.song_item_popup_menu)
            }
            if(type=="playlist"){
                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.item1->{
                            playlist?.let { it1 ->
                                songViewModel.deleteSongPlaylist(currentSong,
                                    it1
                                )
                            }
                            (songList.toMutableList() as ArrayList<Song>).remove(currentSong)
                            if(MusicService.currentPlaylist!=null && MusicService.currentPlaylist==playlist!!.name){
                               MusicService.songList.value = songList
                            }
                            (ListFragment.songs.toMutableList() as ArrayList<Song>).remove(currentSong)
                            notifyItemRemoved(position)
                            ListFragment.ivState.value = false
                            return@setOnMenuItemClickListener true
                        }
                        else -> {
                            return@setOnMenuItemClickListener false
                        }
                    }
                }
                popupMenu.inflate(R.menu.playlist_song_item)
                popupMenu.gravity = Gravity.END
            }
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