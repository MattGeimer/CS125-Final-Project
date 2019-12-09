package com.example.playlisterforspotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Playlist;

public class MainActivity extends AppCompatActivity {
    //private PopulateViewWithMyPlaylists populater;
    private LinearLayout playlistList;
    private String accessToken;
    private ArrayList<MyPlaylist> playlists;

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

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("shared-playlists");

        playlists = new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot playlist : dataSnapshot.getChildren()) {
                        Integer score = playlist.child("rating").getValue(Integer.class);
                        if (score != null) {
                            MyPlaylist temp = new MyPlaylist(playlist.child("playlist-id").getValue(String.class), score);
                            playlists.add(temp);
                        }
                    }
                    for (int i = 0; i < playlists.size(); i++) {
                        Log.i("mgeimer2", playlists.get(i).getID());
                        Log.i("mgeimer2", "" + playlists.get(i).getScore());
                    }
                    //TODO: Load chunk using playlist object data and spotify API
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Database Error", databaseError.getMessage());
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

    private class MyPlaylist {

        private String id;
        private int score;

        public MyPlaylist(String id, int score) {
            this.id = id;
            this.score = score;
        }
        public String getID() {return id;}

        public int getScore() {return score;}
    }

}