@file:Suppress("DEPRECATION")

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
import java.util.*

import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context


class MusicService():Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    //media player
    var player:MediaPlayer = MediaPlayer()
    //song list
    lateinit var songs:ArrayList<Song>
    //current position
    var songPosn:Int = 0
    //binder
    var musicBind:IBinder = MusicBinder()

    var songTitle:String = ""

    //notification id
    var NOTIFY_ID = 1




    var shuffle:Boolean = false
    var rand:Random = Random(1515155)

    override fun onCreate() {
        //create the service
        super.onCreate()
        //initialize position
        songPosn = 0
        //create player
        //player = MediaPlayer()
        //initialize
        initMusicPlayer()
    }
    fun initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(),
            PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
        songPosn = 0
        //create player
        player = MediaPlayer()
        //initialize
        initMusicPlayer()
        //play
        player.reset()
        //get song
        val playSong = songs.get(songPosn)

        songTitle = playSong.getTitulo()
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
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }
    override fun onError(mp:MediaPlayer, what:Int, extra:Int):Boolean {
        mp.reset();
        return false
    }
    override fun onPrepared(mp:MediaPlayer) {
        //start playback
        mp.start()
    }

    fun getPosn():Int {
        return player.getCurrentPosition()
    }
    fun getDur():Int {
        return player.getDuration()
    }
    fun isPng():Boolean {
        return player.isPlaying()
    }
    fun pausePlayer() {
        player.pause()
    }
    fun seek(posn:Int) {
        player.seekTo(posn)
    }
    fun go() {
        player.start()
        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(this, 0,
            notIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = Notification.Builder(this)
        (builder.setContentIntent(pendInt)
            .setSmallIcon(R.drawable.play)
            .setTicker(songTitle)
            .setOngoing(true)
            .setContentTitle("Playing")
            .setContentText(songTitle))
        val not = builder.build()
        startForeground(NOTIFY_ID, not)
    }

    //skip to previous track
    fun playPrev() {
        songPosn--
        if (songPosn < 0) songPosn = songs.size - 1
        playSong()
    }
    //skip to next
    fun playNext() {
        if (shuffle)
        {
            var newSong = songPosn
            while (newSong == songPosn)
            {
                newSong = rand.nextInt(songs.size)
            }
            songPosn = newSong
        }
        else
        {
            songPosn++
            if (songPosn >= songs.size) songPosn = 0
        }
        playSong()
    }

    //toggle shuffle
    fun setShuffle() {
        if (shuffle)
            shuffle = false
        else
            shuffle = true
    }

    override fun onDestroy() {
        stopForeground(true)
    }

}