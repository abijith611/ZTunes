package com.example.mymusicapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.databinding.FragmentLoginBinding
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.snackbar.Snackbar


class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(layoutInflater)
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repository = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repository))[SongViewModel::class.java]
        binding.btnLogin.setOnClickListener {
            val email =binding.etEmail.text.toString()
            val pwd = binding.etPwd.text.toString()
            when(songViewModel.isAuthenticated(email,pwd)){
                -1-> {
                    Snackbar.make(binding.root, "If new user, Sign up first!", Snackbar.LENGTH_SHORT)
                        .show()
                }
                 0-> {
                     Snackbar.make(binding.root, "Incorrect password!", Snackbar.LENGTH_SHORT).show()
                 }
                1->{
                    MainActivity.isMiniPlayerActive.value = false
                    val user = songViewModel.getUser(email) as User
                    songViewModel.userLogIn(user)
                    Intent(requireActivity().application, MainActivity::class.java).apply {
                        songViewModel.userLogIn(user)
                        this.putExtra("user",user)
                        startActivity(this)
                        activity?.finish()
                    }
                }
            }
        }
        return binding.root
    }

}