package com.example.playlisterforspotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private PopulateViewWithPlaylists populater;
    private LinearLayout playlistList;
    private String accessToken;
    private ArrayList<StoredPlaylist> playlists;

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
                        String firebaseUID = playlist.child("firebase-user-id").getValue(String.class);
                        String screenname = playlist.child("firebase-screenname").getValue(String.class);
                        String spotifyUID = playlist.child("spotify-user-id").getValue(String.class);
                        String playlistID = playlist.child("playlist-id").getValue(String.class);
                        Integer score = playlist.child("rating").getValue(Integer.class);
                        if (score != null) {
                            StoredPlaylist storedPlaylist = new StoredPlaylist
                                    (playlistID, firebaseUID, screenname, spotifyUID, score);
                            playlists.add(storedPlaylist);
                        }
                    }
                }
                populater = new PopulateViewWithPlaylists(playlistList, getApplicationContext(), playlists, accessToken);
                populater.execute();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Database Error", databaseError.getMessage());
            }
        });

        swipeRefresh.setOnRefreshListener(() -> refresh(swipeRefresh));
    }

    private void refresh(SwipeRefreshLayout refreshLayout) {
        populater.cancel(true);
        playlistList.removeAllViews();
        populater = new PopulateViewWithPlaylists(playlistList, this, playlists, accessToken);
        populater.execute();
        refreshLayout.setRefreshing(false);
    }
}