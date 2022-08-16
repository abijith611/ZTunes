package com.example.mymusicapplication.adapter

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.R
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.databinding.AlbumListItemBinding


import com.example.mymusicapplication.db.Playlist
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.fragments.AlbumFragment
import com.example.mymusicapplication.fragments.AlbumFragment.Companion.adapterUpdate
import com.example.mymusicapplication.fragments.AlbumFragment.Companion.songAdded
import com.example.mymusicapplication.fragments.ListFragment
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.service.MusicService.Companion.songChanged
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewholder.MyPlaylistViewHolder
import com.google.android.material.snackbar.Snackbar

class MyPlaylistRecyclerViewAdapter(private var albumList: List<Playlist>, private var type: String, private var songViewModel: SongViewModel, private var song: Song?, private var user: User):RecyclerView.Adapter<MyPlaylistViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlaylistViewHolder {
        val layoutInflater =  LayoutInflater.from(parent.context)
        val binding = AlbumListItemBinding.inflate(layoutInflater, parent, false)
        return MyPlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyPlaylistViewHolder, position: Int) {
        holder.title.text = albumList[position].name
        if(albumList[position].playlist.isNotEmpty())
        holder.imageView.setImageResource(albumList[position].playlist[0].img)
        if(type=="selection_view"){holder.options.visibility = View.INVISIBLE}
        holder.options.setOnClickListener {
            val activity = it.context.scanForActivity()
            var context = it.context
            Log.i("PlaylistAdapter","clicked")
            val popupMenu = PopupMenu(activity, it)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item1 -> {
                        val dialog = Dialog(context, R.style.DialogStyle)
                        dialog.setContentView(R.layout.rename_custom_dialog)
                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.show()
                        val playlistName = dialog.findViewById<EditText>(R.id.etName)
                        val okButton = dialog.findViewById<TextView>(R.id.okButton)
                        val cancelButton = dialog.findViewById<TextView>(R.id.cancelButton)
                        cancelButton.setOnClickListener{
                            dialog.cancel()
                        }
                        playlistName.setText(albumList[position].name)
                        playlistName.onFocusChangeListener =
                            View.OnFocusChangeListener { v, hasFocus -> if(hasFocus){
                                playlistName.hint = ""
                                }
                            }

                        okButton.setOnClickListener{
                            if(playlistName.text.isNotEmpty()) {
                                if(playlistName.text.length < 15) {
                                    if (!isExisting(
                                            playlistName.text.toString(),
                                            songViewModel.getPlaylistForUser(user.email!!)
                                        )
                                    ) {
                                        albumList[position].name = playlistName.text.toString()
                                        songViewModel.updatePlaylist(albumList[position])
                                        notifyDataSetChanged()
                                        dialog.cancel()
                                    }
                                    else{
                                        playlistName.error = "Playlist exists already!"
                                    }
                                }
                                else{
                                    playlistName.error = "Too long!"
                                }

                            }
                            else{
                                playlistName.error = "The field is empty!"
                            }

                        }
                        return@setOnMenuItemClickListener true
                    }
                    R.id.item2->{
                        songViewModel.deletePlaylist(albumList[position])
                        (albumList.toMutableList() as ArrayList<Playlist>).remove(albumList[position])
                        Log.i("Adapter",albumList.toString())
                        notifyItemRemoved(position)
                        updateAdapter(songViewModel.getPlaylistForUser(user.email).toMutableList())
                        //activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, AlbumFragment())?.commit()
                        return@setOnMenuItemClickListener true
                    }

                    else -> return@setOnMenuItemClickListener false
                }
            }
            popupMenu.inflate(R.menu.album_menu)
            popupMenu.gravity = Gravity.END
            popupMenu.show()
        }
        //holder.itemView.transitionName = albumList[position].name
        holder.itemView.setOnClickListener {

            val bundle = Bundle()
            bundle.putParcelableArrayList("songs",albumList[position].playlist)
            bundle.putString("genre", albumList[position].name)
            bundle.putString("type","playlist")
            bundle.putParcelable("playlist",albumList[position])
            val listFragment = ListFragment()
            listFragment.arguments = bundle

            val activity = it.context.scanForActivity()
            val ft =activity?.supportFragmentManager
                ?.beginTransaction()
                //?.addSharedElement(holder.itemView, albumList[position].name.toString())
                ?.replace(R.id.fragmentContainer,listFragment)






            if(type == "selection_view"){

                if(!song?.let { it1 -> isSongExisting(it1,albumList[position].playlist) }!!) {
                    Log.i("nextList", MusicService.nextPlayingList.toString())
                    if (MusicService.songList.value != null) {
                        if (MainActivity().checkList(
                                MusicService.songList.value,
                                albumList[position].playlist
                            )
                        ) {
                            Log.i("nextPlaying", "playing")
                            //MusicService.nextPlayingList!!.toMutableList().add(song!!)
                            MusicService.songList.value = albumList[position].playlist
                            //MusicService.songList.value!!.add(song!!)
                            //MusicService.songList.value = arrayList
                            //songChanged.value = true

                        }
                    }
                    song?.let { it1 -> songViewModel.addSongsPlaylist(it1, albumList[position]) }
                    holder.imageView.setImageResource(albumList[position].playlist[0].img)

                    songAdded.value = true

                    Snackbar.make(it, "Song Added", Snackbar.LENGTH_SHORT).setAnchorView(R.id.snack_bar_anchor).show()
//                    activity?.supportFragmentManager?.findFragmentByTag("albumDialog").let {
//                        (it as AlbumDialogFragment).dismiss()
//                    }
                    //activity.onBackPressed()
                }
                else{
                    Snackbar.make(it,"Song already in album",Snackbar.LENGTH_SHORT).setAnchorView(R.id.snack_bar_anchor).show()
                }
            }
            else{
                ft?.addToBackStack(null)
                ft?.commit()
            }


//                .addToBackStack(null)
//                .commit()
        }
    }

    override fun getItemCount(): Int {
//        if(albumList.isNotEmpty())
//        albumList = songViewModel.getPlaylistForUser(user.email).toMutableList()
        return albumList.size
    }

    private fun isSongExisting(currentSong: Song, playlist: List<Song>): Boolean{
        for(song in playlist){
            if(song.title== currentSong.title){
                return true
            }
        }
        return false
    }

    private fun Context.scanForActivity(): AppCompatActivity? {
        return when (this) {
            is AppCompatActivity -> this
            is ContextWrapper -> baseContext.scanForActivity()
            else -> null
        }
    }

    fun updateAdapter(playlistList: List<Playlist>){
        albumList = playlistList
        notifyDataSetChanged()
        adapterUpdate.value = true
    }

    private fun isExisting(name: String, playlists: Array<Playlist>): Boolean{
        for(playlist in playlists){
            if(playlist.name == name){
                return true
            }
        }
        return false
    }
}