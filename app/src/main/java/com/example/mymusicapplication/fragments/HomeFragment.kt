package com.example.mymusicapplication.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.service.MusicService
import com.example.mymusicapplication.R
import com.example.mymusicapplication.adapter.ImageAdapter
import com.example.mymusicapplication.databinding.FragmentHomeBinding
import com.example.mymusicapplication.db.Song
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.transition.MaterialFadeThrough

class HomeFragment : Fragment() {

    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var adapter: ImageAdapter
    private lateinit var imageList: ArrayList<Song>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        enterTransition = MaterialFadeThrough()
        val activity = activity as MainActivity
        val binding = FragmentHomeBinding.inflate(layoutInflater)
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        val songsEDM =songViewModel.getGenreSongs("EDM")
        val songsRomantic =songViewModel.getGenreSongs("Romantic")
        val songsParty =songViewModel.getGenreSongs("Party")
        val songsPopular =songViewModel.getGenreSongs("Popular")
        viewPager2 = binding.viewPager2
        handler = Handler(Looper.myLooper()!!)
        imageList = ArrayList()
        imageList.add(songsEDM[0])
        imageList.add(songsRomantic[0])
        imageList.add(songsParty[0])
        imageList.add(songsPopular[0])
        adapter = ImageAdapter(imageList, viewPager2)
        viewPager2.adapter = adapter
        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        setupTransformer()
        viewPager2.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 5000)
            }
        })




        binding.card1.setOnClickListener{
            openFragment("EDM")
        }

        binding.card2.setOnClickListener{
            openFragment("Romantic")
        }

        binding.card3.setOnClickListener{
            openFragment("Party")
        }
        binding.card4.setOnClickListener{
            openFragment("Popular")
        }

        binding.card11.setOnClickListener{
            openFragmentForArtist("Ed Sheeran")
        }

        binding.card21.setOnClickListener{
            openFragmentForArtist("Taylor Swift")
        }

        binding.card31.setOnClickListener{
            openFragmentForArtist("The Weeknd")
        }
        binding.card41.setOnClickListener{
            openFragmentForArtist("Katy Perry")
        }

        MainActivity.isMiniPlayerActive.observe(viewLifecycleOwner){
            val scale = resources.displayMetrics.density
            val sizeDp = 65
            val padding = sizeDp*scale+0.5f
            if(MainActivity.isMiniPlayerActive.value == true){
                binding.rootLayout.setPadding(0,0,0,padding.toInt())
            }
        }

        binding.ivRecent.setOnClickListener {
            val frag = LimitedListFragment()
            val bundle = Bundle()
            bundle.putString("title","recent")
            frag.arguments = bundle
            activity.supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, frag).addToBackStack(null).commit()
        }

        binding.ivFrequent.setOnClickListener {
            val frag = LimitedListFragment()
            val bundle = Bundle()
            bundle.putString("title","frequent")
            frag.arguments = bundle
            activity.supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, frag).addToBackStack(null).commit()
        }
        if(MusicService.currentSongInstance==null){
            binding.ivQueue.visibility = View.INVISIBLE
        }
        else{
            binding.ivQueue.visibility = View.VISIBLE
        }

        binding.ivQueue.setOnClickListener {
            val frag = QueueFragment()
            activity.supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,frag).addToBackStack(null).commit()
        }
        return binding.root
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

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 4000)
    }

    private val runnable = Runnable{
        viewPager2.currentItem = viewPager2.currentItem + 1
    }

    private fun setupTransformer(){
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer{page, position ->
            val r = 1 - kotlin.math.abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }
        viewPager2.setPageTransformer(transformer)

    }


}