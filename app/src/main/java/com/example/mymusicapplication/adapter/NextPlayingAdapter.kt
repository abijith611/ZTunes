package com.example.mymusicapplication.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.util.Queue
import com.example.mymusicapplication.R
import com.example.mymusicapplication.databinding.ListItemBinding
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.fragments.*
import com.example.mymusicapplication.viewholder.MyViewHolder
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class NextPlayingAdapter(private var songList: ArrayList<Song>, var context: Context, var activity: MainActivity):
    RecyclerView.Adapter<MyViewHolder>(), LifecycleObserver{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(layoutInflater,parent,false)
        return MyViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var snackBarAnchor: View?
        holder.layout.setBackgroundResource(R.color.black)
        val currentSong = songList[position]
        holder.imageView.setImageResource(currentSong.img)
        holder.title.isSelected = true
        holder.artist.text = currentSong.artist
        holder.title.text = currentSong.title
        holder.options.setOnClickListener {
            val view = it
            val activity = it.context as AppCompatActivity
            snackBarAnchor = if(MainActivity.isMiniPlayerActive.value == true){
                activity.findViewById(R.id.miniPlayerFragmentContainer) as FragmentContainerView
            } else{
                activity.findViewById(R.id.menu) as BottomNavigationView
            }
            val popupMenu = PopupMenu(it.context, it)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.item1 -> {
                            val bundle = Bundle()
                            bundle.putString("type", "selection_view")
                            bundle.putParcelable("song", currentSong)
                            val dialog = AlbumDialogFragment()
                            dialog.setStyle(DialogFragment.STYLE_NORMAL,R.style.DialogStyle2)
                            dialog.arguments = bundle
                            dialog.show(activity.supportFragmentManager, "albumDialog")
//                            val fragment = AlbumFragment()
//                            fragment.arguments = bundle
//                            activity.supportFragmentManager.beginTransaction()
//                                .replace(R.id.fragmentContainer, fragment)
//                                .addToBackStack("album")
//                                .commit()
                            return@setOnMenuItemClickListener true
                        }
                        R.id.item2 -> {
                            if(Queue.getCount(currentSong)>=5)
                                Snackbar.make(view,"Song already added 5 times!", Snackbar.LENGTH_SHORT).setAnchorView(snackBarAnchor).show()
                            else {
                                Queue.queue.add(currentSong)
                                activity.findViewById<FragmentContainerView>(R.id.miniPlayerFragmentContainer)
                                Log.i("RV", snackBarAnchor.toString())
                                Snackbar.make(view, "Song added to queue", Snackbar.LENGTH_SHORT)
                                    .setAnchorView(snackBarAnchor).show()
                                val frag = QueueFragment()
                                activity.supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,frag).addToBackStack(null).commit()

                            }
                            return@setOnMenuItemClickListener true
                        }
                        R.id.item3 -> {
                            if(Queue.getCount(currentSong)>=5)
                                Snackbar.make(view,"Song already added 5 times!", Snackbar.LENGTH_SHORT).setAnchorView(snackBarAnchor).show()
                            else {
                                Queue.queue.add(0, currentSong)
                                Snackbar.make(view,"Song will play next", Snackbar.LENGTH_SHORT).setAnchorView(snackBarAnchor).show()
                                val frag = QueueFragment()
                                activity.supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,frag).addToBackStack(null).commit()
                            }
                            return@setOnMenuItemClickListener true
                        }

                        else -> return@setOnMenuItemClickListener false
                    }
                }
                popupMenu.gravity = Gravity.END
                popupMenu.inflate(R.menu.song_item_popup_menu)
                popupMenu.show()
            }

       // holder.itemView.transitionName = currentSong.title
        holder.itemView.setOnClickListener{
            Log.i("CLICK","clicked")
            val bundle = Bundle()
            bundle.putParcelable("song",currentSong)
            bundle.putParcelableArrayList("songList",songList.toMutableList() as java.util.ArrayList<Song>)
            bundle.putInt("position",position)
           // MusicService.songList = songList
            val frag = DetailFragment()
            val miniFrag = MiniPlayerFragment()
            frag.arguments = bundle
            miniFrag.arguments = bundle
            if(MusicService.mediaPlayerService.isPlaying || MusicService.STATE.value=="PAUSE"){
                if(MusicService.currentSongInstance?.song != currentSong.song){
                    MusicService.mediaPlayerService.stop()
                }
                else{
                    Log.i("mediarec","same song")
                }
            }

            val activity = it.context as AppCompatActivity

            if(MainActivity.isMiniPlayerActive.value==true){
                //activity.onBackPressed()
                //isMiniPlayerActive.value = true
                if(MusicService.mediaPlayerService.isPlaying){
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
            QueueFragment.upNextItemClick.value = true


        }




//        holder.itemView.setOnClickListener {
//            Intent(context, MusicService::class.java).also {
//                it.putExtra("position", position)
//                it.putParcelableArrayListExtra("songList", songList)
//                activity.startService(it)
//                MusicService.mediaPlayerService.stop()
//                QueueDialogFragment.queueSongState.value = true
//            }
//        }

//        holder.options.setOnClickListener {
//            val activity = it.context.scanForActivity()
//            val popupMenu = PopupMenu(activity,it)
//            popupMenu.setOnMenuItemClickListener {
//                when(it.itemId){
//                    R.id.item1 -> {
//                        Queue.queue.remove(currentSong)
//                        notifyItemRemoved(position)
//                        val dFrag = activity?.supportFragmentManager?.findFragmentByTag("queueDialog") as DialogFragment
//                        if(Queue.queue.isEmpty()){
//                            val tv=dFrag.view?.findViewById<TextView>(R.id.tvNoSongs)
//                            tv?.visibility = View.VISIBLE
//
//                        }
//                        return@setOnMenuItemClickListener true
//                    }
//                    else -> return@setOnMenuItemClickListener false
//                }
//            }
//            popupMenu.inflate(R.menu.queue_menu)
//            popupMenu.gravity = Gravity.END
//            popupMenu.show()
//        }

    }

    override fun getItemCount(): Int {
        return songList.size
    }

    fun Context.scanForActivity(): AppCompatActivity? {
        return when (this) {
            is AppCompatActivity -> this
            is ContextWrapper -> baseContext.scanForActivity()
            else -> null
        }
    }



}