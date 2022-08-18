package com.example.mymusicapplication.fragments

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.NOT_FOCUSABLE
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.R
import com.example.mymusicapplication.adapter.MyPlaylistItemRecyclerViewAdapter
import com.example.mymusicapplication.databinding.FragmentListBinding
import com.example.mymusicapplication.db.*

import com.example.mymusicapplication.receiver.NotificationReceiver
import com.example.mymusicapplication.receiver.NotificationReceiver.Companion.ACTION_NEXT
import com.example.mymusicapplication.receiver.NotificationReceiver.Companion.ACTION_PREV
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class ListFragment : Fragment() {


    private lateinit var binding: FragmentListBinding
    lateinit var songViewModel: SongViewModel
    var scrollPosition=0
    var topViewRV = 0
    var nestedScrollY = 0
    private var totalDuration = ""
    var UNSORTED = "unsorted"
    var ASCENDING = "ascending"
    var DESCENDING = "descending"
    companion object{
        var swipeChange = MutableLiveData<Boolean>(false)
        var ivState = MutableLiveData<Boolean>()
        lateinit var songs: List<Song>
    }

    var collapsed:Boolean? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var titleSTATE = "unsorted"
        var artistSTATE = "unsorted"
        var albumSTATE = "unsorted"
        binding = FragmentListBinding.inflate(layoutInflater)
        binding.noResultLayout.visibility = View.INVISIBLE

        Log.i("ListFrag", "OnCreateView")
        val user = activity?.intent?.getParcelableExtra<User>("user")
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
//        songs = songViewModel.allSongs
        songs = arguments?.getParcelableArrayList<Song>("songs") as List<Song>


        val type = arguments?.getString("type")
        val genre = arguments?.getString("genre")
        sharedElementEnterTransition = MaterialContainerTransform()
        sharedElementReturnTransition = MaterialContainerTransform()
        binding.root.transitionName = genre
        val playlist = arguments?.getParcelable<Playlist>("playlist")
//        Handler(Looper.getMainLooper()).postDelayed({
//            if(totalDuration.isEmpty())
//                totalDuration = getDuration(songs)
//            binding.tvDuration.text = totalDuration
//        },0)

        setDuration(songs)

        binding.tvHeadingList.text = genre
        if(songs.isNotEmpty()){
            binding.noItemLayout.visibility = View.INVISIBLE
            binding.iv.setImageResource(songs[0].img)
            binding.appBarLayout.setExpanded(true)
        }
        else{
            binding.noItemLayout.visibility = View.VISIBLE
            binding.appBarLayout.setExpanded(false)
        }
        var adapter = MyPlaylistItemRecyclerViewAdapter(songs as ArrayList<Song>,type.toString(),songViewModel,playlist, user)
        val linearLayoutManager = LinearLayoutManager(requireActivity().application)
        GlobalScope.launch(Dispatchers.Main){

            binding.rvList.adapter = adapter
            binding.rvList.layoutManager = linearLayoutManager
        }
        ivState.observe(viewLifecycleOwner){
            if(type=="playlist"){
                val playlistSongs = songViewModel.getPlaylistForUser(user?.email)[0].playlist
                if (it == false) {
                    var playlists = songViewModel.getPlaylistForUser(user!!.email)
                    var playlist1: Playlist? = null
                    for(eachPlaylist in playlists){
                        if(eachPlaylist.name == playlist!!.name){
                            playlist1 = eachPlaylist
                        }
                    }

                    Log.i("ListFrag", songs.toString())
                    if (playlist1!!.playlist.isNotEmpty()) {
                        binding.noItemLayout.visibility = View.INVISIBLE
                        binding.iv.setImageResource(playlist1.playlist[0].img)
                        binding.appBarLayout.setExpanded(true)
                    }
                    else{
                        binding.noItemLayout.visibility = View.VISIBLE
                        binding.iv.setImageResource(0)
                        binding.appBarLayout.setExpanded(false)
                    }

                    songs = playlist1!!.playlist.toMutableList()
                    val adapter1 = MyPlaylistItemRecyclerViewAdapter(songs.toMutableList() as ArrayList<Song>,type.toString(),songViewModel,playlist, user)
                    val linearLayoutManager1 = LinearLayoutManager(requireActivity().application)
                    GlobalScope.launch(Dispatchers.Main){

                        binding.rvList.adapter = adapter1
                        binding.rvList.layoutManager = linearLayoutManager1
                    }
                    scrollToPosition(scrollPosition, topViewRV)
                    if(collapsed == true){
                        binding.appBarLayout.setExpanded(false)
                    }
                    collapsed = false
                    if(songs.size in 1..4){
                        binding.appBarLayout.setExpanded(true)
                    }
                    Log.i("ivstate", songs.toString())


                }
            }
        }



//        val linearLayoutManager = object : LinearLayoutManager(requireActivity().application){
//            override fun canScrollVertically(): Boolea  {
//                return false
//            }
//        }




        binding.rvList.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollPosition = (binding.rvList.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()!!
                val v = (binding.rvList.layoutManager as? LinearLayoutManager)?.getChildAt(0)
                topViewRV = if(v==null) 0 else v.top - (binding.rvList.layoutManager as? LinearLayoutManager)?.paddingTop!!
            }
        })

        binding.nested.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            //Log.i("ListFragment","$scrollX $scrollY $oldScrollX $oldScrollY")
            nestedScrollY = scrollY
            binding.appBarLayout.setExpanded(false)
        }
        if(nestedScrollY!=0){
            binding.nested.scrollY = nestedScrollY
            binding.appBarLayout.setExpanded(false)
        }
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            collapsed = abs(verticalOffset) == appBarLayout?.totalScrollRange
        }
        if(collapsed == true){
            binding.appBarLayout.setExpanded(false)
        }








        NotificationReceiver.STATE.observe(viewLifecycleOwner){
            if(it.equals("PREVIOUS") || it.equals("NEXT")){
                (binding.rvList.adapter as RecyclerView.Adapter?)?.notifyDataSetChanged()
                //scrollToPosition(scrollPosition, topViewRV)
            }
            if(it== NotificationReceiver.ACTION_FAV){
                (binding.rvList.adapter as RecyclerView.Adapter?)?.notifyDataSetChanged()
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


        binding.etSearch.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
//                if(songs.isEmpty()){
//                    //Toast.makeText(requireActivity().application, "Cannot perform search operation", Toast.LENGTH_SHORT).show()
//                    //binding.etSearch.focusable = NOT_FOCUSABLE
//                    return@OnFocusChangeListener
//                }
                if(songs.isEmpty()){
                    if (binding.etSearch.text.isEmpty()) {
                        binding.noItemLayout.visibility = View.VISIBLE
                        binding.noResultLayout.visibility = View.INVISIBLE
                    } else {
                        binding.noResultLayout.visibility = View.VISIBLE
                        binding.noItemLayout.visibility = View.INVISIBLE
                    }
                }

                //binding.tvDuration.visibility = View.INVISIBLE
                //binding.card.visibility = View.INVISIBLE

                binding.appBarLayout.setExpanded(false)
//                val set = ConstraintSet()
//                val layout = binding.constraintLayoutList
//                set.clone(layout)
//                set.connect(binding.tvHeading1.id, ConstraintSet.TOP,binding.searchCard.id, ConstraintSet.BOTTOM)
//                set.applyTo(layout)
                val scale = resources.displayMetrics.density
                val sizeDp = 0
                val padding = sizeDp*scale+0.5f
                binding.rvList.setPadding(0,0,0,padding.toInt())
            }
                else{
                binding.noItemLayout.visibility = View.VISIBLE
                binding.tvDuration.visibility = View.VISIBLE
                binding.ivPlayAll.visibility = View.VISIBLE
                    setConstraints()
            }

            }

        val fullList = songs
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
                binding.appBarLayout.setExpanded(false)
                //binding.card.visibility = View.INVISIBLE

