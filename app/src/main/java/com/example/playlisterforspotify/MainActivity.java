package com.example.playlisterforspotify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private PopulateViewWithMyPlaylists populater;
    private LinearLayout playlistList;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("MainActivity", "Reached MainActivity.java onCreate");

        accessToken = getIntent().getStringExtra("accessToken");
        playlistList = findViewById(R.id.playlistList);
        SwipeRefreshLayout swipeRefresh = findViewById(R.id.refresh);

        populater = new PopulateViewWithMyPlaylists(playlistList, this, accessToken);
        populater.execute();
        swipeRefresh.setOnRefreshListener(() -> refresh(swipeRefresh));
    }

    private void refresh(SwipeRefreshLayout refreshLayout) {
        populater.cancel(true);
        playlistList.removeAllViews();
        populater = new PopulateViewWithMyPlaylists(playlistList, this, accessToken);
        populater.execute();
        refreshLayout.setRefreshing(false);
    }
}
