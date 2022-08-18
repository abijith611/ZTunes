package com.example.mymusicapplication.viewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.mymusicapplication.R
import com.example.mymusicapplication.activity.MainActivity
import com.example.mymusicapplication.db.*
import com.example.mymusicapplication.notification.NotificationHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

@OptIn(DelicateCoroutinesApi::class)
class SongViewModel(private val repository: SongRepository):ViewModel() {

    private val allSongs = runBlocking {
        return@runBlocking repository.getAllSongs()
    }
    init {
        if(getAllUsers().isEmpty()) {
            insertUser(User(0, "admin", "admin", "Admin", "9840123456"))
        }
        if(allSongs.isEmpty()) {
            GlobalScope.launch {
                repository.insertSong(
                    Song(
                        1,
                        "Cumbia City",
                        "NCS",
                        "An Jone",
                        R.raw.cumbia_city,
                        R.mipmap.ncsstart,
                        "EDM"
                    )
                )
                repository.insertSong(
                    Song(
                        2,
                        "Let It Go",
                        "NCS",
                        "Unknown",
                        R.raw.let_it_go,
                        R.mipmap.ncs2,
                        "EDM"
                    )
                )
                repository.insertSong(
                    Song(
                        3,
                        "The Future Bass",
                        "NCS",
                        "Kasbo",
                        R.raw.the_future_bass,
                        R.mipmap.ncs3,
                        "EDM"
                    )
                )
                repository.insertSong(
                    Song(
                        4,
                        "The Saphire City",
                        "NCS",
                        "Unknown",
                        R.raw.the_sapphire_city,
                        R.mipmap.ncs41,
                        "EDM"
                    )
                )
                repository.insertSong(
                    Song(
                        5,
                        "Shape of You",
                        "Divide",
                        "Ed Sheeran",
                        R.raw.shape_of_you,
                        R.mipmap.divide_cover,
                        "Romantic"
                    )
                )
                repository.insertSong(
                    Song(
                        6,
                        "Perfect",
                        "Divide",
                        "Ed Sheeran",
                        R.raw.perfect,
                        R.mipmap.divide_cover,
                        "Romantic"
                    )
                )
                repository.insertSong(
                    Song(
                        7,
                        "Love Story",
                        "Fearless",
                        "Taylor Swift",
                        R.raw.love_story,
                        R.mipmap.fearless_cover,
                        "Romantic"
                    )
                )
                repository.insertSong(
                    Song(
                        8,
                        "Blank Space",
                        "1989",
                        "Taylor Swift",
                        R.raw.blankspace,
                        R.mipmap.cover_1989,
                        "Romantic"
                    )
                )
                repository.insertSong(
                    Song(
                        9,
                        "Dusk Till Dawn",
                        "Icarus Falls",
                        "Zayn, Sia",
                        R.raw.dusk_till_dawn,
                        R.mipmap.dusk_till_dawn_cover,
                        "Romantic"
                    )
                )
                repository.insertSong(
                    Song(
                        10,
                        "On the Floor",
                        "Love?",
                        "Jennifer Lopez",
                        R.raw.on_the_floor,
                        R.mipmap.love_album,
                        "Party"
                    )
                )
                repository.insertSong(
                    Song(
                        11,
                        "Lean On",
                        "Peace is the Mission",
                        "Major Lazor",
                        R.raw.lean_on,
                        R.mipmap.peace_is_the_mission,
                        "Party"
                    )
                )
                repository.insertSong(
                    Song(
                        12,
                        "Taki Taki",
                        "Carte Blanche",
                        "Selena Gomez",
                        R.raw.taki_taki,
                        R.mipmap.carte_blanche,
                        "Party"
                    )
                )
                repository.insertSong(
                    Song(
                        13,
                        "Waka Waka",
                        "Listen Up! The Official 2010 Fifa World Cup Album",
                        "Shakira",
                        R.raw.waka_waka,
                        R.mipmap.listen_up_the_offl_2010_fifa_world_cup_album,
                        "Party"
                    )
                )
                repository.insertSong(
                    Song(
                        14,
                        "Animals",
                        "Gold Skies",
                        "Martin Garrix",
                        R.raw.animals,
                        R.mipmap.gold_skies,
                        "Party"
                    )
                )
                repository.insertSong(
                    Song(
                        15,
                        "Roar",
                        "Prism",
                        "Katy Perry",
                        R.raw.roar,
                        R.mipmap.prism_album,
                        "Popular"
                    )
                )
                repository.insertSong(
                    Song(
                        16,
                        "Starboy",
                        "Starboy",
                        "The Weeknd",
                        R.raw.starboy,
                        R.mipmap.starboy_album,
                        "Popular"
                    )
                )
                repository.insertSong(
                    Song(
                        17,
                        "New Rules",
                        "Dua Lipa",
                        "Dua Lipa",
                        R.raw.new_rules,
                        R.mipmap.dua_lipa_album,
                        "Popular"
                    )
                )
                repository.insertSong(
                    Song(
                        18,
                        "Empire State of Mind",
                        "The Blueprint 3",
                        "Jay-Z",
                        R.raw.empire_state_of_mind,
                        R.mipmap.the_blueprint_3,
                        "Popular"
                    )
                )
                repository.insertSong(
                    Song(
                        19,
                        "Uptown Funk",
                        "Uptown Funk",
                        "Mark Ronson",
                        R.raw.uptown_funk,
                        R.mipmap.uptown_special,
                        "Popular"
                    )
                )
                repository.insertSong(
                    Song(
                        20,
                        "Beautiful People",
                        "No.6 Collaborations Project",
                        "Ed Sheeran",
                        R.raw.beautiful_people,
                        R.mipmap.no_6_collaborations_project,
                        "Popular"
                    )
                )
                repository.insertSong(
                    Song(
                        21,
                        "Bad Blood",
                        "1989",
                        "Taylor Swift",
                        R.raw.bad_blood,
                        R.mipmap.cover_1989,
                        "Popular"
                    )
                )
                repository.insertSong(
                    Song(
                        22,
                        "Dark Horse",
                        "Prism",
                        "Katy Perry",
                        R.raw.dark_horse,
                        R.mipmap.prism_album,
                        "Party"
                    )
                )
                repository.insertSong(
                    Song(
                        23,
                        "Bon Appetit",
                        "Witness",
                        "Katy Perry",
                        R.raw.bon_appetit,
                        R.mipmap.witness_album,
                        "Party"
                    )
                )
                repository.insertSong(
                    Song(
                        24,
                        "The Hills",
                        "Beauty Behind the madness",
                        "The Weeknd",
                        R.raw.the_hills,
                        R.mipmap.beauty_behind_the_madness,
                        "EDM"
                    )
                )
                repository.insertSong(
                    Song(
                        25,
                        "Blinding Lights",
                        "Blinding Lights",
                        "The Weeknd",
                        R.raw.blinding_lights,
                        R.mipmap.blinding_lights_album,
                        "Popular"
                    )
                )
            }
        }
    }

        


//        val allPlaylists = runBlocking {
//            return@runBlocking repository.getAllPlaylist()
//        }

