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
import android.view.Menu
import android.view.MenuItem;
import android.view.View;

import com.example.musicplayerl6.MusicService.MusicBinder
import java.util.jar.Manifest


//Referencias utilizadas
// https://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764


class MainActivity : AppCompatActivity() {

    var songList:ArrayList<Song> = ArrayList()
    var musicSrv:MusicService = MusicService()
    var playIntent:Intent = Intent()
    var musicBound:Boolean = false
    lateinit var songView:ListView

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

        val songView:ListView = findViewById<ListView>(R.id.song_list)

        //var songList
        var songList = ArrayList<Song>()

        getSongList()

        Collections.sort(songList, object:Comparator<Song> {
            override fun compare(a:Song, b:Song):Int {
                return a.getArtista().compareTo(b.getArtista())
            }
        })

        val songAdt:SongAdapter = SongAdapter(this,songList)
        songView.adapter = songAdt





    }

//    lateinit var musicConnection:ServiceConnection
//
//    fun onServiceConnected(name:ComponentName,service:IBinder){
//        var binder:MusicService.MusicBinder = service as MusicService.MusicBinder
//        //get service
//        musicSrv = binder.service
//        //pass list
//        musicSrv.setList(songList)
//        musicBound = true
//    }
//    fun onServiceDisconnected(name:ComponentName){
//        musicBound = false
//
//    }

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
    }
    override fun onCreateOptionsMenu(menu: Menu):Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu)
        return true
    }
    override fun onOptionsItemSelected(item:MenuItem):Boolean {
        //menu item selected
        when (item.getItemId()) {
            R.id.action_shuffle -> {}
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
        val musicCursor:Cursor = musicResolver.query(musicUri,null,null,null)


        if(musicCursor.moveToFirst()) {
            val titleColumn: Int = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val idColumn: Int = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val artistColumn: Int = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)

            do{
                val thisId:Long = musicCursor.getLong(idColumn)
                val thisTitle:String = musicCursor.getString(titleColumn)
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


}
