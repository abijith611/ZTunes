package com.example.mymusicapplication.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.R
import com.example.mymusicapplication.adapter.MyRecyclerViewAdapter
import com.example.mymusicapplication.databinding.FragmentSearchBinding

import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.receiver.NotificationReceiver
import com.example.mymusicapplication.receiver.NotificationReceiver.Companion.ACTION_FAV
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.transition.MaterialFadeThrough


class SearchFragment : Fragment() {
    companion object{
        val itemSelected = MutableLiveData(false)
    }
    var UNSORTED = "unsorted"
    var ASCENDING = "ascending"
    var DESCENDING = "descending"
    var scrollPosition=0
    var topViewRV = 0
    lateinit var binding: FragmentSearchBinding


    @SuppressLint("RestrictedApi", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        enterTransition = MaterialFadeThrough()
        //exitTransition = MaterialFadeThrough()
        var titleSTATE = "unsorted"
        var artistSTATE = "unsorted"
        var albumSTATE = "unsorted"
        binding = FragmentSearchBinding.inflate(layoutInflater)
        Log.i("SearchFragment","onCreate")
        binding.noResultLayout.visibility = View.INVISIBLE
        val user = activity?.intent?.getParcelableExtra<User>("user")
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        var songs = emptyArray<Song>()
       var adapter = MyRecyclerViewAdapter(songs.toMutableList(),songViewModel,false,user!!)
//        binding.rvSearch.adapter = adapter
//        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)

        binding.rvSearch.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollPosition = (binding.rvSearch.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()!!
                val v = (binding.rvSearch.layoutManager as? LinearLayoutManager)?.getChildAt(0)
                topViewRV = if(v==null) 0 else v.top - (binding.rvSearch.layoutManager as? LinearLayoutManager)?.paddingTop!!
            }
        })
        Log.i("position",scrollPosition.toString())
        NotificationReceiver.STATE.observe(viewLifecycleOwner){
            if(it.equals("PREVIOUS") || it.equals("NEXT")){
                if(binding.rvSearch.adapter!=null)
                (binding.rvSearch.adapter as RecyclerView.Adapter).notifyDataSetChanged()
                scrollToPosition(scrollPosition, topViewRV)
            }
            if(it==ACTION_FAV){
                if(binding.rvSearch.adapter!=null)
                (binding.rvSearch.adapter as RecyclerView.Adapter).notifyDataSetChanged()
            }
        }

        MusicService.songState.observe(viewLifecycleOwner){
            if(it.equals("PREV") || it.equals("NEXT")){
                (binding.rvSearch.adapter as RecyclerView.Adapter?)?.notifyDataSetChanged()
                //scrollToPosition(scrollPosition, topViewRV)
            }
            if(it== NotificationReceiver.ACTION_FAV){
                (binding.rvSearch.adapter as RecyclerView.Adapter?)?.notifyDataSetChanged()
            }
        }

        MusicService.songChanged.observe(viewLifecycleOwner){
            (binding.rvSearch.adapter as RecyclerView.Adapter?)?.notifyDataSetChanged()
        }
