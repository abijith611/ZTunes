package com.example.mymusicapplication.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Song::class, Playlist::class, User::class, Favourite::class, SongData::class, UserLog::class], version = 1)
@TypeConverters(SongTypeConvertor::class, LocalDateTimeConverter::class)
abstract class SongDatabase: RoomDatabase() {
    abstract val songDao: SongDao

    companion object{

        fun getInstance(context: Context): SongDatabase{
            synchronized(this){
                var instance: SongDatabase? = null
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SongDatabase::class.java,
                        "song_table"
                    )
//                        .addCallback(object:Callback(){
//                            override fun onCreate(db: SupportSQLiteDatabase) {
//                                super.onCreate(db)
//                                GlobalScope.launch{

//                                        instance?.songDao?.insertSong(Song(1,"Cumbia City", "NCS", "An Jone", R.raw.cumbia_city, R.drawable.icon, "EDM"))
//                                        instance?.songDao?.insertSong(Song(2,"Let It Go", "NCS", "Unknown", R.raw.let_it_go, R.drawable.ncs2, "EDM"))
//                                        instance?.songDao?.insertSong(Song(3,"The Future Bass", "NCS", "Kasbo", R.raw.the_future_bass, R.drawable.ncs3, "EDM"))
//                                        instance?.songDao?.insertSong(Song(4,"The Saphire City", "NCS", "Unknown", R.raw.the_sapphire_city, R.drawable.ncs41, "EDM"))
//                                        Log.i("db", "added")
//                                        instance?.songDao?.insertSong(Song(5,"Shape of You", "Divide", "Ed Sheeran", R.raw.shape_of_you, R.drawable.divide_cover, "Romantic"))
//                                        instance?.songDao?.insertSong(Song(6,"Perfect", "Divide", "Ed Sheeran", R.raw.perfect, R.drawable.divide_cover, "Romantic"))
//                                        instance?.songDao?.insertSong(Song(7,"Love Story", "Fearless", "Taylor Swift", R.raw.love_story, R.drawable.fearless_cover, "Romantic"))
//                                        instance?.songDao?.insertSong(Song(8,"Blank Space", "1989", "Taylor Swift", R.raw.blankspace, R.drawable.cover_1989, "Romantic"))
//                                        instance?.songDao?.insertSong(Song(9,"Dusk Till Dawn", "Icarus Falls", "Zayn, Sia", R.raw.dusk_till_dawn, R.drawable.dusk_till_dawn_cover, "Romantic"))
//                                        Log.i("db", "added")
//                                        instance?.songDao?.insertSong(Song(10,"On the Floor", "Love?", "Jennifer Lopez", R.raw.on_the_floor, R.drawable.love_album, "Party"))
//                                        instance?.songDao?.insertSong(Song(11,"Lean On", "Peace is the Mission", "Major Lazor", R.raw.lean_on, R.drawable.peace_is_the_mission, "Party"))
//                                        instance?.songDao?.insertSong(Song(12,"Taki Taki", "Carte Blanche", "Selena Gomez", R.raw.taki_taki, R.drawable.carte_blanche, "Party"))
//                                        instance?.songDao?.insertSong(Song(13,"Waka Waka", "Listen Up! The Official 2010 Fifa World Cup Album", "Shakira", R.raw.waka_waka, R.drawable.listen_up_the_offl_2010_fifa_world_cup_album, "Party"))
//                                        instance?.songDao?.insertSong(Song(14,"Animals", "Gold Skies", "Martin Garrix", R.raw.animals, R.drawable.gold_skies, "Party"))
//                                        Log.i("db", "added")
//                                        instance?.songDao?.insertSong(Song(15,"Roar", "Prism", "Katy Perry", R.raw.roar, R.drawable.prism_album, "Popular"))
//                                        instance?.songDao?.insertSong(Song(16,"Starboy", "Starboy", "The Weeknd", R.raw.starboy, R.drawable.starboy_album, "Popular"))
//                                        instance?.songDao?.insertSong(Song(17,"New Rules", "Dua Lipa", "Dua Lipa", R.raw.new_rules, R.drawable.dua_lipa_album, "Popular"))
//                                        instance?.songDao?.insertSong(Song(18,"Empire State of Mind", "The Blueprint 3", "Jay-Z", R.raw.empire_state_of_mind, R.drawable.the_blueprint_3, "Popular"))
//                                        instance?.songDao?.insertSong(Song(19,"Uptown Funk", "Uptown Funk", "Mark Ronson", R.raw.uptown_funk, R.drawable.uptown_special, "Popular"))
//                                        Log.i("db", "added")
//                                        instance?.songDao?.insertSong(Song(20,"Beautiful People", "No.6 Collaborations Project", "Ed Sheeran", R.raw.beautiful_people, R.drawable.no_6_collaborations_project, "Popular"))
//                                        instance?.songDao?.insertSong(Song(21,"Bad Blood", "1989", "Taylor Swift", R.raw.bad_blood, R.drawable.cover_1989, "Popular"))
//                                        instance?.songDao?.insertSong(Song(22,"Dark Horse", "Prism", "Katy Perry", R.raw.dark_horse, R.drawable.prism_album, "Party"))
//                                        instance?.songDao?.insertSong(Song(23,"Bon Appetit", "Witness", "Katy Perry", R.raw.bon_appetit, R.drawable.witness_album, "Party"))
//                                        instance?.songDao?.insertSong(Song(24,"The Hills", "Beauty Behind the madness", "The Weeknd", R.raw.the_hills, R.drawable.beauty_behind_the_madness, "EDM"))
//                                        instance?.songDao?.insertSong(Song(25,"Blinding Lights", "Blinding Lights", "The Weeknd", R.raw.blinding_lights, R.drawable.blinding_lights_album, "Popular"))
//                                        Log.i("db", "added")

//
//                                }
//                            }
//                        })
                    .fallbackToDestructiveMigration()
                    .build()
                }
                return instance
            }
        }
    }
}