package com.example.mymusicapplication.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.adapter.MyRecyclerViewAdapter
import com.example.mymusicapplication.databinding.FragmentLimitedListBinding
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.receiver.NotificationReceiver
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.transition.MaterialSharedAxis


class LimitedListFragment : Fragment() {
    lateinit var binding:FragmentLimitedListBinding


    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y,true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y,false)
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        binding = FragmentLimitedListBinding.inflate(layoutInflater)
        val user = activity?.intent?.getParcelableExtra<User>("user")
        var songList = user?.let { getSongList(songViewModel, it) }

        if (songList != null) {
            binding.rvList.adapter =
                user?.let { MyRecyclerViewAdapter(songList!!,songViewModel, false, it) }
        }
        binding.rvList.layoutManager = LinearLayoutManager(requireActivity().parent)



        MusicService.songChanged.observe(viewLifecycleOwner){
            if(it==true){
                (binding.rvList.adapter as RecyclerView.Adapter).notifyDataSetChanged()
            }
        }

        MusicService.songState.observe(viewLifecycleOwner){ it ->
            if(it=="NEXT" || it=="PREV"){
                 songList = user?.let { getSongList(songViewModel, it) }

                if (songList != null) {
                    binding.rvList.adapter =
                        user?.let { it1 ->
                            MyRecyclerViewAdapter(
                                songList!!,songViewModel, false,
                                it1
                            )
                        }
                }
                binding.rvList.layoutManager = LinearLayoutManager(requireActivity().parent)
            }
        }

        MainActivity.isMiniPlayerActive.observe(viewLifecycleOwner){
            Log.i("Mini player status", it.toString())
            val scale = resources.displayMetrics.density
            val sizeDp = 165
            val padding = sizeDp*scale+0.5f
            if(MainActivity.isMiniPlayerActive.value == true){
                Log.i("setPadding", "setPadding")
                binding.rvList.setPadding(0,10,0,padding.toInt())
            }
        }

        NotificationReceiver.STATE.observe(viewLifecycleOwner){
            if(it.equals("PREVIOUS") || it.equals("NEXT")){
                (binding.rvList.adapter as RecyclerView.Adapter).notifyDataSetChanged()
                //scrollToPosition(scrollPosition, topViewRV)
            }
            if(it== NotificationReceiver.ACTION_FAV){
                (binding.rvList.adapter as RecyclerView.Adapter).notifyDataSetChanged()
            }
        }
        MusicService.songState.observe(viewLifecycleOwner){
            if(it.equals("PREV") || it.equals("NEXT")){
                (binding.rvList.adapter as RecyclerView.Adapter?)?.notifyDataSetChanged()
                //scrollToPosition(scrollPosition, topViewRV)
            }
            if(it== NotificationReceiver.ACTION_FAV){
                (binding.rvList.adapter as RecyclerView.Adapter?)?.notifyDataSetChanged()
            }
        }

        MusicService.songChanged.observe(viewLifecycleOwner){
            (binding.rvList.adapter as RecyclerView.Adapter?)?.notifyDataSetChanged()
        }

        return binding.root
    }
    private fun count(song: Song, songList: ArrayList<Song>): Int{
        var count = 0
        for(songI in songList){
            if(songI==song){
                count++
            }
        }
        return count
    }

    private fun getSongList(songViewModel: SongViewModel, user: User): ArrayList<Song>{
        val songData =songViewModel.getSongData(user.email)
        var songList = arrayListOf<Song>()
        Log.i("HomeSongData", songList.toList().toString())
        val title = arguments?.getString("title")
        if(title == "recent"){
            binding.textTitle.text = "Recently Played"
            songList = songData[0].recentSongList
            songList.reverse()
        }
        else if(title == "frequent"){
            binding.textTitle.text = "Most Listened Songs"
            songList = songData[0].frequentSongList
            val hashMap = hashMapOf<Song, Int>()
            for(song in songList){
                val c =count(song, songList)
                hashMap[song] = c
            }
            val result = hashMap.toList().sortedBy{ (_,value) -> value}.toMap()
            //result = result.toList().reversed().toMap()
            val arrayList = arrayListOf<Song>()
            for(entry in result){
                arrayList.add(entry.key)
            }
            songList = arrayList
            songList.reverse()
            Log.i("songListFreq", songList.toString())
        }

        if(songList.size>10){
            val mainList = arrayListOf<Song>()
            val tempList = songList.subList(0,9)
            for(song in tempList){
                mainList.add(song)
            }
            songList = mainList
        }

        if(songList.isEmpty()){
            binding.noResultLayout.visibility = View.VISIBLE
        }
        else{
            binding.noResultLayout.visibility = View.INVISIBLE
        }
        return songList
    }


}