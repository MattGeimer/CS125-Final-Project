package com.example.playlisterforspotify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("MainActivity", "Reached MainActivity.java onCreate");

        String accessToken = getIntent().getStringExtra("accessToken");
        LinearLayout playlistList = findViewById(R.id.playlistList);

        new PopulateViewWithMyPlaylists(playlistList, this, accessToken).execute();
    }
}
