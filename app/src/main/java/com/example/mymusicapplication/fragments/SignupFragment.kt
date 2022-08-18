package com.example.mymusicapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mymusicapplication.util.Validator
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.databinding.FragmentSignupBinding
import com.example.mymusicapplication.db.SongDatabase
import com.example.mymusicapplication.db.SongRepository
import com.example.mymusicapplication.db.User
import com.example.mymusicapplication.viewModel.SongViewModel
import com.example.mymusicapplication.viewModel.SongViewModelFactory
import com.google.android.material.snackbar.Snackbar

class SignupFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val validator = Validator()
        val binding = FragmentSignupBinding.inflate(layoutInflater)
        val dao = SongDatabase.getInstance(requireActivity().application).songDao
        val repo = SongRepository(dao)
        val songViewModel = ViewModelProvider(this, SongViewModelFactory(repo))[SongViewModel::class.java]
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val pwd = binding.etPwd.text.toString()
            val cPwd = binding.etConfirmPwd.text.toString()
            val mob = binding.etMob.text.toString()
            var isError = false
            if(!validator.isValidPhoneNumber(mob)) {
                binding.etMob.error = "Invalid phone number!"
                isError = true
            }
            if(pwd!=cPwd) {
                binding.etConfirmPwd.error = "Password do not match!"
                isError = true
            }
            if(!validator.isValidEmail(email)){
                binding.etEmail.error = "Invalid email!"
                if(email.length>30){
                    binding.etEmail.error = "Email too long!!"
                }
                isError = true
            }
            if(songViewModel.getUser(email)!=null){
                Snackbar.make(binding.root, "User with the userId already exists!!", Snackbar.LENGTH_LONG)
                    .show()
                isError = true
            }
            if(!validator.isStrongPwd(pwd)){
                binding.etPwd.error = "The password must contain\n 8-16 character in length\n At least 1 upper case character\n At least 1 lower case letter\n At least 1 special character\n At least 1 number"
                isError = true
            }
            if(!validator.isValidName(name)){
                binding.etName.error = "Invalid Name!"
                if(name.length > 30)
                    binding.etName.error = "Name too long!!"
                isError = true
            }
            if(isError){
                return@setOnClickListener
            }

            val user = User(0,email,pwd,name,mob)
            songViewModel.insertUser(user)
            Intent(requireActivity().application, MainActivity::class.java).apply {
                this.putExtra("user",user)
                startActivity(this)
                activity?.finish()
            }

        }

        return binding.root
    }


}