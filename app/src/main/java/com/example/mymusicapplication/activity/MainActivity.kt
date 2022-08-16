package com.example.mymusicapplication.activity


import android.content.Context
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.mymusicapplication.listener.BackKeyListener
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.service.MusicService.Companion.STATE
import com.example.mymusicapplication.service.MusicService.Companion.currentSongInstance
import com.example.mymusicapplication.service.MusicService.Companion.mediaPlayerService
import com.example.mymusicapplication.listener.MusicServiceListener
import com.example.mymusicapplication.R
import com.example.mymusicapplication.databinding.ActivityMainBinding

import com.example.mymusicapplication.db.Song


import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.fragments.*
import com.example.mymusicapplication.notification.NotificationHandler
import com.example.mymusicapplication.receiver.AudioBecomingNoisyReceiver
import com.example.mymusicapplication.service.MusicService.Companion.isShuffleOn
import com.example.mymusicapplication.service.MusicService.Companion.nextPlayingList
import com.example.mymusicapplication.service.MusicService.Companion.randomList
import com.example.mymusicapplication.service.MusicService.Companion.songChanged
import com.example.mymusicapplication.service.MusicService.Companion.songList
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.math.min
import kotlin.random.Random

class MainActivity : AppCompatActivity(), AudioManager.OnAudioFocusChangeListener {
    companion object{
        val notActive = MutableLiveData(false)
        var isMiniPlayerActive = MutableLiveData(false)
        var snackBarAnchor:Int = R.id.menu
    }
    private var backPressed = MutableLiveData(false)
    var backPressCounter = 0

    private lateinit var audioBecomingNoisyReceiver: AudioBecomingNoisyReceiver
    private lateinit var intentFilter: IntentFilter

    private var audioManager: AudioManager? = null
   private var listener: BackKeyListener? = null
    private var mListener: MusicServiceListener? = null

    private lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
//        BackStackQueue.backStackQueue.clear()
//        BackStackQueue.backStackQueue.add(R.id.miHome)
        notActive.value = true
        audioBecomingNoisyReceiver = AudioBecomingNoisyReceiver()
        intentFilter= IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(audioBecomingNoisyReceiver,intentFilter)
        Log.i("MainActivity", "onCreate")



        val dao = SongDatabase.getInstance(this).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]







        val audioRequest=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(this)
            .build()

        audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        STATE.observe(this){
            if(it=="PLAY"){
                Log.i("Focus","PLAY")
                val requestAudioFocusResult = audioManager!!.requestAudioFocus(audioRequest)
                if(requestAudioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                    Log.i("Focus","no request")
                    mediaPlayerService.pause()
                    STATE.value = "PAUSE"
                }
            }
            else if(it=="PAUSE"){
                Log.i("Focus","PAUSE")
                audioManager!!.abandonAudioFocusRequest(audioRequest)
            }
        }


        val user = intent.getParcelableExtra<User>("user")
        if(user!=null)
        Toast.makeText(this,"Logged in as ${user.email}",Toast.LENGTH_LONG).show()


        MusicService.songChanged.observe(this){
            if(it==true){
                val songData =songViewModel.getSongData(user?.email)
                currentSongInstance?.let { it1 -> songViewModel.addSongData(songData[0], it1) }
                Log.i("SongData", songViewModel.getSongData(user?.email).toList().toString())
            }
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        window.statusBarColor = Color.parseColor("#000000")
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)

        supportFragmentManager.addOnBackStackChangedListener {
//            if(backPressed.value==true){
//                backPressed.value = false
//                return@addOnBackStackChangedListener
//            }
            backPressCounter = 0
//            Log.i("MainActivity","OnBackStackChanged")
//            for(ele in BackStackQueue.backStackQueue){
//                Log.i("MenuList1",binding.menu.menu.findItem(ele).title.toString())
//            }
//            BackStackQueue.backStackQueue.add(binding.menu.selectedItemId)
//            binding.menu.menu.findItem(BackStackQueue.backStackQueue[BackStackQueue.backStackQueue.size-1]).isChecked = true
        }

        setContentView(binding.root)
        val homeFragment = HomeFragment()
        val searchFragment = SearchFragment()
        val albumFragment = AlbumFragment()
        val accountFragment = AccountFragment()

        supportActionBar?.hide()

        isMiniPlayerActive.observe(this){
            Log.i("Mini player status", it.toString())
            if(it == true){
                binding.miniPlayerFragmentContainer.visibility = View.VISIBLE
                snackBarAnchor = R.id.miniPlayerFragmentContainer
            }
            else{
                binding.miniPlayerFragmentContainer.visibility = View.INVISIBLE
                snackBarAnchor = R.id.menu
            }
        }

