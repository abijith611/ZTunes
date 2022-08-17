package com.example.mymusicapplication.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mymusicapplication.R
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.databinding.FragmentDetailBinding
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.listener.BackKeyListener
import com.example.mymusicapplication.listener.MusicServiceListener
import com.example.mymusicapplication.notification.NotificationHandler
import com.example.mymusicapplication.receiver.NotificationReceiver
import com.example.mymusicapplication.receiver.NotificationReceiver.Companion.ACTION_FAV
import com.example.mymusicapplication.receiver.NotificationReceiver.Companion.ACTION_NEXT
import com.example.mymusicapplication.receiver.NotificationReceiver.Companion.ACTION_PREV
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.service.MusicService.Companion.currentSongInstance
import com.example.mymusicapplication.service.MusicService.Companion.mediaPlayerService
import com.example.mymusicapplication.util.Queue
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import kotlin.math.abs

class DetailFragment : Fragment(), BackKeyListener, MusicServiceListener {
    companion object{

        //var songList:ArrayList<Song>? = null
    }
    private var x2:Float = 0.0f
    private var x1:Float = 0.0f
    private var y2:Float = 0.0f
    private var y1:Float = 0.0f
    var next: ImageView? = null
    private var prev: ImageView? = null
    private var play:ImageView? = null
    private var cardView: CardView? = null

    private var position: Int? = 0
    private var song:Song? = null
    private var mService: MusicService = MusicService()
    lateinit var binding: FragmentDetailBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MainActivity.isMiniPlayerActive.value = false


        val activity = activity as MainActivity
        activity.setBackKeyListener(this)
        activity.setMusicServiceListener(this)

        binding = FragmentDetailBinding.inflate(layoutInflater)
        val animation = AnimationUtils.loadAnimation(requireActivity().application, R.anim.slide_up)
        binding.root.animation = animation
        binding.tvAlbum.isSelected = true

        next = view?.findViewById(R.id.ivNext)
        prev = view?.findViewById(R.id.ivPrevious)
        play = view?.findViewById(R.id.ivPlay)
        cardView = view?.findViewById(R.id.cardView)
        song = arguments?.getParcelable("song") as Song?
        val songList:ArrayList<Song>? = arguments?.getParcelableArrayList("songList")
        position = arguments?.get("position") as Int?
        sharedElementEnterTransition = MaterialContainerTransform()
        sharedElementReturnTransition = MaterialContainerTransform()
        binding.root.transitionName = song?.title
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        val user = activity.intent.getParcelableExtra<User>("user")
        val fav = songViewModel.getFavForUser(user!!.email)


        isFav(songViewModel, user)
        binding.ivFav.setOnClickListener {
            if(!isFav( songViewModel, user)){
                songViewModel.addSongsFav(song!!, fav[0])
                (it as ImageView).setImageResource(R.drawable.heart)
            }
            else{
                songViewModel.removeSongsFav(song!!,fav[0])
                (it as ImageView).setImageResource(R.drawable.heart_un)
            }
            NotificationHandler(requireActivity()).showNotification(R.drawable.ic_simple_pause_,1F)
        }

        if(MusicService.STATE.value=="PAUSE"){
            binding.ivPlay.setImageResource(R.drawable.ic_play)
        }
        else{
            binding.ivPlay.setImageResource(R.drawable.ic_pause)
        }




        if(currentSongInstance?.song != song!!.song){
            Intent(context, MusicService::class.java).also {
                it.putExtra("position", position)
                it.putParcelableArrayListExtra("songList",songList)
                activity.startService(it)
//                notificationHandler.release()
//                notificationHandler.showNotification(R.drawable.ic_simple_pause_,1F)
            }
//            if(!mediaPlayerService.isPlaying){
//
//            }
            MusicService.STATE.value = "PLAY"
        }
        else{
            setSeekbarPosition()
            setSongTime()
        }
        setSongDetails(song)
        val miniPlayerFragment = MiniPlayerFragment()
        val bundle = Bundle()
        bundle.putParcelable("song",song)
        bundle.putParcelableArrayList("songList",songList)
        bundle.putInt("position",position!!)
        miniPlayerFragment.arguments = bundle
        activity.supportFragmentManager.beginTransaction()
            //.remove(this)
            .replace(R.id.miniPlayerFragmentContainer, miniPlayerFragment)
            .commit()

