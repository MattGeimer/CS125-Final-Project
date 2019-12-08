package com.example.playlisterforspotify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    //private PopulateViewWithMyPlaylists populater;
    private LinearLayout playlistList;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        Log.i("MainActivity", "Reached MainActivity.java onCreate");

        accessToken = getIntent().getStringExtra("accessToken");
        playlistList = findViewById(R.id.playlistList);
        SwipeRefreshLayout swipeRefresh = findViewById(R.id.refresh);
        TabLayout tabLayout = findViewById(R.id.tabs);

        tabLayout.getTabAt(0).select();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals(getString(R.string.my_profile))) {
                    Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                    intent.putExtra("accessToken", accessToken);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                refresh(swipeRefresh);
            }
        });

        //populater = new PopulateViewWithMyPlaylists(playlistList, this, accessToken);
        //populater.execute();
        swipeRefresh.setOnRefreshListener(() -> refresh(swipeRefresh));
    }

    private void refresh(SwipeRefreshLayout refreshLayout) {
        //populater.cancel(true);
        playlistList.removeAllViews();
        //populater = new PopulateViewWithMyPlaylists(playlistList, this, accessToken);
        //populater.execute();
        refreshLayout.setRefreshing(false);
    }
}