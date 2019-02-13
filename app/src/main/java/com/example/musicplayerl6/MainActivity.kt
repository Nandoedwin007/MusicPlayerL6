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


//Referencias utilizadas
// https://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val songList:ArrayList<Song>
        val songView:ListView

    }
}
