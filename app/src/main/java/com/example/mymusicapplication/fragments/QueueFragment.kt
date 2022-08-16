package com.example.mymusicapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.service.MusicService.Companion.currentSongInstance
import com.example.mymusicapplication.util.Queue
import com.example.mymusicapplication.R
import com.example.mymusicapplication.adapter.MyRecyclerViewAdapter
import com.example.mymusicapplication.adapter.NextPlayingAdapter
import com.example.mymusicapplication.adapter.QueueRecyclerViewAdapter
import com.example.mymusicapplication.databinding.FragmentQueueDialogBinding
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.service.MusicService.Companion.randomList
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.transition.MaterialSharedAxis
import java.util.*
import kotlin.collections.ArrayList


class QueueFragment : DialogFragment() {
//    companion object{
//        var queueSongState = MutableLiveData(false)
//    }
companion object{
    val upNextItemClick = MutableLiveData(false)
     val queueItemClick = MutableLiveData(false)
}
    lateinit var binding: FragmentQueueDialogBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("QueueOnCreate", "onCreate")
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y,true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y,false)
        binding = FragmentQueueDialogBinding.inflate(layoutInflater)
        val user = activity?.intent?.getParcelableExtra<User>("user")
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        val adapter = QueueRecyclerViewAdapter(Queue.queue, songViewModel, user, requireActivity().application, activity as MainActivity)
        //binding.tvNoSongs.visibility = View.INVISIBLE
        if(currentSongInstance==null){
            binding.nowPlayingLayout.visibility = View.INVISIBLE
            setQueueConstraints()
        }
        else{
            binding.nowPlayingLayout.visibility = View.VISIBLE
            setSongDetails()
        }
        MusicService.songState.observe(viewLifecycleOwner){
            if(it=="NEXT"|| it=="PREV"){
                setSongDetails()
                setUpNextConstraints()
            }
        }

        MusicService.songChanged.observe(viewLifecycleOwner){
            setSongDetails()
            setUpNextConstraints()
        }

        setUpNextConstraints()
        if(MusicService.songList.value==null){
            binding.upNextLayout.visibility = View.INVISIBLE
        }
        else{
            binding.upNextLayout.visibility = View.VISIBLE
        }

       upNextItemClick.observe(viewLifecycleOwner){
            if(it==true){
                setSongDetails()
            }
        }

        queueItemClick.observe(viewLifecycleOwner){
            if(it==true){
                setSongDetails()
            }
        }




        binding.rvQueue.adapter = adapter
        binding.rvQueue.layoutManager = LinearLayoutManager(requireActivity().application)
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvQueue)
        MusicService.firstSongPlayed.observe(viewLifecycleOwner){
            if(it==true){
                Log.i("rv","removed 0")
                adapter.notifyItemRemoved(0)
            }
        }



        // Inflate the layout for this fragment
        return binding.root
    }
    private val simpleCallback = object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END, 0){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPos = viewHolder.adapterPosition
            val toPos = target.adapterPosition
            Collections.swap(Queue.queue,fromPos,toPos)
            recyclerView.adapter?.notifyItemMoved(fromPos,toPos)
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

    }
    private fun setSongDetails(){
        binding.tvTitle.text = MusicService.currentSongInstance?.title
        binding.tvArtist.text = MusicService.currentSongInstance?.artist
        MusicService.currentSongInstance?.let { binding.imageView.setImageResource(it.img) }
        setNextSongDetails()
    }


    private fun setNextSongDetails(){
        val nextSongs = if(randomList!=null)
            randomList
        else
            MusicService.songList.value

        if(nextSongs!!.isEmpty()){

        }

        Log.i("nextSongs",nextSongs.toString())

        var position = getPosition(nextSongs.toMutableList(), currentSongInstance!!)
        if(currentSongInstance in Queue.played && currentSongInstance!= Queue.played[0]){
            position = getPosition(nextSongs, Queue.played[0])
        }
        var subList  = mutableListOf<Song>()
        if(position<nextSongs.size-1){
            subList =nextSongs.toMutableList().subList(position+1, nextSongs.size)
            Log.i("Pos","$position ${nextSongs.size-1}")
        }
        else
            subList = nextSongs.toMutableList().subList(0, nextSongs.size-1)
//        if(nextSongs.size - subList.size > 1){
//             subList.addAll(nextSongs.subList(0,nextSongs.size - subList.size))
//        }
        Log.i("Queue", "$nextSongs $subList $position ${nextSongs.size-1}")
        val emptyArrayList = ArrayList<Song>()
        for(song in subList){
            emptyArrayList.add(song)
        }
        val adapter1 = NextPlayingAdapter(emptyArrayList,requireActivity().application, activity as MainActivity)
        binding.rvSearch.adapter = adapter1
        binding.rvSearch.layoutManager = LinearLayoutManager(requireActivity().application)
    }

    private fun setUpNextConstraints(){
        if(Queue.queue.isEmpty()){
            binding.queueLayout.visibility = View.INVISIBLE
            val parent = binding.parentLayout
            val constraintSet = ConstraintSet()
            constraintSet.clone(parent)
            constraintSet.connect(R.id.upNextLayout, ConstraintSet.TOP, R.id.nowPlayingLayout, ConstraintSet.BOTTOM)
            constraintSet.applyTo(parent)
            //binding.tvNoSongs.visibility = View.VISIBLE
        }
        else{
            binding.queueLayout.visibility = View.VISIBLE
            //binding.tvNoSongs.visibility = View.VISIBLE
        }
    }

    private fun setQueueConstraints(){
            val parent = binding.parentLayout
            val constraintSet = ConstraintSet()
            constraintSet.clone(parent)
            constraintSet.connect(R.id.queueLayout, ConstraintSet.TOP, R.id.parentLayout, ConstraintSet.TOP)
            constraintSet.applyTo(parent)
    }

    private fun getPosition(songList: List<Song>, song: Song): Int{
        for( i in songList.indices){
            if(songList[i].id == song.id){
                return i
            }
        }
        return -1
    }


}