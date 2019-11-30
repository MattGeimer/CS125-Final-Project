package com.example.playlisterforspotify;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("MainActivity", "Reached MainActivity.java onCreate");

        LinearLayout playlistList = findViewById(R.id.playlistList);

        //TODO: Replace this with code that fetches playlists
        for (int i = 0; i < 50; i++) {
            View playlist = getLayoutInflater().inflate(R.layout.playlist_chunk, playlistList, false);
            ImageButton playlistCover = playlist.findViewById(R.id.playlistCover);
            TextView playlistTitle = playlist.findViewById(R.id.playlistTitle);
            TextView playlistAuthor = playlist.findViewById(R.id.playlistAuthor);
            ImageButton expand = playlist.findViewById(R.id.expand);
            ImageButton rateUp = playlist.findViewById(R.id.rateUp);
            ImageButton rateDown = playlist.findViewById(R.id.rateDown);

            //TODO: Replace with data from fetched playlist
            playlistTitle.setText("Joe's Playlist No. " + (i + 1));
            playlistAuthor.setText("Shared by: " + "Joe");
            playlistCover.setVisibility(View.VISIBLE);

            //TODO: Replace stubs with intended functions of each button
            expand.setOnClickListener(unused -> {
                Log.i("Playlist Chunk", "expand button tapped");
            });

            rateUp.setOnClickListener(unused -> {
                Log.i("Playlist Chunk", "rateUp button tapped");
            });

            rateDown.setOnClickListener(unused -> {
                Log.i("Plailist Chunk", "rateDown button tapped");
            });
            playlistList.addView(playlist);
        }
    }
}