//        itemSelected.observe(viewLifecycleOwner){
//            if(it==true){
//                adapter =MyRecyclerViewAdapter(songs.toList() ,songViewModel,false,user)
//                binding.rvSearch.adapter = adapter
//                binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
//                scrollToPosition(scrollPosition, topViewRV)
//            }
//        }
        binding.etSearch.addTextChangedListener (object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                titleSTATE = UNSORTED
                artistSTATE = UNSORTED
                albumSTATE = UNSORTED
                    val searchText = binding.etSearch.text.toString()
                    Log.i("search text",searchText)
                    songs= songViewModel.getSearchSongs(searchText)

                Log.i("searched song", songs.toList().toString())
                    if(songs.isNotEmpty()) {
                        binding.genreView.visibility = View.INVISIBLE
                        binding.noResultLayout.visibility = View.INVISIBLE
                        Log.i("searched song", songs.toList().toString())
                        adapter = MyRecyclerViewAdapter(songs.toMutableList(),songViewModel,false, user)
                        binding.rvSearch.adapter = adapter
                        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
                        adapter.notifyDataSetChanged()
                    }
                    else{
                        binding.genreView.visibility = View.INVISIBLE
                        binding.noResultLayout.visibility = View.VISIBLE
                        binding.tvNoResult.text = "No results found for \"$searchText\""
                        adapter = MyRecyclerViewAdapter(mutableListOf(),songViewModel,false, user)
                        binding.rvSearch.adapter = adapter
                        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
                        adapter.notifyDataSetChanged()
                    }
                if(searchText.isEmpty()){
                    binding.genreView.visibility = View.VISIBLE
                    songs = emptyArray()
                    adapter = MyRecyclerViewAdapter(mutableListOf(),songViewModel,false, user)
                    binding.rvSearch.adapter = adapter
                    binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
                    adapter.notifyDataSetChanged()
                }
            }

        })


        MainActivity.isMiniPlayerActive.observe(viewLifecycleOwner){
            Log.i("Mini player status", it.toString())
            val scale = resources.displayMetrics.density
            val sizeDp = 150
            val padding = sizeDp*scale+0.5f
            if(MainActivity.isMiniPlayerActive.value == true){
                binding.rvSearch.setPadding(0,0,0,padding.toInt())
            }
        }




        binding.sort.setOnClickListener { it ->
            val popup = PopupMenu(it.context, it)
                popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.item1->{
                        songs = sortSongs(songs, "title").toTypedArray()
                        when (titleSTATE) {
                            DESCENDING -> {
                                titleSTATE = ASCENDING
                            }
                            ASCENDING -> {
                                //count1++
                                //if (count1 % 2 == 0) {
                                songs = songs.reversedArray()
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
                        //songs = songs.reversedArray()
                        adapter =MyRecyclerViewAdapter(songs.toMutableList(),songViewModel,false,user!!)
                        binding.rvSearch.adapter = adapter
                        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
                        adapter.notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.item2->{
                        songs = sortSongs(songs, "album").toTypedArray()
                        when (albumSTATE) {
                            DESCENDING -> {
                                albumSTATE = ASCENDING
                            }
                            ASCENDING -> {
                                //count1++
                                //if (count1 % 2 == 0) {
                                songs = songs.reversedArray()
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
                        adapter =MyRecyclerViewAdapter(songs.toMutableList(),songViewModel,false,user!!)
                        binding.rvSearch.adapter = adapter
                        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
                        adapter.notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.item3->{
                        songs = sortSongs(songs, "artist").toTypedArray()
                        when (artistSTATE) {
                            DESCENDING -> {
                                artistSTATE = ASCENDING
                            }
                            ASCENDING -> {
                                //count1++
                                //if (count1 % 2 == 0) {
                                songs = songs.reversedArray()
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

                        adapter =MyRecyclerViewAdapter(songs.toMutableList(),songViewModel,false,user !!)
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
        binding.cardEDM.setOnClickListener{
            openFragment("EDM")
        }

        binding.cardRomantic.setOnClickListener{
            openFragment("Romantic")
        }

        binding.cardParty.setOnClickListener{
            openFragment("Party")
        }
        binding.cardPop.setOnClickListener{
            openFragment("Popular")
        }

        binding.cardEd.setOnClickListener{
            openFragmentForArtist("Ed Sheeran")
        }

        binding.cardTaylor.setOnClickListener{
            openFragmentForArtist("Taylor Swift")
        }

        binding.cardWeeknd.setOnClickListener{
            openFragmentForArtist("The Weeknd")
        }
        binding.cardKaty.setOnClickListener{
            openFragmentForArtist("Katy Perry")
        }


//        binding.order.setOnClickListener { it->
//            val popup2 = PopupMenu(requireActivity().application, it)
//            popup2.setOnMenuItemClickListener {
//                when(it.itemId){
//                    R.id.item1->{
//                        if(!isOrder){
//                            songs = songs.reversedArray()
//                            isOrder = true
//                        }
//                        adapter =MyRecyclerViewAdapter(songs.toList() )
//                        binding.rvSearch.adapter = adapter
//                        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
//                        adapter.notifyDataSetChanged()
//                        return@setOnMenuItemClickListener true
//                    }
//                    R.id.item2->{
//                        if(isOrder){
//                            songs = songs.reversedArray()
//                            isOrder = false
//                        }
//
//                        adapter =MyRecyclerViewAdapter(songs.toList() )
//                        binding.rvSearch.adapter = adapter
//                        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
//                        adapter.notifyDataSetChanged()
//                        return@setOnMenuItemClickListener true
//                    }
//                    else -> {
//                        return@setOnMenuItemClickListener false
//                    }
//                }
//            }
//            popup2.inflate(R.menu.popup_menu_2)
//            popup2.show()
//
//        }



        // Inflate the layout for this fragment
        return binding.root
    }

    private fun scrollToPosition(position: Int, offset: Int = 0){
        binding.rvSearch.stopScroll()
        (binding.rvSearch.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, offset)

    }

    private fun sortSongs(songs: Array<Song>, field: String): List<Song>{
        var sortedList = emptyList<Song>()
        when(field){
            "title"->{
                sortedList = songs.sortedWith(compareBy { it.title })
            }
            "album"->{
                sortedList = songs.sortedWith(compareBy { it.album })
            }
            "artist"->{
                sortedList = songs.sortedWith(compareBy { it.artist })
            }
        }
        return sortedList
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i("onSave", "${scrollPosition}, $topViewRV")
        outState.putInt("scrollPosition",scrollPosition)
        outState.putInt("topViewRV",topViewRV)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if(savedInstanceState!=null){
            val scrollPosition = savedInstanceState.getInt("scrollPosition")
            val topViewRV = savedInstanceState.getInt("topViewRV")
            Log.i("onRestore", "${scrollPosition}, $topViewRV")
            scrollToPosition(scrollPosition, topViewRV)
        }
    }

    private fun openFragment(genre: String){
        val bundle = Bundle()
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        val genreSongs=songViewModel.getGenreSongs(genre)
        bundle.putParcelableArrayList("songs",genreSongs.toList() as ArrayList)
        bundle.putString("genre", genre)
        bundle.putString("type","genre")
        val frag = ListFragment()
        frag.arguments = bundle
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer,frag)?.addToBackStack(null)?.commit()
    }

    private fun openFragmentForArtist(artist: String){
        val bundle = Bundle()
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        val artistSongs=songViewModel.getArtistSongs(artist)
        bundle.putParcelableArrayList("songs",artistSongs.toList() as ArrayList)
        bundle.putString("genre", artist)
        bundle.putString("type","artist")
        val frag = ListFragment()
        frag.arguments = bundle
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer,frag)?.addToBackStack(null)?.commit()
    }



}