        val handler = Handler(Looper.getMainLooper())
        val delay = 1000L
        handler.postDelayed(object: Runnable{
            override fun run() {
                try{
                    if(mediaPlayerService.currentPosition <= mediaPlayerService.duration) {
                        setSeekbarPosition()
                        setSongTime()
                    }
                }
                catch (e:Exception){
                    Log.i("ex", e.printStackTrace().toString())                }

                handler.postDelayed(this,delay)
            }
        },delay)

        binding.seekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayerService.seekTo(seekBar!!.progress)
                NotificationHandler(requireActivity()).showNotification(R.drawable.ic_simple_pause_,1F)
            }

        })

        binding.ivPlay.setOnClickListener{
            mService.play(context)
        }

        MusicService.STATE.observe(viewLifecycleOwner){
            when(it){
                "PAUSE"-> binding.ivPlay.setImageResource(R.drawable.ic_play)
                "PLAY"-> binding.ivPlay.setImageResource(R.drawable.ic_pause)

            }
        }

        binding.ivNext.setOnClickListener{
            mService.next(context)
            setUi()
            isFav( songViewModel, user)
        }

        binding.ivPrevious.setOnClickListener{
            mService.prev(context)
            setUi()
            isFav( songViewModel, user)
        }

        MusicService.songState.observe(viewLifecycleOwner){
            if(it=="NEXT"){
                setUi()
                isFav( songViewModel, user)
            }
        }

        NotificationReceiver.STATE.observe(viewLifecycleOwner){
            when(it){
                ACTION_NEXT-> setSongDetails(currentSongInstance)
                ACTION_PREV-> setSongDetails(currentSongInstance)
                ACTION_FAV-> isFav(songViewModel, user)
            }
        }

        binding.downButton.setOnClickListener{
            MainActivity.isMiniPlayerActive.value = true
            activity.onBackPressed()
        }


        MusicService.isLoopOn.observe(viewLifecycleOwner){
            if(it==true) binding.ivLoop.setImageResource(R.drawable.ic_baseline_loop_on)
            else binding.ivLoop.setImageResource(R.drawable.ic_baseline_loop_24)
        }

        MusicService.isShuffleOn.observe(viewLifecycleOwner){
            if(it==true) binding.ivShuffle.setImageResource(R.drawable.ic_baseline_shuffle_on)
            else binding.ivShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24)
        }

        binding.ivLoop.setOnClickListener {
            MusicService.isLoopOn.value = MusicService.isLoopOn.value != true

        }

        binding.ivShuffle.setOnClickListener {
            if(MusicService.isShuffleOn.value == true){
                MusicService.isShuffleOn.value = false
                binding.ivShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24)
            }
            else{
                MusicService.isShuffleOn.value = true
                binding.ivShuffle.setImageResource(R.drawable.ic_baseline_shuffle_on)
            }
        }

        binding.ivQueue.setOnClickListener {
            val frag = QueueFragment()
            activity.supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,frag).addToBackStack(null).commit()
            MainActivity.isMiniPlayerActive.value = true
        }

        binding.optionsButton.setOnClickListener { it ->
            val view = it
            val activity1 = it.context as AppCompatActivity
            val popupMenu = PopupMenu(it.context, it)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item1 -> {
                        val dialog = AlbumDialogFragment()
                        dialog.setStyle(DialogFragment.STYLE_NORMAL,R.style.DialogStyle2)
                        val bundle = Bundle()
                        bundle.putString("type", "selection_view")
                        bundle.putParcelable("song", currentSongInstance)
                        dialog.arguments = bundle
                        dialog.show(activity1.supportFragmentManager, "albumDialog")
                        return@setOnMenuItemClickListener true
                    }

                    R.id.item2 -> {
                        if(Queue.getCount(currentSongInstance!!)>=5)
                            Snackbar.make(view,"Song already added 5 times!", Snackbar.LENGTH_SHORT).setAnchorView(
                                MainActivity.snackBarAnchor
                            ).show()
                        else {
                            Queue.queue.add(currentSongInstance!!)
                            Snackbar.make(view, "Song added to queue", Snackbar.LENGTH_SHORT)
                                .setAnchorView(MainActivity.snackBarAnchor).show()
                        }
                        return@setOnMenuItemClickListener true
                    }

                    R.id.item3 -> {
                        if(Queue.getCount(currentSongInstance!!)>=5)
                            Snackbar.make(view,"Song already added 5 times!", Snackbar.LENGTH_SHORT).setAnchorView(
                                MainActivity.snackBarAnchor
                            ).show()
                        else {
                            Queue.queue.add(0, currentSongInstance!!)
                            Snackbar.make(view,"Song will play next", Snackbar.LENGTH_SHORT).setAnchorView(
                                MainActivity.snackBarAnchor
                            ).show()
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

        binding.root.setOnTouchListener { _, event ->
            when(event.action) {
                0->{
                    x1 = event.x
                    y1 = event.y
                }
                1->{
                    x2 = event.x
                    y2 = event.y
                    x2-x1
                    val yVal = y2-y1

                    if(abs(yVal) > MiniPlayerFragment.MIN_DISTANCE){
                        if(y2 > y1){
                            binding.downButton.performClick()
                        }

                    }
                }
            }
            true
        }



        return binding.root
    }


    private fun setSeekbarPosition(){
        binding.seekBar.max = mediaPlayerService.duration
        binding.seekBar.progress = mediaPlayerService.currentPosition
    }

    private fun setSongTime(){
        binding.tvSongStart.text = createTime(mediaPlayerService.currentPosition)
        binding.tvSongStop.text = createTime(mediaPlayerService.duration)
    }

    private fun setSongDetails(song: Song?){

        val animation = AnimationUtils.loadAnimation(requireActivity().application,R.anim.fade_in)

        val animation1 = AnimationUtils.loadAnimation(requireActivity().application,R.anim.fade_out)
        binding.ivImg.animation = animation1
        binding.tvAlbum.animation = animation1
        binding.tvTitle.animation = animation1
        binding.tvArtist.animation = animation1
        currentSongInstance = song
        var song1 = song
        if(mediaPlayerService.isPlaying){
            song1 = currentSongInstance
        }
        binding.ivImg.setImageResource(song1!!.img)
        binding.tvTitle.text = song1.title
        binding.tvArtist.text = song1.artist
        binding.tvAlbum.text = song1.album
        binding.tvSongStop.text = createTime(mediaPlayerService.duration)
        binding.ivImg.animation = animation
        binding.tvAlbum.animation = animation
        binding.tvTitle.animation = animation
        binding.tvArtist.animation = animation
    }

    private fun setUi(){
        setSongDetails(currentSongInstance)
    }


    private fun createTime(duration: Int): String{
        var time = ""
        val min = duration/1000/60
        val sec = duration/1000%60

        time += "$min:"
        if(sec<10){
            time+="0"
        }
        time+=sec
        return time
    }

    override fun onBackKeyPressed() {
        val slideIn = AnimationUtils.loadAnimation(requireActivity().application,R.anim.slide_down)
        binding.root.animation = slideIn
        MainActivity.isMiniPlayerActive.value = true
    }

    override fun onMusicStop() {
        NotificationHandler.notificationManager?.cancel(0)
        mService.stopSelf()
    }

    private fun isFav(songViewModel: SongViewModel, user: User?):Boolean{
        val fav = songViewModel.getFavForUser(user!!.email)
        return if(fav[0].favList.contains(currentSongInstance)){
            binding.ivFav.setImageResource(R.drawable.heart)
            true
        } else{
            binding.ivFav.setImageResource(R.drawable.heart_un)
            false
        }
    }


}