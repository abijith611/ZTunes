package com.example.mymusicapplication.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.adapter.LoginAdapter
import com.example.mymusicapplication.databinding.ActivityLoginBinding
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.tabs.TabLayout

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        supportActionBar?.hide()
        window.statusBarColor = Color.parseColor("#000000")
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val dao = SongDatabase.getInstance(this).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        Log.i("userlog", songViewModel.getAllUserLog().toList().toString())
        if(songViewModel.getAllUserLog().isNotEmpty()) {
            val userLog = songViewModel.getAllUserLog().last()
            Log.i("user", userLog.toString())

            if (userLog.isLoggedIn) {
                val user = songViewModel.getUser(userLog.email)
                Intent(this, MainActivity::class.java).apply {
                    songViewModel.userLogIn(user!!)
                    this.putExtra("user", user)
                    startActivity(this)
                    finish()
                }
            }
        }

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Login"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Sign Up"))
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = LoginAdapter(this, binding.tabLayout.tabCount)
        binding.viewPager.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.viewPager.currentItem = tab.position
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

        if(MusicService.mediaPlayerService.isPlaying){
            MusicService.mediaPlayerService.stop()
            MusicService.mediaSession.release()
            MusicService.currentSongInstance = null
            MainActivity.isMiniPlayerActive.value = false
        }

    }



}