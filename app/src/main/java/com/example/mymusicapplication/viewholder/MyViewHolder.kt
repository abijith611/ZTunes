package com.example.mymusicapplication.viewholder

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.databinding.FragmentListBinding
import com.example.mymusicapplication.databinding.ListItemBinding

class MyViewHolder(binding: ListItemBinding): RecyclerView.ViewHolder(binding.root) {
    val imageView = binding.imageView
    val title = binding.tvTitle
    val artist = binding.tvArtist
    val options = binding.ivOptions
    val fav = binding.ivFav
    val layout = binding.listItemLayout

}