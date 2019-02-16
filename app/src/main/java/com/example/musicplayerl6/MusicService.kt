package com.example.musicplayerl6

import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log

import java.util.ArrayList;


class MusicService:Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    //media player
    lateinit var player:MediaPlayer
    //song list
    lateinit var songs:ArrayList<Song>
    //current position
    var songPosn:Int = 0
    //binder
    var musicBind:IBinder = MusicBinder()
    override fun onCreate() {
        //create the service
        super.onCreate()
        //initialize position
        songPosn = 0
        //create player
        player = MediaPlayer()
        //initialize
        initMusicPlayer()
    }
    fun initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(),
            PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        //player.setAudioAttributes(STREAM_MUSIC)
        //set listeners
        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }
    //pass song list
    fun setList(theSongs:ArrayList<Song>) {
        songs = theSongs
    }
    //binder
    inner class MusicBinder: Binder() {
        internal val service:MusicService
            get() {
                return this@MusicService
            }
    }
    //activity will bind to service
    override fun onBind(intent:Intent):IBinder {
        return musicBind
    }
    //release resources when unbind
    override fun onUnbind(intent:Intent):Boolean {
        player.stop()
        player.release()
        return false
    }
    //play a song
    fun playSong() {
        //play
        player.reset()
        //get song
        val playSong = songs.get(songPosn)
        //get id
        val currSong = playSong.getID()
        //set uri
        val trackUri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            currSong)
        //set the data source
        try
        {
            player.setDataSource(getApplicationContext(), trackUri)
        }
        catch (e:Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
        player.prepareAsync()
    }
    //set the song
    fun setSong(songIndex:Int) {
        songPosn = songIndex
    }
    override fun onCompletion(mp: MediaPlayer) {
        // TODO Auto-generated method stub
    }
    override fun onError(mp:MediaPlayer, what:Int, extra:Int):Boolean {
        // TODO Auto-generated method stub
        return false
    }
    override fun onPrepared(mp:MediaPlayer) {
        //start playback
        mp.start()
    }
}