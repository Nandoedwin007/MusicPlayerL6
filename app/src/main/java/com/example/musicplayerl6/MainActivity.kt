package com.example.musicplayerl6

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import kotlinx.android.synthetic.main.activity_main.*

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager
import android.os.Build
import android.text.BoringLayout
import android.util.Log
import android.view.Menu
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;


import com.example.musicplayerl6.MusicService.MusicBinder
import java.util.jar.Manifest


//Referencias utilizadas
// https://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764


class MainActivity : AppCompatActivity(),MediaPlayerControl {

    var songTitle:String = ""

    //notification id
    var NOTIFY_ID = 1

    var paused:Boolean = false
    var playbackPaused:Boolean = false

    override fun canSeekForward(): Boolean {
        return true;
    }

    override fun getDuration(): Int {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    override fun pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    override fun getBufferPercentage(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun seekTo(pos: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCurrentPosition(): Int {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAudioSessionId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canPause(): Boolean {
        return true;
    }

    override fun isPlaying(): Boolean {
        if(musicSrv!=null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    var songList:ArrayList<Song> = ArrayList()
    var musicSrv:MusicService = MusicService()
    var playIntent:Intent = Intent()
    var musicBound:Boolean = false
    lateinit var songView:ListView

    lateinit var controller:MusicController

    fun setController() {
        controller = MusicController(this)

        controller.setPrevNextListeners(object:View.OnClickListener {
            override fun onClick(v:View) {
                playNext()
            }
        }, object:View.OnClickListener {
            override fun onClick(v:View) {
                playPrev()
            }
        })
        controller.setMediaPlayer(this)
        controller.setAnchorView(findViewById(R.id.song_list))
        controller.setEnabled(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if ((checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED))
            {
                requestPermissions(arrayOf<String>(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant
                return
            }
        }

        songView = findViewById<ListView>(R.id.song_list)

        //var songList
        songList = ArrayList<Song>()

        getSongList()
        setController()

        Collections.sort(songList, object:Comparator<Song> {
            override fun compare(a:Song, b:Song):Int {
                return a.getArtista().compareTo(b.getArtista())
            }
        })

        val songAdt:SongAdapter = SongAdapter(this,songList)
        songView.adapter = songAdt




    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        if (paused){
            setController()
            paused = false
        }
    }

    override fun onStop() {
        controller.hide()
        super.onStop()
    }
    //connect to the service
    private val musicConnection = object:ServiceConnection {
        override fun onServiceConnected(name:ComponentName, service:IBinder) {
            val binder = service as MusicBinder
            //get service
            //musicSrv = binder.getService()
            musicSrv = binder.service
            //pass list
            musicSrv.setList(songList)
            musicBound = true
        }
        override fun onServiceDisconnected(name:ComponentName) {
            musicBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (playIntent==null){
            playIntent = Intent(this,MusicService::class.java)
            bindService(playIntent,musicConnection,Context.BIND_AUTO_CREATE)
            startService(playIntent)


        }
    }

    //user song select
    fun songPicked(view:View) {
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()))
        musicSrv.playSong()
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }
    override fun onCreateOptionsMenu(menu: Menu):Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu)
        return true
    }


    override fun onOptionsItemSelected(item:MenuItem):Boolean {
        //menu item selected
        when (item.getItemId()) {
            R.id.action_shuffle -> {
                musicSrv.setShuffle()
            }
            R.id.action_end -> {
                stopService(playIntent)
                musicSrv = null!!
                System.exit(0)
            }
        }//shuffle
        return super.onOptionsItemSelected(item)
    }

    fun getSongList(){
        //retrieve song info
        val musicResolver:ContentResolver = contentResolver
        val musicUri:Uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicUri2:Uri = android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        val musicCursor:Cursor = musicResolver.query(musicUri,null,null,null)


        if(musicCursor != null && musicCursor.moveToFirst()) {
            val titleColumn: Int = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val idColumn: Int = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val artistColumn: Int = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)

            do{
                val thisId:Long = musicCursor.getLong(idColumn)
                val thisTitle:String = musicCursor.getString(titleColumn)
                Log.d("Cancion: ",thisTitle)
                val thisArtist:String = musicCursor.getString(artistColumn)
                songList.add(Song(thisId, thisTitle,thisArtist))

            }
            while (musicCursor.moveToNext())
        }
    }

    override fun onDestroy() {
        stopService(playIntent)
        musicSrv = null!!
        super.onDestroy()

    }

    //play next
    private fun playNext() {
        musicSrv.playNext()
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0)
    }

    //play previous
    private fun playPrev() {
        musicSrv.playPrev()
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0)
    }




}
