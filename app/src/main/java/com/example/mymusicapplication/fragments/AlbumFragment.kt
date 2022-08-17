package com.example.mymusicapplication.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.mymusicapplication.R
import com.example.mymusicapplication.activity.MainActivity
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
        if(playlists!!.isEmpty()){
            binding.noResultLayout.visibility = View.VISIBLE
        }
        val type = arguments?.getString("type")
        val song = arguments?.getParcelable<Song>("song")

        var adapter = MyPlaylistRecyclerViewAdapter(playlists.toList(), type.toString(), songViewModel, song, user!!)
        binding.rvPlaylist.adapter = adapter
        binding.rvPlaylist.layoutManager = LinearLayoutManager(requireActivity().application)

        adapterUpdate.observe(viewLifecycleOwner){
            if(adapter.itemCount==0){
                binding.noResultLayout.visibility = View.VISIBLE
            }
            else{
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
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val playlistName = dialog.findViewById<EditText>(R.id.etName)
            val okButton = dialog.findViewById<TextView>(R.id.okButton)
            val cancelButton = dialog.findViewById<TextView>(R.id.cancelButton)
            playlistName.onFocusChangeListener =
                View.OnFocusChangeListener { _, hasFocus ->
                    if(hasFocus){
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
                playlists= songViewModel.getSearchPlaylists(searchText, user.email)
                if(playlists!!.isNotEmpty()) {
                    binding.noResultLayout.visibility = View.INVISIBLE
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
            val scale = resources.displayMetrics.density
            val sizeDp = 125
            val padding = sizeDp*scale+0.5f
            if(MainActivity.isMiniPlayerActive.value == true){
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


}