//                val set = ConstraintSet()
//                val layout = binding.constraintLayoutList
//                set.clone(layout)
//                set.connect(binding.tvHeading1.id, ConstraintSet.TOP,binding.searchCard.id, ConstraintSet.BOTTOM)
//                set.applyTo(layout)
                val scale = resources.displayMetrics.density
                var sizeDp = 0
                var padding = sizeDp*scale+0.5f
                binding.rvList.setPadding(0,0,0,padding.toInt())
                if(binding.etSearch.text.isEmpty()){
                    binding.tvDuration.visibility = View.VISIBLE
                    binding.ivPlayAll.visibility = View.VISIBLE
                    setConstraints()
                    if(songs.isNotEmpty())
                        binding.appBarLayout.setExpanded(true)
                    else
                        binding.appBarLayout.setExpanded(false)
//                    set.clone(layout)
//                    set.connect(binding.tvHeading1.id, ConstraintSet.TOP,binding.card.id, ConstraintSet.BOTTOM)
//                    set.applyTo(layout)
                    binding.card.visibility = View.VISIBLE
                    sizeDp = 85
                    padding = sizeDp*scale+0.5f
                    binding.rvList.setPadding(0,0,0,padding.toInt())
                    if(MainActivity.isMiniPlayerActive.value == true){
                        sizeDp = 165
                        padding = sizeDp*scale+0.5f
                        binding.rvList.setPadding(0,0,0,padding.toInt())
                    }
                }
                else{
                    binding.tvDuration.visibility = View.INVISIBLE
                    binding.ivPlayAll.visibility = View.INVISIBLE
                    setConstraints()
                }

                val searchText = binding.etSearch.text.toString()

                Log.i("search text",searchText)
                //var searchSongs = emptyList<Song>()
                if(type == "genre")
                     songs =songViewModel.getGenreSearchSongs(genre!!,searchText).toList()
                else if(type == "artist")
                     songs =songViewModel.getArtistSearchSongs(genre!!,searchText).toList()
                else if(type == "playlist")
                    songs = songViewModel.getPlaylistSearchSongs(user?.email!!, searchText).toList()
                if(searchText.isEmpty()){
                    songs = fullList
                    Log.i("isEmpty", songs.toString())
                }
                if(songs.isNotEmpty()) {
                    Log.i("isNotEmpty", songs.toString())
                    binding.noResultLayout.visibility = View.INVISIBLE
                    Log.i("searched song", songs.toMutableList().toString())
                    val adapter1 = MyPlaylistItemRecyclerViewAdapter(songs.toMutableList(),type.toString(),songViewModel,playlist, user)
                    GlobalScope.launch(Dispatchers.Main){
                        binding.rvList.adapter = adapter1
                        binding.rvList.layoutManager = linearLayoutManager
                        adapter1.notifyDataSetChanged()
                    }

                }
                else{
                    binding.noResultLayout.visibility = View.VISIBLE
                    binding.tvNoResult.text = "No results found for \"$searchText\""
                    val adapter1 = MyPlaylistItemRecyclerViewAdapter(mutableListOf(),type.toString(),songViewModel,playlist, user)
                    binding.rvList.adapter = adapter1
                    binding.rvList.layoutManager = linearLayoutManager
                    adapter1.notifyDataSetChanged()
                }
                if(binding.noItemLayout.visibility == View.VISIBLE){
                    binding.noResultLayout.visibility = View.INVISIBLE
                }

            }

        })

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

        binding.sort.setOnClickListener { it ->
            val popup = PopupMenu(it.context, it)
            if(songs.isEmpty()){
                //Toast.makeText(requireActivity().application, "Cannot perform sort operation", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.item1->{
                        songs = sortSongs(songs, "title")
                        when (titleSTATE) {
                            DESCENDING -> {
                                titleSTATE = ASCENDING
                            }
                            ASCENDING -> {
                                //count1++
                                //if (count1 % 2 == 0) {
                                songs = songs.reversed()
                                titleSTATE = DESCENDING
                                //}
                            }
                            UNSORTED -> {
                                titleSTATE = ASCENDING
                            }
                        }
                        if(songs.isEmpty()){
                            titleSTATE = UNSORTED
                        }
                        albumSTATE = UNSORTED
                        artistSTATE = UNSORTED
                        adapter =MyPlaylistItemRecyclerViewAdapter(songs.toMutableList() as ArrayList<Song>,type.toString(),songViewModel,playlist, user)
                        binding.rvList.adapter = adapter
                        binding.rvList.layoutManager = linearLayoutManager
                        adapter.notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.item2->{
                        songs = sortSongs(songs, "album")
                        when (albumSTATE) {
                            DESCENDING -> {
                                albumSTATE = ASCENDING
                            }
                            ASCENDING -> {
                                //count1++
                                //if (count1 % 2 == 0) {
                                songs = songs.reversed()
                                albumSTATE = DESCENDING
                                //}
                            }
                            UNSORTED -> {
                                albumSTATE = ASCENDING
                            }
                        }
                        if(songs.isEmpty()){
                            albumSTATE = UNSORTED
                        }
                        titleSTATE = UNSORTED
                        artistSTATE = UNSORTED
                        adapter =MyPlaylistItemRecyclerViewAdapter(songs.toMutableList() as ArrayList<Song>,type.toString(),songViewModel,playlist, user)
                        binding.rvList.adapter = adapter
                        binding.rvList.layoutManager = linearLayoutManager
                        adapter.notifyDataSetChanged()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.item3->{
                        songs = sortSongs(songs, "artist")
                        when (artistSTATE) {
                            DESCENDING -> {
                                artistSTATE = ASCENDING
                            }
                            ASCENDING -> {
                                //count1++
                                //if (count1 % 2 == 0) {
                                songs = songs.reversed()
                                artistSTATE = DESCENDING
                                //}
                            }
                            UNSORTED -> {
                                artistSTATE = ASCENDING
                            }
                        }
                        if(songs.isEmpty()){
                            artistSTATE = UNSORTED
                        }
                        titleSTATE = UNSORTED
                        albumSTATE = UNSORTED

                        adapter =MyPlaylistItemRecyclerViewAdapter(songs.toMutableList() as ArrayList<Song>,type.toString(),songViewModel,playlist, user)
                        binding.rvList.adapter = adapter
                        binding.rvList.layoutManager = linearLayoutManager
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
                //val helper = MenuPopupHelper(requireActivity().application, popup.menu as MenuBuilder)
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


//        binding.order.setOnClickListener { it->
//            val popup2 = PopupMenu(requireActivity().application, it)
//            popup2.setOnMenuItemClickListener {
//                when(it.itemId){
//                    R.id.item1->{
//                        if(!isOrder){
//                            songs = songs.reversed()
//                            isOrder = true
//                        }
//                        adapter =MyPlaylistItemRecyclerViewAdapter(songs.toList() as ArrayList<Song>,type.toString(),songViewModel,playlist, user)
//                        binding.rvList.adapter = adapter
//                        binding.rvList.layoutManager = linearLayoutManager
//                        adapter.notifyDataSetChanged()
//                        return@setOnMenuItemClickListener true
//                    }
//                    R.id.item2->{
//                        if(isOrder){
//                            songs = songs.reversed()
//                            isOrder = false
//                        }
//
//                        adapter =MyPlaylistItemRecyclerViewAdapter(songs.toList() as ArrayList<Song>,type.toString(),songViewModel,playlist, user)
//                        binding.rvList.adapter = adapter
//                        binding.rvList.layoutManager = linearLayoutManager
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

        
        
        NotificationReceiver.STATE.observe(viewLifecycleOwner){
            if(NotificationReceiver.STATE.value == ACTION_NEXT || NotificationReceiver.STATE.value == ACTION_PREV){
                Log.i("action next or previous", "a")
            }
        }

        if(type=="playlist"){
            binding.listFrameLayout.setBackgroundResource(R.color.black)
            val itemTouchHelper = ItemTouchHelper(simpleCallback)
            itemTouchHelper.attachToRecyclerView(binding.rvList)
            swipeChange.observe(viewLifecycleOwner){
                if(it==true){
                    playlist?.playlist = songs as ArrayList<Song>
                    songViewModel.updatePlaylist(playlist!!)
                    //Log.i("List", songs.toList().toString())
                }
            }

        }

        binding.searchButton.setOnClickListener {
            val frag = SearchFragment()
            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer,frag)?.addToBackStack(null)?.commit()
        }

        binding.ivPlayAll.setOnClickListener {
            binding.rvList.findViewHolderForAdapterPosition(0)?.itemView?.performClick()
        }
        

        
        
        Log.i("list songs", songs.toString())

        // Inflate the layout for this fragment
        return binding.root
    }


        private fun getDuration(songs: List<Song>): String{

        var duration = 0
        for(song in songs){
            val metaRetriever = MediaMetadataRetriever()
            val uriPath = Uri.parse("android.resource://com.example.mymusicapplication/${song.song}")
            metaRetriever.setDataSource(requireActivity().application , uriPath)
            duration += metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toInt() ?: 0
            metaRetriever.release()
        }
        val hours = duration/(1000 * 60 * 60)
        val totalMinutes = duration/(1000 * 60)
        val residualMinutes = totalMinutes % (60 * 24)
        return "${hours}h ${residualMinutes}m"
    }

    override fun onResume() {
        Log.i("ListFrag", "OnResume")
        super.onResume()

    }

    private fun scrollToPosition(position: Int, offset: Int = 0){
        binding.rvList.stopScroll()
        (binding.rvList.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, offset)
    }

    private fun sortSongs(songs: List<Song>, field: String): List<Song>{
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

    private val simpleCallback = object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, 0){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPos = viewHolder.adapterPosition
            val toPos = target.adapterPosition
            Collections.swap(songs,fromPos,toPos)
            recyclerView.adapter?.notifyItemMoved(fromPos,toPos)
            MusicService.songList.value = songs.toMutableList()
            swipeChange.value = true
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

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

    private fun setDuration(songs: List<Song>){
        GlobalScope.launch(Dispatchers.Main) {
            totalDuration = getDuration(songs)
            Log.i("TotalDuration", totalDuration)
            binding.tvDuration.text = totalDuration
        }
    }

    private fun  setConstraints(){
        if(binding.tvDuration.visibility == View.INVISIBLE){
            val layout = binding.parentLayout
            val constraintSet = ConstraintSet()
            constraintSet.clone(layout)
            constraintSet.connect(R.id.viewList, ConstraintSet.TOP, R.id.tvHeadingList,ConstraintSet.BOTTOM )
            constraintSet.applyTo(layout)
        }
        else{
            val layout = binding.parentLayout
            val constraintSet = ConstraintSet()
            constraintSet.clone(layout)
            constraintSet.connect(R.id.viewList, ConstraintSet.TOP, R.id.tvDuration,ConstraintSet.BOTTOM )
            constraintSet.applyTo(layout)
        }
    }



}