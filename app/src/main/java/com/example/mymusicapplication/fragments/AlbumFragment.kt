package com.example.mymusicapplication.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.R
import com.example.mymusicapplication.adapter.MyPlaylistRecyclerViewAdapter
import com.example.mymusicapplication.databinding.FragmentAlbumBinding
import com.example.mymusicapplication.db.*
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.coroutines.runBlocking

class AlbumFragment : Fragment() {

    companion object{
        var songAdded = MutableLiveData<Boolean>()
        var adapterUpdate = MutableLiveData(false)
    }

    lateinit var binding: FragmentAlbumBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        enterTransition = MaterialFadeThrough()
        //exitTransition = MaterialFadeThrough()



        songAdded.value = false
        binding = FragmentAlbumBinding.inflate(layoutInflater)
        binding.sort.setImageResource(R.drawable.ic_baseline_sort_24)
        binding.noResultLayout.visibility = View.INVISIBLE

        var scrollPosition=0
        var topViewRV = 0
        val user = activity?.intent?.getParcelableExtra<User>("user")
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]


        var playlists= user?.let { songViewModel.getPlaylistForUser(it.email) }
        Log.i("AlbumFragment", playlists?.toList().toString())
        if(playlists!!.isEmpty()){
            Log.i("AlbumFragment","empty")
            binding.noResultLayout.visibility = View.VISIBLE
        }


        Log.i("album", songViewModel.getPlaylistForUser("admin").toList().toString())
        val type = arguments?.getString("type")
        val song = arguments?.getParcelable<Song>("song")

        var adapter = MyPlaylistRecyclerViewAdapter(playlists.toList(), type.toString(), songViewModel, song, user!!)
        binding.rvPlaylist.adapter = adapter
        binding.rvPlaylist.layoutManager = LinearLayoutManager(requireActivity().application)

        adapterUpdate.observe(viewLifecycleOwner){
            //playlists = songViewModel.getPlaylistForUser(user.email)
            Log.i("adapterUpdate","update")
            if(adapter.itemCount==0){
                Log.i("adapterUpdate", "empty")
                binding.noResultLayout.visibility = View.VISIBLE
            }
            else{
                Log.i("adapterUpdate", "not empty")
                binding.noResultLayout.visibility = View.INVISIBLE
            }
        }

        binding.add.setOnClickListener{
            if(songViewModel.getPlaylistForUser(user.email).size >= 10){
                view?.let { it1 -> Snackbar.make(it1,"Playlist limit exceeded",Snackbar.LENGTH_LONG).show() }
                return@setOnClickListener
            }
            val dialog = Dialog(requireContext(), R.style.DialogStyle)
            dialog.setContentView(R.layout.custom_dialog)
            dialog.show()
            val playlistName = dialog.findViewById<EditText>(R.id.etName)
            val okButton = dialog.findViewById<TextView>(R.id.okButton)
            val cancelButton = dialog.findViewById<TextView>(R.id.cancelButton)
            playlistName.onFocusChangeListener =
                View.OnFocusChangeListener { v, hasFocus ->
                    if(hasFocus){
                        Log.i("EditText","hasFocus")
                        playlistName.hint = ""
                    }
                }
            cancelButton.setOnClickListener{
                dialog.cancel()
            }
            okButton.setOnClickListener{
                if(playlistName.text.isNotEmpty()) {
                    if(playlistName.text.length < 30) {
                        if (!isExisting(
                                playlistName.text.toString(),
                                songViewModel.getPlaylistForUser(user.email)
                            )
                        ) {
                            runBlocking {
                                repository.insertPlaylist(
                                    Playlist(
                                        0,
                                        playlistName.text.toString(),
                                        user.email,
                                        arrayListOf()
                                    )
                                )
                            }
                            playlists = songViewModel.getPlaylistForUser(user.email)
                            Log.i("playlists", playlists!!.toList().toString())
                            adapter = MyPlaylistRecyclerViewAdapter(playlists!!.toList(),type.toString(), songViewModel,
                                song, user)
                            binding.rvPlaylist.adapter = adapter
                            binding.rvPlaylist.layoutManager =
                                LinearLayoutManager(requireActivity().application)
                            adapter.notifyDataSetChanged()
                            dialog.cancel()
                            binding.noResultLayout.visibility = View.INVISIBLE
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
                scrollToPosition(scrollPosition, topViewRV)

            }
        }
//
//        if(songViewModel.allPlaylists.isEmpty())
//        runBlocking {
//            repository.insertPlaylist(Playlist(0,"playlist1",email, arrayListOf(Song(
//                0,
//                "Bon Appetit",
//                "Witness",
//                "Katy Perry",
//                R.raw.bon_appetit,
//                R.drawable.witness_album,
//                "Party"
//            ))))
//        }

        //Log.i("playlists", playlists.toString())


        binding.rvPlaylist.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollPosition = (binding.rvPlaylist.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()!!
                val v = (binding.rvPlaylist.layoutManager as? LinearLayoutManager)?.getChildAt(0)
                topViewRV = if(v==null) 0 else v.top - (binding.rvPlaylist.layoutManager as? LinearLayoutManager)?.paddingTop!!
            }
        })

        binding.etSearch.addTextChangedListener (object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val searchText = binding.etSearch.text.toString()
                Log.i("search text",searchText)
                playlists= songViewModel.getSearchPlaylists(searchText, user.email)
                Log.i("searched song", playlists!!.toList().toString())
                if(playlists!!.isNotEmpty()) {
                    binding.noResultLayout.visibility = View.INVISIBLE
                    Log.i("searched song", playlists!!.toList().toString())
                    adapter = MyPlaylistRecyclerViewAdapter(playlists!!.toList(),type.toString(), songViewModel, song, user)
                    binding.rvPlaylist.adapter = adapter
                    binding.rvPlaylist.layoutManager = LinearLayoutManager(requireActivity().application)
                    adapter.notifyDataSetChanged()
                }
                else{
                    binding.noResultLayout.visibility = View.VISIBLE
                    adapter = MyPlaylistRecyclerViewAdapter(emptyList(),type.toString(), songViewModel, song, user)
                    binding.rvPlaylist.adapter = adapter
                    binding.rvPlaylist.layoutManager = LinearLayoutManager(requireActivity().application)
                    adapter.notifyDataSetChanged()
                }
            }

        })

