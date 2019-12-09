package com.example.playlisterforspotify;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Playlist;

public class PopulateViewWithPlaylists extends AsyncTask<Void, Void, HashMap<Playlist, List<String>>> {
    private WeakReference<ViewGroup> parent;
    private WeakReference<Context> context;
    private List<StoredPlaylist> storedPlaylists;
    private String accessToken;

    PopulateViewWithPlaylists(ViewGroup view, Context c, List<StoredPlaylist> sPlaylists, String token) {
        parent = new WeakReference<>(view);
        context = new WeakReference<>(c);
        storedPlaylists = sPlaylists;
        accessToken = token;
    }

    @Override
    public HashMap<Playlist, List<String>> doInBackground(Void... voids) {
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(accessToken);
        SpotifyService spotify = api.getService();

        HashMap<Playlist, List<String>> playlists = new HashMap<>();
        for (StoredPlaylist storedPlaylist : storedPlaylists) {
            String firebaseID = storedPlaylist.getFirebaseUID();
            String screenname = storedPlaylist.getSceenname();
            List<String> firebaseInfo = new ArrayList<>();
            firebaseInfo.add(firebaseID);
            firebaseInfo.add(screenname);
            Playlist playlist = spotify.getPlaylist(storedPlaylist.getSpotifyUID(), storedPlaylist.getPlaylistID());

            playlists.put(playlist, firebaseInfo);
        }
        return playlists;
    }

