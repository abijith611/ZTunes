package com.example.mymusicapplication.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.example.mymusicapplication.databinding.AlbumListItemBinding


class MyPlaylistViewHolder(binding: AlbumListItemBinding): RecyclerView.ViewHolder(binding.root) {
    val imageView = binding.imageView
    val title = binding.tvTitle
    val options = binding.ivOptions
}