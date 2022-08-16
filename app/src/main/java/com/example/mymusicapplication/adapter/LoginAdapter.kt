package com.example.mymusicapplication.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mymusicapplication.fragments.LoginFragment
import com.example.mymusicapplication.fragments.SignupFragment

class LoginAdapter(fragAct: FragmentActivity, private var totalTabs: Int): FragmentStateAdapter(fragAct) {
    override fun getItemCount(): Int {
        return totalTabs
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> LoginFragment()
            1-> SignupFragment()
            else-> LoginFragment()
        }
    }

}