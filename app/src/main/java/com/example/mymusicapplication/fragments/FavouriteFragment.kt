package com.example.mymusicapplication.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.R
import com.example.mymusicapplication.adapter.MyRecyclerViewAdapter
import com.example.mymusicapplication.databinding.FragmentFavouriteBinding
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.receiver.NotificationReceiver
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.transition.MaterialContainerTransform


class FavouriteFragment : Fragment() {
    lateinit var binding: FragmentFavouriteBinding
    var UNSORTED = "unsorted"
    var ASCENDING = "ascending"
    var DESCENDING = "descending"

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        sharedElementEnterTransition = MaterialContainerTransform()
        var titleSTATE = "unsorted"
        var artistSTATE = "unsorted"
        var albumSTATE = "unsorted"
        binding = FragmentFavouriteBinding.inflate(layoutInflater)
        binding.noResultLayout.visibility = View.INVISIBLE
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        val user = activity?.intent?.getParcelableExtra<User>("user")
        val favSongs = songViewModel.getFavForUser(user!!.email)
        var songs = favSongs[0].favList
        if(songs.isEmpty()){
            binding.noSongsLayout.visibility = View.VISIBLE
        }
        else{
            binding.noSongsLayout.visibility = View.INVISIBLE
        }
        var adapter = MyRecyclerViewAdapter(songs, songViewModel,true, user)
        binding.rvSearch.adapter = adapter
        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().parent)
        NotificationReceiver.STATE.observe(viewLifecycleOwner){
            if(it.equals("PREVIOUS") || it.equals("NEXT")){
                adapter = MyRecyclerViewAdapter(songs, songViewModel,true, user)
                binding.rvSearch.adapter = adapter
                binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().parent)
            }
        }
        MusicService.songState.observe(viewLifecycleOwner){
            if(it.equals("PREV") || it.equals("NEXT")){
                adapter = MyRecyclerViewAdapter(songs, songViewModel,true, user)
                binding.rvSearch.adapter = adapter
                binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().parent)
            }
        }

        MusicService.songChanged.observe(viewLifecycleOwner){
            (binding.rvSearch.adapter as RecyclerView.Adapter?)?.notifyDataSetChanged()
        }

        binding.etSearch.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
                if(songs.isEmpty()){
                    if (binding.etSearch.text.isEmpty()) {
                        binding.noSongsLayout.visibility = View.VISIBLE
                        binding.noResultLayout.visibility = View.INVISIBLE
                    } else {
                        binding.noResultLayout.visibility = View.VISIBLE
                        binding.noSongsLayout.visibility = View.INVISIBLE
                    }
                    //Toast.makeText(requireActivity().application, "Cannot perform search operation",Toast.LENGTH_SHORT).show()
                    //binding.etSearch.focusable = View.NOT_FOCUSABLE
                    //return@OnFocusChangeListener
                }
            }
            else{
                binding.noSongsLayout.visibility = View.VISIBLE
            }
            }



        binding.etSearch.addTextChangedListener (object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun afterTextChanged(s: Editable?) {
                titleSTATE = UNSORTED
                artistSTATE = UNSORTED
                albumSTATE = UNSORTED
                val searchText = binding.etSearch.text.toString()
                val searchSongs = if(searchText.isNotEmpty())
                    songViewModel.getFavSearchSongs(user.email,searchText).toMutableList() as ArrayList<Song>
                else
                    songs
                if(searchSongs.isNotEmpty()) {
                    binding.noResultLayout.visibility = View.INVISIBLE
                    Log.i("searched song", searchSongs.toString())
                    val adapter1 = MyRecyclerViewAdapter(searchSongs, songViewModel,true, user)
                    binding.rvSearch.adapter = adapter1
                    binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
                    adapter1.notifyDataSetChanged()
                }
                else{
                    if(searchText.isEmpty()) {
                        binding.noResultLayout.visibility = View.INVISIBLE
                        //binding.tvNoResult.text = "No results found for \"$searchText\""
                    }
                    else
                        binding.noResultLayout.visibility = View.VISIBLE
                    val adapter1 = MyRecyclerViewAdapter(mutableListOf(),songViewModel,true, user)
                    binding.rvSearch.adapter = adapter1
                    binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
                    adapter1.notifyDataSetChanged()
                }
                if(binding.noSongsLayout.visibility == View.VISIBLE){
                    binding.noResultLayout.visibility = View.INVISIBLE
                }
            }

        })

        MainActivity.isMiniPlayerActive.observe(viewLifecycleOwner){
            Log.i("Mini player status", it.toString())
            val scale = resources.displayMetrics.density
            val sizeDp = 200
            val padding = sizeDp*scale+0.5f
            if(MainActivity.isMiniPlayerActive.value == true){
                binding.rvSearch.setPadding(0,0,0,padding.toInt())
            }
        }


        var count1 = 0
        var count2 = 0
        var count3 = 0
        binding.sort.setOnClickListener { it ->
            if(songs.isEmpty()){
                //Toast.makeText(requireActivity().application, "Cannot perform sort operation",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val popup = PopupMenu(it.context, it)
            popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.item1->{
                        songs = sortSongs(songs, "title") as ArrayList<Song>
                        when (titleSTATE) {
                            DESCENDING -> {
                                titleSTATE = ASCENDING
                            }
                            ASCENDING -> {
                                //count1++
                                //if (count1 % 2 == 0) {
                                songs.reverse()
                                titleSTATE = DESCENDING
                                //}
                            }
                            UNSORTED -> {
                                titleSTATE = ASCENDING
                            }
                        }
                        albumSTATE = UNSORTED
                        artistSTATE = UNSORTED
                        if(songs.isEmpty()){
                            titleSTATE = UNSORTED
                        }
                        adapter =MyRecyclerViewAdapter(songs,songViewModel,true, user)
                        binding.rvSearch.adapter = adapter
                        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
                        adapter.notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.item2->{
                        songs = sortSongs(songs, "album") as ArrayList<Song>
                        when (albumSTATE) {
                            DESCENDING -> {
                                albumSTATE = ASCENDING
                            }
                            ASCENDING -> {
                                //count1++
                                //if (count1 % 2 == 0) {
                                songs.reverse()
                                albumSTATE = DESCENDING
                                //}
                            }
                            UNSORTED -> {
                                albumSTATE = ASCENDING
                            }
                        }
                        titleSTATE = UNSORTED
                        artistSTATE = UNSORTED
                        if(songs.isEmpty()){
                            albumSTATE = UNSORTED
                        }
                        adapter =MyRecyclerViewAdapter(songs,songViewModel,true, user)
                        binding.rvSearch.adapter = adapter
                        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
                        adapter.notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.item3->{
                        songs = sortSongs(songs, "artist") as ArrayList<Song>
                        when (artistSTATE) {
                            DESCENDING -> {
                                artistSTATE = ASCENDING
                            }
                            ASCENDING -> {
                                //count1++
                                //if (count1 % 2 == 0) {
                                songs.reverse()
                                artistSTATE = DESCENDING
                                //}
                            }
                            UNSORTED -> {
                                artistSTATE = ASCENDING
                            }
                        }
                        titleSTATE = UNSORTED
                        albumSTATE = UNSORTED
                        if(songs.isEmpty()){
                            artistSTATE = UNSORTED
                        }
                        adapter =MyRecyclerViewAdapter(songs ,songViewModel,true, user)
                        binding.rvSearch.adapter = adapter
                        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
                        adapter.notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    else -> {
                        return@setOnMenuItemClickListener false
                    }
                }

            }
            popup.inflate(R.menu.popup_menu)
            try{
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popup)
                mPopup.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
                    .invoke(mPopup, true)
                val helper = MenuPopupHelper(requireActivity().application, popup.menu as MenuBuilder)
            }
            catch (e: Exception){
                Log.e("Main", "error showing menu icons: ", e)
            }
            val menuItemTitle = popup.menu.findItem(R.id.item1)
            val menuItemArtist = popup.menu.findItem(R.id.item3)
            val menuItemAlbum = popup.menu.findItem(R.id.item2)
            when (titleSTATE) {
                UNSORTED -> {
                    menuItemTitle.setIcon(R.drawable.ic_transparent)
                }
                ASCENDING -> {
                    menuItemTitle.setIcon(R.drawable.ic_sort)
                }
                DESCENDING -> {
                    menuItemTitle.setIcon(R.drawable.ic_sortza)
                }
            }
            when (artistSTATE) {
                UNSORTED -> {
                    menuItemArtist.setIcon(R.drawable.ic_transparent)
                }
                ASCENDING -> {
                    menuItemArtist.setIcon(R.drawable.ic_sort)
                }
                DESCENDING -> {
                    menuItemArtist.setIcon(R.drawable.ic_sortza)
                }
            }

            when (albumSTATE) {
                UNSORTED -> {
                    menuItemAlbum.setIcon(R.drawable.ic_transparent)
                }
                ASCENDING -> {
                    menuItemAlbum.setIcon(R.drawable.ic_sort)
                }
                DESCENDING -> {
                    menuItemAlbum.setIcon(R.drawable.ic_sortza)
                }
            }
            popup.gravity = Gravity.END
            popup.show()
        }


        return binding.root
    }

    private fun sortSongs(songs: ArrayList<Song>, field: String): ArrayList<Song>{
        var sortedList = emptyList<Song>()
        when(field){
            "title"->{
                sortedList= songs.sortedWith(compareBy { it.title })
            }
            "album"->{
                sortedList = songs.sortedWith(compareBy { it.album })
            }
            "artist"->{
                sortedList = songs.sortedWith(compareBy { it.artist })
            }
        }
        val arrayList = ArrayList<Song>()
        for(obj in sortedList){
            arrayList.add(obj)
        }
        return arrayList
    }

}