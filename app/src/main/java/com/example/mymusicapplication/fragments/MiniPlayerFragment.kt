package com.example.mymusicapplication.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mymusicapplication.R
import com.example.mymusicapplication.databinding.FragmentMiniPlayerBinding
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.receiver.NotificationReceiver
import com.example.mymusicapplication.receiver.NotificationReceiver.Companion.ACTION_NEXT
import com.example.mymusicapplication.receiver.NotificationReceiver.Companion.ACTION_PREV
import com.example.mymusicapplication.service.MusicService
import com.google.android.material.transition.MaterialFadeThrough


class MiniPlayerFragment : Fragment(), GestureDetector.OnGestureListener{

    private lateinit var gestureDetector: GestureDetector
    private var x2:Float = 0.0f
    private var x1:Float = 0.0f
    private var y2:Float = 0.0f
    private var y1:Float = 0.0f

    companion object{
        const val MIN_DISTANCE = 150

    }

    lateinit var binding: FragmentMiniPlayerBinding
    var song: Song? = null
    var songList: ArrayList<Song>? = null
    var position:Int? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //enterTransition = MaterialFadeThrough()

        val mService = MusicService()
        binding = FragmentMiniPlayerBinding.inflate(layoutInflater)
        binding.tvTitle.isSelected = true
        song = arguments?.getParcelable("song") as Song?
        songList = arguments?.getParcelableArrayList("songList")
        position = arguments?.get("position") as Int?
        val string = arguments?.getString("detail")
        if(string=="true"){
            val fade = AnimationUtils.loadAnimation(requireActivity().application, R.anim.miniplayer)
            binding.root.animation = fade
        }
        else{
            enterTransition = MaterialFadeThrough()
        }



        gestureDetector = GestureDetector(requireActivity().application, this)


        NotificationReceiver.STATE.observe(viewLifecycleOwner){
            if(it== ACTION_NEXT || it== ACTION_PREV){
                    binding.ivPlayMini.setImageResource(R.drawable.ic_pause)
                    setSong(binding)
                    song = MusicService.currentSongInstance
            }
        }
        MusicService.songState.observe(viewLifecycleOwner){
            if(it=="NEXT" || it=="PREV"){
                binding.ivPlayMini.setImageResource(R.drawable.ic_pause)
                setSong(binding)
                song = MusicService.currentSongInstance
            }
        }


        Log.i("arguments","${song.toString()} --${songList.toString()} --${position.toString()}")
        //binding.ivPlayMini.setImageResource(R.drawable.ic_pause)
        setSong(binding)
        binding.ivPlayMini.setOnClickListener{
            mService.play(context)
        }

        MusicService.STATE.observe(viewLifecycleOwner){
            when(it){
                "PAUSE"->{
                    binding.ivPlayMini.setImageResource(R.drawable.ic_play)
                }
                "PLAY"->{
                    binding.ivPlayMini.setImageResource(R.drawable.ic_pause)
                }
            }
        }

        val handler = Handler(Looper.getMainLooper())
        val delay = 250L
        handler.postDelayed(object: Runnable{
            override fun run() {
                try{
                    if(MusicService.mediaPlayerService.currentPosition <= MusicService.mediaPlayerService.duration) {
                        setSeekbarPosition(binding)
                        //Log.i("DetailFragment", "${mediaPlayerService.currentPosition} ${mediaPlayerService.duration}")
                    }
                }
                catch (e:Exception){
                    Log.i("ex", e.printStackTrace().toString())                }

                handler.postDelayed(this,delay)
            }
        },delay)

        binding.seekBar.setOnTouchListener{_,_->
            return@setOnTouchListener true
        }




        binding.cardSong.transitionName = song?.title
        binding.root.setOnTouchListener { _, event ->
            Log.i("touchListener", "listener")
            when(event.action) {
                0->{
                    x1 = event.x
                    y1 = event.y
                }
                1->{
                    x2 = event.x
                    y2 = event.y
                    val xVal = x2-x1
                    val yVal = y2-y1
                    if(kotlin.math.abs(xVal) > MIN_DISTANCE){

                        if(x2 > x1){
                            val fadeIn = AnimationUtils.loadAnimation(requireActivity().application,R.anim.fade_in)
                            prev(mService)
                            binding.imageView.animation = fadeIn
                            binding.tvTitle.animation = fadeIn
                            binding.tvArtist.animation = fadeIn
                        }
                        else{

                            val fadeIn = AnimationUtils.loadAnimation(requireActivity().application,R.anim.fade_in)
                            next(mService)
                            binding.imageView.animation = fadeIn
                            binding.tvTitle.animation = fadeIn
                            binding.tvArtist.animation = fadeIn
                        }
                    }
                    else if(kotlin.math.abs(yVal) > MIN_DISTANCE){
                        if(y2 > y1){
                            Log.i("gesture","down")
                        }
                        else{
                            open(binding.cardSong)
                        }
                    }
                    else if(kotlin.math.abs(x1 - x2) ==0.0f && kotlin.math.abs(y1 - y2) ==0.0f){
                        open(binding.cardSong)
                    }
                }
//                MotionEvent.ACTION_MOVE -> {
//                    gestureDetector.onTouchEvent(event)
//                    Log.i("Gesture", "${event.x} ${event.y}")
//                }
            }
            true
        }




        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setSong(bindingVal: FragmentMiniPlayerBinding){
        bindingVal.imageView.setImageResource(MusicService.currentSongInstance!!.img)
        bindingVal.tvTitle.text = MusicService.currentSongInstance!!.title
        bindingVal.tvArtist.text = MusicService.currentSongInstance!!.artist
    }

    override fun onDown(e: MotionEvent?): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
        //TODO("Not yet implemented")
        //return false
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        //TODO("Not yet implemented")
        //return false
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        //TODO("Not yet implemented")
        return false
    }

    fun next(mService: MusicService){
        mService.next(context)
        binding.ivPlayMini.setImageResource(R.drawable.ic_pause)
        NotificationReceiver.STATE.value = ACTION_NEXT
        setSong(binding)
        song = MusicService.currentSongInstance
    }

    private fun prev(mService: MusicService){
        mService.prev(context)
        binding.ivPlayMini.setImageResource(R.drawable.ic_pause)
        NotificationReceiver.STATE.value = ACTION_PREV
        setSong(binding)
        song = MusicService.currentSongInstance
    }

    private fun open(view: View){
            val detailFragment = DetailFragment()
            val bundle = Bundle()
            bundle.putParcelable("song", MusicService.currentSongInstance)
            bundle.putParcelableArrayList("songList",songList)
            bundle.putInt("position",position!!)
            detailFragment.arguments = bundle
            val activity = view.context as AppCompatActivity
            activity.supportFragmentManager.beginTransaction()
                .addSharedElement(binding.cardSong, song?.title.toString())
                .add(R.id.fragmentContainer,detailFragment).addToBackStack(null).commit()

    }

    private fun setSeekbarPosition(binding: FragmentMiniPlayerBinding){
        binding.seekBar.max = MusicService.mediaPlayerService.duration
        binding.seekBar.progress = MusicService.mediaPlayerService.currentPosition
    }








}