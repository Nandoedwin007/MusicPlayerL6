package com.example.musicplayerl6

class Song (songID:Long,songTitle:String, songArtist:String) {
    val id = songID
    val title = songTitle
    val artist = songArtist

    fun getID():Long {
        return id
    }
    fun getTitulo():String{
        return title
    }

    fun getArtista():String{
        return artist
    }

}