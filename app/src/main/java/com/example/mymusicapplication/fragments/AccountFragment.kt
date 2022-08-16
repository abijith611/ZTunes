package com.example.mymusicapplication.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import com.example.mymusicapplication.activity.LoginActivity
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.R
import com.example.mymusicapplication.util.Validator
import com.example.mymusicapplication.adapter.MyPlaylistRecyclerViewAdapter
import com.example.mymusicapplication.databinding.FragmentAccountBinding
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough

class AccountFragment : Fragment() {

    private val detailsUpdate = MutableLiveData(false)

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        enterTransition = MaterialFadeThrough()
        //exitTransition = MaterialFadeThrough()
        val binding = FragmentAccountBinding.inflate(layoutInflater)
        val user = activity?.intent?.getParcelableExtra<User>("user")
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        var user1= user?.let { songViewModel.getUser(it.email) }
        if (user1 != null) {
            binding.tvName.text = user1.name
//            binding.tvUsername.text = user1.email
//            binding.tvMobile.text = user1.mobileNumber
        }
//        detailsUpdate.observe(viewLifecycleOwner){
//            if(it==true){
//                Snackbar.make(requireView(), "User details updated!!",Snackbar.LENGTH_SHORT)
//                    .setAnchorView(MainActivity.snackBarAnchor).show()
//                user1=songViewModel.getUser(user.userId)
//                binding.tvName.text = user1?.name
//                binding.tvUsername.text = user1?.email
//                binding.tvMobile.text = user1?.mobileNumber
//            }
//        }

        binding.ivForward.setOnClickListener {
            val fragment = FavouriteFragment()
            activity?.supportFragmentManager?.beginTransaction()?.addSharedElement(binding.favList, "favClicked")
                ?.replace(R.id.fragmentContainer,fragment)?.addToBackStack(null)?.commit()
        }

        binding.btnSignOut.setOnClickListener {
            Intent(requireActivity().application, LoginActivity::class.java).apply {
                startActivity(this)
                activity?.finish()
            }
            if (user1 != null) {
                songViewModel.userLogOut(user1!!)
            }
        }

        val playlists =songViewModel.getPlaylistForUser(user?.email)
        binding.tvTitle3.text = "${playlists.size} playlists"
        binding.rv.adapter =
            user?.let {
                MyPlaylistRecyclerViewAdapter(playlists.toList(),"", songViewModel,null,
                    it
                )
            }
        binding.rv.layoutManager = LinearLayoutManager(requireActivity().application)



        binding.ivForward1.setOnClickListener foo@{
            val validator = Validator()
            val dialog = Dialog(it.context, R.style.DialogStyle)
            dialog.setContentView(R.layout.fragment_edit_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val etName = dialog.findViewById<EditText>(R.id.etName)
            val etMobile = dialog.findViewById<EditText>(R.id.etMobile)
            //val etEmail = dialog.findViewById<EditText>(R.id.etEmail)
            val cancelButton = dialog.findViewById<TextView>(R.id.cancelButton)
            val okButton = dialog.findViewById<TextView>(R.id.okButton)
            val existingName = user1?.name
            val existingMobile = user1?.mobileNumber
            etName?.setText(existingName)
            etMobile?.setText(existingMobile)


//            if(etEmail.text.isEmpty()){
//                etEmail.error = "The field is Empty!"
//                isError = true
//            }
//            if(etEmail.text.length > 30){
//                etEmail.error = "Too long!"
//                isError = true
//            }
//            if(!etEmail.text.matches( Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$"))){
//                etEmail.error = "Enter the correct email format!"
//                isError = true
//            }
            cancelButton?.setOnClickListener {
                dialog.cancel()
            }
            okButton?.setOnClickListener inner@{
                var isError = false
                Log.i("okButton", "buttonClick")
                if(!validator.isValidName(etName.text.toString())){
                    etName.error = "Invalid Name!"
                    if(etName.text.length > 30)
                        etName.error = "Name too long!!"
                    isError = true
                }
                if(!validator.isValidPhoneNumber(etMobile.text.toString())){
                    etMobile.error = "Invalid phone number!"
                    isError = true
                }
                if(!isError){
                    Log.i("okButton", "noError")
                    val name = etName.text.toString()
                    val mob = etMobile.text.toString()
                    //val email = etEmail.text.toString()
                    if (user != null) {
                        songViewModel.updateUser(user, name, mob)
                    }
                    dialog.cancel()
                    if (user1 != null) {
                        if(!(existingName==name && existingMobile ==mob))
                        detailsUpdate(songViewModel, user1!!, binding)
                        if (user != null) {
                            user1=songViewModel.getUser(user.email)
                        }
                    }

                }



            }
        }

        MainActivity.isMiniPlayerActive.observe(viewLifecycleOwner){
            Log.i("Mini player status", it.toString())
            val scale = resources.displayMetrics.density
            val sizeDp = 150
            val padding = sizeDp*scale+0.5f
            if(MainActivity.isMiniPlayerActive.value == true){
                binding.rv.setPadding(0,0,0,padding.toInt())
            }
        }

        return binding.root
    }

    private fun detailsUpdate(songViewModel: SongViewModel, user: User, binding: FragmentAccountBinding){
        Snackbar.make(requireView(), "User details updated!!",Snackbar.LENGTH_SHORT)
            .setAnchorView(MainActivity.snackBarAnchor).show()
        val user1=songViewModel.getUser(user.email)
        binding.tvName.text = user1?.name
//        binding.tvUsername.text = user1?.email
//        binding.tvMobile.text = user1?.mobileNumber
    }
    }

