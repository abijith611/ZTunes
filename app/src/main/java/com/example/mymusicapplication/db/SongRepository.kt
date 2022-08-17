package com.example.mymusicapplication.db

import android.util.Log
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

class SongRepository(private val dao: SongDao) {

    suspend fun insertSong(song: Song){
        return dao.insertSong(song)
    }


    suspend fun getAllSongs():Array<Song>{
        return dao.getAllSongs()
    }

    suspend fun getGenreBasedSongs(genre: String): Array<Song>{
        return dao.getSongsBasedOnGenre(genre)
    }

    suspend fun getArtistBasedSongs(artist: String): Array<Song>{
        return dao.getSongsBasedOnArtist(artist)
    }

    suspend fun searchSong(query: String): Array<Song>{
        Log.i("repo",dao.searchDb(query).toList().toString())
        return dao.searchDb(query)
    }

    suspend fun insertPlaylist(playlist: Playlist): Long{
        return dao.insertPlaylist(playlist)
    }

    suspend fun updatePlaylist(playlist: Playlist): Int{
        return dao.updatePlaylist(playlist)
    }

    suspend fun deletePlaylist(playlist: Playlist): Int{
        return dao.deletePlaylist(playlist)
    }

    suspend fun searchPlaylist(query: String, userId: String): Array<Playlist>{
        return dao.searchPlaylistDb(query, userId)
    }

    suspend fun getPlaylistsForUser(userId: String): Array<Playlist>{
        return dao.getPlaylistsForUser(userId)
    }

    suspend fun insertUser(user: User){
        return dao.insertUser(user)
    }

    suspend fun updateUser(user: User){
        return dao.updateUser(user)
    }

    suspend fun getUsers(): Array<User>{
        return dao.getUsers()
    }

    suspend fun insertFav(favourite: Favourite){
        return dao.insertFavourite(favourite)
    }

    suspend fun updateFavourite(favourite: Favourite){
        return dao.updateFavList(favourite)
    }

    suspend fun getFavForUser(userId: String): Array<Favourite>{
        return dao.getFavListsForUser(userId)
    }

    suspend fun insertSongData(songData: SongData){
        return dao.insertSongData(songData)
    }


    suspend fun updateSongData(songData: SongData){
        return dao.updateSongData(songData)
    }

    suspend fun getSongData(userId: String): Array<SongData>{
        return dao.getSongData(userId)
    }

    suspend fun getAllUserLog(): Array<UserLog>{
        return dao.getAllUserLog()
    }


    suspend fun insertUserLog(user: UserLog){
        return dao.insertUserLog(user)
    }

    suspend fun deleteUserLog(userLog: UserLog){
        return dao.deleteUserLog(userLog)
    }


    suspend fun updateUserLog(user: UserLog){
        return dao.updateUserLog(user)
    }




}