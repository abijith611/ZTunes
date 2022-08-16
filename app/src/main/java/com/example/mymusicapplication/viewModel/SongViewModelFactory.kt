package com.example.mymusicapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymusicapplication.db.SongRepository

class SongViewModelFactory(private val repository: SongRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SongViewModel::class.java)){
            return SongViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }

}