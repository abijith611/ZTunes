package com.example.mymusicapplication.db

import androidx.room.*


@Dao
interface SongDao {

    @Insert
    suspend fun insertSong(song: Song)

    @Query("SELECT * FROM song_table")
    suspend fun getAllSongs(): Array<Song>

    @Query("SELECT * FROM song_table WHERE genre = :genre")
    suspend fun getSongsBasedOnGenre(genre: String): Array<Song>

    @Query("SELECT * FROM song_table WHERE songArtist = :artist")
    suspend fun getSongsBasedOnArtist(artist: String): Array<Song>

    @Query("SELECT * FROM song_table WHERE songTitle LIKE '%'||:searchQuery||'%'")
    suspend fun searchDb(searchQuery: String): Array<Song>

    @Insert
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("SELECT * FROM playlist_table")
    suspend fun getAllPlaylists(): Array<Playlist>

    @Delete
    suspend fun deletePlaylist(playlist: Playlist): Int

    @Update
    suspend fun updatePlaylist(playlist: Playlist): Int

    @Query("SELECT * FROM playlist_table WHERE playlistName LIKE '%'||:searchQuery||'%' AND userId = :userId")
    suspend fun searchPlaylistDb(searchQuery: String, userId: String): Array<Playlist>

    @Transaction
    @Query("SELECT * FROM user_table WHERE userId = :userId")
    suspend fun getUserWithPlaylists(userId: String): Array<UserWithPlaylists>

    @Query("SELECT * FROM playlist_table WHERE userId = :userId ")
    suspend fun getPlaylistsForUser(userId: String): Array<Playlist>

    @Insert
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM user_table")
    suspend fun getUsers(): Array<User>

    @Insert
    suspend fun insertFavourite(favourite: Favourite)

    @Query("SELECT * FROM favourite_table WHERE userId = :userId")
    suspend fun getFavListsForUser(userId: String): Array<Favourite>

    @Update
    suspend fun updateFavList(favourite: Favourite)

    @Delete
    suspend fun deleteFavourite(favourite: Favourite)

    @Insert
    suspend fun insertSongData(songData: SongData)

    @Update
    suspend fun updateSongData(songData: SongData)

    @Delete
    suspend fun deleteSongData(songData: SongData)

    @Query("SELECT * FROM song_data WHERE userId = :userId")
    suspend fun getSongData(userId: String): Array<SongData>

    @Query("SELECT * FROM user_log")
    suspend fun getAllUserLog(): Array<UserLog>

    @Insert
    suspend fun insertUserLog(user: UserLog)

    @Update
    suspend fun updateUserLog(user: UserLog)

    @Delete
    suspend fun deleteUserLog(userLog: UserLog)



}