//        isMiniPlayerActive.observe(this){
//            snackbarAnchor = if(it==true){
//                R.id.main_snack_bar
//            } else{
//                R.id.menu
//            }
//        }

        binding.menu.setOnItemSelectedListener{
            Log.i("BottomNavSelected", it.itemId.toString())
            when(it.itemId){
                R.id.miHome -> setCurrentFragment(homeFragment)
                R.id.miSearch -> setCurrentFragment(searchFragment)
                R.id.miAlbum -> setCurrentFragment(albumFragment)
                R.id.miAccount ->setCurrentFragment(accountFragment)
            }
            return@setOnItemSelectedListener true
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            Log.i("MainActivity", fragment.toString())
            when(fragment){
                is HomeFragment->binding.menu.menu.findItem(R.id.miHome).isChecked = true
                is SearchFragment->binding.menu.menu.findItem(R.id.miSearch).isChecked = true
                is AlbumFragment->binding.menu.menu.findItem(R.id.miAlbum).isChecked = true
                is AccountFragment ->binding.menu.menu.findItem(R.id.miAccount).isChecked = true
            }
        }

//        MusicService.isShuffleOn.observe(this){
//            if(it==true){
//                randomList = generateShuffleList(MusicService.songList.value)
//            }
//        }

//        ListFragment.listSongs.observe(this){
//            if(it!=null)
//            randomList = generateShuffleList(it)
//        }
//
        MusicService.songList.observe(this){
            if(it!=null){
                Log.i("songListChanged", it.size.toString())
            }
        }

        MusicService.isShuffleOn.observe(this){
            Log.i("isShuffle",it.toString())
            if(it==true){
                randomList = generateShuffleList(MusicService.songList.value)
                nextPlayingList = randomList
            }
            else{
                randomList = null
                nextPlayingList = MusicService.songList.value
            }

        }

        MusicService.songChanged.observe(this){
            if(it==true){
                Log.i("MusicService.songList",MusicService.songList.value.toString())
                if(MusicService.songList.value!=null) {
                    nextPlayingList = MusicService.songList.value
                    Log.i("MainActivityQ", nextPlayingList.toString())
                }
                if(MusicService.songList.value!=null && randomList!=null) {
                    Log.i("MainActivityQ", nextPlayingList.toString())
                    if (!checkList(MusicService.songList.value, randomList)) {
                        randomList = generateShuffleList(MusicService.songList.value)
                        nextPlayingList = randomList
                    }
                }




            }
        }






