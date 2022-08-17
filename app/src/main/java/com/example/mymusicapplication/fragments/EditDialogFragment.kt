package com.example.mymusicapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mymusicapplication.databinding.FragmentEditDialogBinding

class EditDialogFragment : DialogFragment() {

    lateinit var binding: FragmentEditDialogBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditDialogBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

}