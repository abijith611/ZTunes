package com.example.mymusicapplication.fragments
import com.example.mymusicapplication.fragments.AlbumFragment
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.R
import com.example.mymusicapplication.adapter.MyPlaylistRecyclerViewAdapter
import com.example.mymusicapplication.databinding.FragmentAlbumDialogBinding
import com.example.mymusicapplication.db.*
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking

class AlbumDialogFragment : DialogFragment() {

    companion object{
        var songAdded = MutableLiveData<Boolean>()
    }

    lateinit var binding: FragmentAlbumDialogBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        songAdded.value = false
        binding = FragmentAlbumDialogBinding.inflate(layoutInflater)

        var scrollPosition=0
        var topViewRV = 0

        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]

        val user =activity?.intent?.getParcelableExtra<User>("user")
        var playlists=songViewModel.getPlaylistForUser(user!!.email!!)


        val type = arguments?.getString("type")
        val song = arguments?.getParcelable<Song>("song")

        var adapter = MyPlaylistRecyclerViewAdapter(playlists.toMutableList(), type.toString(), songViewModel, song, user)
        binding.rvPlaylistDialog.adapter = adapter
        binding.rvPlaylistDialog.layoutManager = LinearLayoutManager(requireActivity().application)

        binding.add.setOnClickListener{
            if(songViewModel.getPlaylistForUser(user.email!!).size > 10){
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
            cancelButton.setOnClickListener{
                dialog.cancel()
            }
            okButton.setOnClickListener{
                if(playlistName.text.isNotEmpty()) {
                    if(playlistName.text.length < 30) {
                        if (!isExisting(
                                playlistName.text.toString(),
                                songViewModel.getPlaylistForUser(user.email!!)
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
                            adapter = MyPlaylistRecyclerViewAdapter(playlists.toMutableList(),type.toString(), songViewModel,
                                song, user)
                            binding.rvPlaylistDialog.adapter = adapter
                            binding.rvPlaylistDialog.layoutManager =
                                LinearLayoutManager(requireActivity().application)
                            adapter.notifyDataSetChanged()
                            dialog.cancel()
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

        binding.rvPlaylistDialog.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollPosition = (binding.rvPlaylistDialog.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()!!
                val v = (binding.rvPlaylistDialog.layoutManager as? LinearLayoutManager)?.getChildAt(0)
                topViewRV = if(v==null) 0 else v.top - (binding.rvPlaylistDialog.layoutManager as? LinearLayoutManager)?.paddingTop!!
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
                if(playlists.isNotEmpty()) {
                    val adapter1 = MyPlaylistRecyclerViewAdapter(playlists.toMutableList(),type.toString(), songViewModel, song, user)
                    binding.rvPlaylistDialog.adapter = adapter1
                    binding.rvPlaylistDialog.layoutManager = LinearLayoutManager(requireActivity().application)
                    adapter1.notifyDataSetChanged()
                }
                else{
                    val adapter1 = MyPlaylistRecyclerViewAdapter(emptyList(),type.toString(), songViewModel, song, user)
                    binding.rvPlaylistDialog.adapter = adapter1
                    binding.rvPlaylistDialog.layoutManager = LinearLayoutManager(requireActivity().application)
                    adapter1.notifyDataSetChanged()
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
//                        adapter =MyPlaylistRecyclerViewAdapter(playlists.toList(),type.toString(), songViewModel, song, user)
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
//                        adapter =MyPlaylistRecyclerViewAdapter(playlists.toList(),type.toString(), songViewModel, song, user)
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
            playlists = sortPlaylists(playlists).toTypedArray()
            count++
            binding.sort.setImageResource(R.drawable.ic_sort)
            if(count%2==0){
                playlists = playlists.reversedArray()
                binding.sort.setImageResource(R.drawable.ic_sortza)
            }
            adapter = MyPlaylistRecyclerViewAdapter(playlists.toMutableList() as ArrayList<Playlist>,type.toString(), songViewModel, song, user)
            binding.rvPlaylistDialog.adapter = adapter
            binding.rvPlaylistDialog.layoutManager = LinearLayoutManager(requireActivity().application)
            adapter.notifyDataSetChanged()
        }

        songAdded.observe(viewLifecycleOwner){
            if(it == true){
                view?.let { it1 -> Snackbar.make(it1,"Song Added", Snackbar.LENGTH_LONG).show() }
            }
        }
        return binding.root
    }


    private fun scrollToPosition(position: Int, offset: Int = 0){
        binding.rvPlaylistDialog.stopScroll()
        (binding.rvPlaylistDialog.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, offset)

    }

    private fun isExisting(name: String, playlists: Array<Playlist>): Boolean{
        for(playlist in playlists){
            if(playlist.name == name){
                return true
            }
        }
        return false
    }

    private fun sortPlaylists(playlists: Array<Playlist>):List<Playlist>{
        return playlists.sortedWith(compareBy { it.name })
    }

}