        fun getPlaylistForUser(userId: String?): Array<Playlist>{
            val playlists = runBlocking {
                return@runBlocking repository.getPlaylistsForUser(userId!!)
            }
            return playlists
        }

//    fun getPlaylists(): Array<Playlist>{
//        val allPlaylists = runBlocking {
//            return@runBlocking repository.getAllPlaylist()
//        }
//        return allPlaylists
//    }

//    fun getPlaylists(userId: String): Array<UserWithPlaylists>{
//        val allPlaylists = runBlocking {
//            return@runBlocking repository.getUserWithPlaylists(userId)
//        }
//        return allPlaylists
//    }

    fun getSearchPlaylists(query: String, userId: String?): Array<Playlist>{
        val searchPlaylist = runBlocking {
            return@runBlocking repository.searchPlaylist(query, userId!!)
        }
        return searchPlaylist
    }


    fun getGenreSongs(genre: String): Array<Song>{
            val genreSongs = runBlocking {
                return@runBlocking repository.getGenreBasedSongs(genre)
            }
            return genreSongs
    }

    fun getArtistSongs(artist: String): Array<Song>{
        val artistSongs = runBlocking {
            return@runBlocking repository.getArtistBasedSongs(artist)
        }
        return artistSongs
    }

    fun getSearchSongs(query: String): Array<Song>{
        val searchSongs = runBlocking {
            return@runBlocking repository.searchSong(query)
        }
        return searchSongs
    }

    fun getGenreSearchSongs(genre: String, query: String): Array<Song> {
        val finalSongArray: ArrayList<Song> = arrayListOf()
        val searchSongs = getSearchSongs(query)
        for(song in searchSongs){
            if(song.genre == genre){
                finalSongArray.add(song)
            }
        }
        val arr:Array<Song> = emptyArray()
        return finalSongArray.toArray(arr)
    }

    fun getArtistSearchSongs(artist: String, query: String): Array<Song> {
        val finalSongArray: ArrayList<Song> = arrayListOf()
        val searchSongs = getSearchSongs(query)
        for(song in searchSongs){
            if(song.artist == artist){
                finalSongArray.add(song)
            }
        }
        val arr:Array<Song> = emptyArray()
        return finalSongArray.toArray(arr)
    }

    fun getPlaylistSearchSongs(userId: String, query: String): Array<Song>{
        val finalSongArray: ArrayList<Song> = arrayListOf()
        val playlist = getPlaylistForUser(userId)
        val searchSongs = getSearchSongs(query)
        for(song in searchSongs){
            if(playlist[0].playlist.contains(song)){
                finalSongArray.add(song)
            }
        }
        val arr: Array<Song> = emptyArray()
        return finalSongArray.toArray(arr)
    }