    @Override
    public void onPostExecute(HashMap<Playlist, List<String>> playlists) {
        for (Map.Entry playlistMap : playlists.entrySet()) {
            Playlist playlist = (Playlist) playlistMap.getKey();
            List<String> firebaseInfo = (List<String>) playlistMap.getValue();
            LayoutInflater inflater = (LayoutInflater) context.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View playlistView = inflater.inflate(R.layout.playlist_chunk, parent.get(), false);

            ProgressBar progress = playlistView.findViewById(R.id.playlistCoverProgress);
            ImageButton playlistCover = playlistView.findViewById(R.id.playlistCover);
            TextView playlistTitle = playlistView.findViewById(R.id.playlistTitle);
            TextView playlistAuthor = playlistView.findViewById(R.id.playlistAuthor);
            ImageButton expand = playlistView.findViewById(R.id.expand);
            ImageButton rateUp = playlistView.findViewById(R.id.rateUp);
            ImageButton rateDown = playlistView.findViewById(R.id.rateDown);
            TextView score = playlistView.findViewById(R.id.score);

            playlistTitle.setText(playlist.name);

            StringBuilder authorString = new StringBuilder("Shared by: ");
            authorString.append(firebaseInfo.get(1));
            playlistAuthor.setText(authorString);

            new FillViewWithCoverImage(playlistCover, progress).execute(playlist);
            playlistCover.setOnClickListener(unused -> {
                String url = playlist.external_urls.get("spotify");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.get().startActivity(intent);
            });

            parent.get().addView(playlistView);

            /* A new LinearLayout must be made to hold the tracks;
                the view must exist before tracks can be added to it. */
            LinearLayout trackList = new LinearLayout(context.get());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            trackList.setLayoutParams(layoutParams);
            trackList.setOrientation(LinearLayout.VERTICAL);
            trackList.setVisibility(View.GONE);
            parent.get().addView(trackList);
            PopulateViewWithTracks populater = new PopulateViewWithTracks
                    (trackList, context.get(), playlist.id, playlist.owner.id, accessToken);

            expand.setOnClickListener(arrow -> {
                try {
                    populater.execute();
                } catch(IllegalStateException e) {
                    // The tracklist has already been made
                    Log.i("P.V.W/Tracks not called", "already executed");
                }

                // Flip arrow by 180 degrees
                arrow.setRotation(arrow.getRotation() + 180);

                if (trackList.getVisibility() == View.VISIBLE) {
                    trackList.setVisibility(View.GONE);
                } else {
                    trackList.setVisibility(View.VISIBLE);
                }
            });

            DatabaseReference overallRatingRef = FirebaseDatabase.getInstance().getReference()
                    .child("shared-playlists").child(playlist.id).child("rating");
            overallRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Long rating = dataSnapshot.getValue(Long.class);
                    score.setText(String.format(Locale.getDefault(), "%d", rating));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Database Error", databaseError.getMessage());
                }
            });

            DatabaseReference myRatingsRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("ratings");
            myRatingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(playlist.id)) {
                        if (dataSnapshot.child(playlist.id).getValue(Long.class) == 1) {
                            rateDown.setTag(false);
                            rateUp.setTag(true);
                            rateUp.clearColorFilter();
                            rateUp.setColorFilter(R.color.colorAccent, PorterDuff.Mode.SRC_IN);
                        } else if (dataSnapshot.child(playlist.id).getValue(Long.class) == -1) {
                            rateUp.setTag(false);
                            rateDown.setTag(true);
                            rateDown.clearColorFilter();
                            rateDown.setColorFilter(R.color.colorAccentComplementary, PorterDuff.Mode.SRC_IN);
                        } else {
                            rateUp.setTag(false);
                            rateDown.setTag(false);
                            myRatingsRef.child(playlist.id).setValue(null);
                        }
                    } else {
                        rateUp.setTag(false);
                        rateDown.setTag(false);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Database Error", databaseError.getMessage());
                }
            });

            rateUp.setOnClickListener(unused -> {
                if ((boolean) rateUp.getTag()) {
                    rateUp.setTag(false);
                    rateUp.clearColorFilter();
                    rateUp.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

                    myRatingsRef.child(playlist.id).setValue(0);
                    overallRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Long rating = dataSnapshot.getValue(Long.class) - 1;
                            overallRatingRef.setValue(rating);
                            score.setText(String.format(Locale.getDefault(), "%d", rating));

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Database Error", databaseError.getMessage());
                        }
                    });
                } else if ((boolean) rateDown.getTag()) {
                    rateDown.setTag(false);
                    rateDown.clearColorFilter();
                    rateDown.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                    rateUp.setTag(true);
                    rateUp.clearColorFilter();
                    rateUp.setColorFilter(R.color.colorAccent, PorterDuff.Mode.SRC_IN);

                    myRatingsRef.child(playlist.id).setValue(1);
                    overallRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Long rating = dataSnapshot.getValue(Long.class) + 2;
                            overallRatingRef.setValue(rating);
                            score.setText(String.format(Locale.getDefault(), "%d", rating));

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Database Error", databaseError.getMessage());
                        }
                    });
                } else {
                    rateUp.setTag(true);
                    rateUp.clearColorFilter();
                    rateUp.setColorFilter(R.color.colorAccent, PorterDuff.Mode.SRC_IN);

                    myRatingsRef.child(playlist.id).setValue(1);
                    overallRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Long rating = dataSnapshot.getValue(Long.class) + 1;
                            overallRatingRef.setValue(rating);
                            score.setText(String.format(Locale.getDefault(), "%d", rating));

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Database Error", databaseError.getMessage());
                        }
                    });
                }
            });

            rateDown.setOnClickListener(unused -> {
                if ((boolean) rateDown.getTag()) {
                    rateDown.setTag(false);
                    rateDown.clearColorFilter();
                    rateDown.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

                    myRatingsRef.child(playlist.id).setValue(0);
                    overallRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Long rating = dataSnapshot.getValue(Long.class) + 1;
                            overallRatingRef.setValue(rating);
                            score.setText(String.format(Locale.getDefault(), "%d", rating));

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Database Error", databaseError.getMessage());
                        }
                    });
                } else if ((boolean) rateUp.getTag()) {
                    rateUp.setTag(false);
                    rateUp.clearColorFilter();
                    rateUp.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                    rateDown.setTag(true);
                    rateDown.clearColorFilter();
                    rateDown.setColorFilter(R.color.colorAccentComplementary, PorterDuff.Mode.SRC_IN);

                    myRatingsRef.child(playlist.id).setValue(-1);
                    overallRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Long rating = dataSnapshot.getValue(Long.class) - 2;
                            overallRatingRef.setValue(rating);
                            score.setText(String.format(Locale.getDefault(), "%d", rating));

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Database Error", databaseError.getMessage());
                        }
                    });
                } else {
                    rateDown.setTag(true);
                    rateDown.clearColorFilter();
                    rateDown.setColorFilter(R.color.colorAccent, PorterDuff.Mode.SRC_IN);

                    myRatingsRef.child(playlist.id).setValue(-1);
                    overallRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Long rating = dataSnapshot.getValue(Long.class) - 1;
                            overallRatingRef.setValue(rating);
                            score.setText(String.format(Locale.getDefault(), "%d", rating));

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Database Error", databaseError.getMessage());
                        }
                    });
                }
            });
        }
    }
}
