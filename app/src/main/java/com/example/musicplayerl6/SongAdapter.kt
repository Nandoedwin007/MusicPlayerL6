package com.example.musicplayerl6

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

class SongAdapter(c:Context,theSongs:ArrayList<Song>):BaseAdapter() {

    var songs = theSongs

    //lateinit var songInf:LayoutInflater
    var songInf = LayoutInflater.from(c)


    //private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getCount():Int {
        return 0
    }

    override fun getItem(position: Int): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemId(position: Int): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //Map to song layout
        val songLay:LinearLayout = songInf.inflate(R.layout.song,parent,false) as LinearLayout

        //get title and artist views
        val songView:TextView = songLay.findViewById<TextView>(R.id.song_title)
        val artistView:TextView = songLay.findViewById<TextView>(R.id.song_artist)
        //get song using position
        val currSong:Song = songs.get(position)
        //Get song title and artis strings
        songView.setText(currSong.getTitulo())
        artistView.setText(currSong.getArtista())
        //set position as tag
        songLay.setTag(position)
        return songLay




    }

}