//        val op = intent.getStringExtra("fragment")
//        Log.i("op", op.toString())
//        if(op == "detail"){
//            Log.i("op", "inside")
//            supportFragmentManager.beginTransaction().replace(R.id.fragmentList, DetailFragment()).addToBackStack(null).commit()
//        }

    }

    fun checkList(value: List<Song>?, randomList: List<Song>?):Boolean {
        if(value!!.size!=randomList!!.size){
            Log.i("check","sizeNot equal ${value.size} ${randomList.size}")
            return false
        }
            for(song in value){
                if(!randomList.contains(song)){
                    Log.i("check","song not in second")
                    return false
                }
            }
        return true
    }

    fun generateShuffleList(songList: List<Song>?):List<Song> {
        var randomList = ArrayList<Song>()
        while(randomList.size < songList!!.size){
            var r = Random.nextInt(0, songList.size)
            if (!randomList.contains(songList[r])) {
                randomList.add(songList[r])
            }
        }
        Log.i("randomList", randomList.toString())
        return randomList

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        registerReceiver(audioBecomingNoisyReceiver,intentFilter)
        Log.i("MainActivity", "onDestroy")
        mListener?.onMusicStop()
        //NotificationHandler(applicationContext).hello()
        super.onDestroy()
    }

    override fun onStop() {
        notActive.value = true
        Log.i("MainActivity", "onStop")
        //mListener?.onMusicStop()
        super.onStop()
    }

    override fun onStart() {
        Log.i("MainActivity", "onStart")
        notActive.value = false
        super.onStart()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRestart() {
//        BackStackQueue.backStackQueue.clear()
//        BackStackQueue.backStackQueue.add(R.id.miHome)
        if(mediaPlayerService.isPlaying)
            NotificationHandler(application).showNotification(R.drawable.ic_simple_pause_,1F)
        notActive.value = true
        Log.i("MainActivity", "onRestart")
        //NotificationHandler(applicationContext).showNotification(R.drawable.ic_simple_pause_, 1F)
        super.onRestart()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        if(mediaPlayerService.isPlaying)
            NotificationHandler(application).showNotification(R.drawable.ic_simple_pause_,1F)
        Log.i("MainActivity", "onPause")
        super.onPause()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        Log.i("MainActivity", "onResume")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCurrentFragment(fragment: Fragment){
        val tag = assignTag(fragment)
        if(mediaPlayerService.isPlaying || MusicService.STATE.value=="PAUSE"){
            isMiniPlayerActive.value = true
        }
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
                .addToBackStack(tag)
                .commit()
        }
        if(mediaPlayerService.isPlaying)
            NotificationHandler(application).showNotification(R.drawable.ic_simple_pause_,1F)
        //supportFragmentManager.executePendingTransactions()
    }

    fun setBackKeyListener(listener: BackKeyListener){
        this.listener = listener
    }

    fun setMusicServiceListener(listener: MusicServiceListener){
        this.mListener = listener
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {

        if(isMainFragment()){
            if(backPressCounter < 1){
                Toast.makeText(this, "Press back again to exit!",Toast.LENGTH_SHORT).show()
                backPressCounter++
            }
            else{
                super.onPause()
                moveTaskToBack(true)
                backPressCounter = 0
            }
        }

        else {
//            backPressed.value = true
//            for (ele in BackStackQueue.backStackQueue) {
//                Log.i("MenuList", binding.menu.menu.findItem(ele).title.toString())
//            }
//            BackStackQueue.backStackQueue.removeLast()
//            Log.i("BackStackQ", BackStackQueue.backStackQueue.toString())
//            //val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
//            Log.i("MainActivity", "back pressed")
//                binding.menu.menu.findItem(BackStackQueue.backStackQueue[BackStackQueue.backStackQueue.size - 1]).isChecked = true
//                Log.i("MenuItem", binding.menu.menu.findItem(BackStackQueue.backStackQueue[BackStackQueue.backStackQueue.size - 1]).title.toString())
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if(fragment is DetailFragment) {
                listener?.onBackKeyPressed()
                val miniFrag =supportFragmentManager.findFragmentById(R.id.miniPlayerFragmentContainer)
                val replaceFrag = MiniPlayerFragment()
                if (miniFrag != null) {
                    miniFrag.arguments?.putString("detail","true")
                    replaceFrag.arguments = miniFrag.arguments
                        supportFragmentManager.beginTransaction().replace(R.id.miniPlayerFragmentContainer,replaceFrag).commit()
                }
            }
            super.onBackPressed()
            if (mediaPlayerService.isPlaying)
                    NotificationHandler(application).showNotification(
                        R.drawable.ic_simple_pause_,
                        1F
                    )
        }

//        Log.i("MainActivityBackStack",supportFragmentManager.backStackEntryCount.toString())

    }

    private fun assignTag(fragment: Fragment): String{
        return when(fragment){
            is HomeFragment-> "home"
            is SearchFragment-> "search"
            is AlbumFragment-> "album"
            is AccountFragment -> "account"
            else -> "home"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onAudioFocusChange(focusChange: Int) {
        when(focusChange){
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT-> {
                Log.i("AudioFocus", "loss tran")
                STATE.value = "PAUSE"
                    mediaPlayerService.pause()
                    NotificationHandler(application).showNotification(
                        R.drawable.ic_simple_play_,
                        0F
                    )
            }
            AudioManager.AUDIOFOCUS_GAIN-> {
                Log.i("AudioFocus", "gain")
                STATE.value = "PLAY"
                    mediaPlayerService.start()
                    NotificationHandler(application).showNotification(
                        R.drawable.ic_simple_pause_,
                        1F
                    )

            }
            AudioManager.AUDIOFOCUS_LOSS-> {
                Log.i("AudioFocus", "loss")
                STATE.value = "PAUSE"
               // mediaPlayerService.pause()
//                NotificationHandler(application).showNotification(
//                    R.drawable.ic_simple_play_,
//                    0F
//                )
            }
        }
    }

    private fun isMainFragment():Boolean{
        return when(supportFragmentManager.findFragmentById(R.id.fragmentContainer)){
            is HomeFragment-> true
            is SearchFragment-> true
            is AlbumFragment-> true
            is AccountFragment -> true
            else-> false
        }
    }


}