    fun addSongsPlaylist(song: Song, playlist: Playlist){
        playlist.playlist.add(song)
        runBlocking{
            repository.updatePlaylist(playlist)
        }
    }

    fun deleteSongPlaylist(song: Song, playlist: Playlist){
        playlist.playlist.remove(song)
        runBlocking{
            repository.updatePlaylist(playlist)
        }
    }

    fun deletePlaylist(playlist: Playlist){
        runBlocking { repository.deletePlaylist(playlist) }
    }

    fun updatePlaylist(playlist: Playlist){
        runBlocking {
            repository.updatePlaylist(playlist)
        }
    }

    fun insertUser(user: User){
        val fav = Favourite(user.email!!, ArrayList())
        val data = SongData(user.email!!, ArrayList(), ArrayList())
        if(getFavForUser(user.email!!).isEmpty() && getSongData(user.email!!).isEmpty()){
            insertFavourite(fav)
            insertSongData(data)
        }
        runBlocking {
            repository.insertUser(user)
            repository.insertUserLog(UserLog(0, user.email!!, LocalDateTime.now()))
        }
    }

    private fun getAllUsers(): Array<User>{
        val users = runBlocking{
            return@runBlocking repository.getUsers()
        }
        return users
    }

    fun isAuthenticated(email: String, pwd: String): Int{
        val users = getAllUsers()
        for(user in users){
            if(user.email == email){
                return if(user.pwd == pwd){
                    1
                } else{
                    0
                }

            }

        }
        return -1
    }

    fun getUser(email: String?): User?{
        val users = getAllUsers()
        for(user in users){
            if(user.email == email){
                return user
            }
        }
        return null
    }

    fun userLogOut(){
        Log.i("userLog1",getAllUserLog().toList().toString())
        if(getAllUserLog().isNotEmpty()){
            val userLog = getAllUserLog()[0]
            deleteUserLog(userLog)
        }
        Log.i("userLog2",getAllUserLog().toList().toString())
    }

    fun userLogIn(user: User){
        if(getAllUserLog().isEmpty())
            insertUserLog(UserLog( 0,user.email!!, LocalDateTime.now()))
    }

//    private fun updateUser(user:User){
//        runBlocking { repository.updateUser(user) }
//    }

    private fun insertFavourite(favourite: Favourite){
        runBlocking { repository.insertFav(favourite) }
    }

    fun getFavForUser(userId: String?): Array<Favourite> {
        return runBlocking { repository.getFavForUser(userId!!) }
    }

    fun addSongsFav(song: Song, favourite: Favourite){
        favourite.favList.add(song)
        runBlocking { repository.updateFavourite(favourite) }
    }

    fun removeSongsFav(song: Song, favourite: Favourite){
        favourite.favList.remove(song)
        runBlocking { repository.updateFavourite(favourite) }
    }

    fun getFavSearchSongs(userId: String?, query: String): Array<Song>{
        val finalSongArray: ArrayList<Song> = arrayListOf()
        val fav =getFavForUser(userId)
        val searchSongs = getSearchSongs(query)
        for(song in searchSongs){
            if(song in fav[0].favList){
                finalSongArray.add(song)
            }
        }
        val arr:Array<Song> = emptyArray()
        return finalSongArray.toArray(arr)
    }

    fun updateUser(user: User, name: String, mobile: String ){
        val user1 =getUser(user.email) as User
        user1.name = name
        user1.mobileNumber = mobile
        runBlocking {
            repository.updateUser(user1)
        }
    }


    override fun onCleared() {
        if(MainActivity.notActive.value == true){
            NotificationHandler.notificationManager?.cancel(0)
            //NotificationHandler.notificationManager?.deleteNotificationChannel("CHANNEL_2")
        }
        super.onCleared()
    }

    private fun insertSongData(songData: SongData){
        runBlocking{ repository.insertSongData(songData) }
    }

    fun getSongData(userId: String?): Array<SongData> {
        return runBlocking { repository.getSongData(userId!!) }
    }

    fun addSongData(songData: SongData, song: Song){
        if(songData.recentSongList.contains(song)){
            songData.recentSongList.remove(song)
        }
        songData.recentSongList.add(song)
        songData.frequentSongList.add(song)
        runBlocking{ repository.updateSongData(songData) }
    }

    private fun insertUserLog(userLog: UserLog){
        runBlocking {
            repository.insertUserLog(userLog)
        }
    }

    private fun deleteUserLog(userLog: UserLog){
        runBlocking {
            repository.deleteUserLog(userLog)
        }
    }

    private fun updateUserLog(userLog: UserLog){
        runBlocking {
            repository.updateUserLog(userLog)
        }
    }

    fun getAllUserLog(): Array<UserLog>{
        return runBlocking { repository.getAllUserLog() }
    }









}