//        binding.order.setOnClickListener { it->
//            val popup2 = PopupMenu(requireActivity().application, it)
//            popup2.setOnMenuItemClickListener {
//                when(it.itemId){
//                    R.id.item1->{
//                        if(!isOrder){
//                            playlists = playlists.reversedArray()
//                            isOrder = true
//                        }
//                        adapter =MyPlaylistRecyclerViewAdapter(playlists.toList() as ArrayList<Playlist>,type.toString(), songViewModel, song)
//                        binding.rvPlaylist.adapter = adapter
//                        binding.rvPlaylist.layoutManager = LinearLayoutManager(requireActivity().application)
//                        adapter.notifyDataSetChanged()
//                        return@setOnMenuItemClickListener true
//                    }
//                    R.id.item2->{
//                        if(isOrder){
//                            playlists = playlists.reversedArray()
//                            isOrder = false
//                        }
//
//                        adapter =MyPlaylistRecyclerViewAdapter(playlists.toList() as ArrayList<Playlist>,type.toString(), songViewModel, song)
//                        binding.rvPlaylist.adapter = adapter
//                        binding.rvPlaylist.layoutManager = LinearLayoutManager(requireActivity().application)
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
        var count = 0
        binding.sort.setOnClickListener {
            playlists = sortPlaylists(playlists!!).toTypedArray()
            count++
            binding.sort.setImageResource(R.drawable.ic_sort)
            if(count%2==0){
                playlists = playlists!!.reversedArray()
                binding.sort.setImageResource(R.drawable.ic_sortza)

            }
            adapter = MyPlaylistRecyclerViewAdapter(playlists!!.toList(),type.toString(), songViewModel, song, user)
            binding.rvPlaylist.adapter = adapter
            binding.rvPlaylist.layoutManager = LinearLayoutManager(requireActivity().application)
            adapter.notifyDataSetChanged()
        }

        songAdded.observe(viewLifecycleOwner){
            if(it == true){
                view?.let { it1 -> Snackbar.make(it1,"Song Added", Snackbar.LENGTH_LONG).show() }
            }
        }

        MainActivity.isMiniPlayerActive.observe(viewLifecycleOwner){
            Log.i("Mini player status", it.toString())
            val scale = resources.displayMetrics.density
            val sizeDp = 125
            val padding = sizeDp*scale+0.5f
            if(MainActivity.isMiniPlayerActive.value == true){
                Log.i("setPadding", "setPadding")
                binding.rvPlaylist.setPadding(0,0,0,padding.toInt())
            }
        }


        return binding.root
    }


    private fun scrollToPosition(position: Int, offset: Int = 0){
        binding.rvPlaylist.stopScroll()
        (binding.rvPlaylist.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, offset)

    }

    private fun isExisting(name: String, playlists: Array<Playlist>): Boolean{
            for (playlist in playlists) {
                if (playlist.name == name) {
                    return true
                }
            }
        return false
    }

    private fun sortPlaylists(playlists: Array<Playlist>):List<Playlist>{
        return playlists.sortedWith(compareBy { it.name })
    }

//    fun String.capitalized(): String{
//        var s = ""
//        if(this[0].isLowerCase()){
//             s+=this[0].uppercase()
//            for(i in 1 until this.length){
//                s+=this[i]
//            }
//        }
//        return s
//    }

}