package com.example.playlisterforspotify;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

/** Populates a given view with the user's own playlists. */
public class PopulateViewWithMyPlaylists extends AsyncTask<Void, Void, List<PlaylistSimple>> {
    private WeakReference<ViewGroup> parent;
    private WeakReference<Context> context;
    private String accessToken;
    private String myID;

    /**
     * Initializes the view, context, and access token.
     * @param view the view to put all of the playlists in
     * @param c the application context (this)
     * @param token the access token gained from authorization
     */
    PopulateViewWithMyPlaylists(ViewGroup view, Context c, String token) {
        parent = new WeakReference<>(view);
        context = new WeakReference<>(c);
        accessToken = token;
    }

    /**
     * Gets info from the SpotifyApi about the user's playlist as well as the user's id.
     * @param voids no arguments
     * @return a List of the playlists belonging to the user
     */
    @Override
    public List<PlaylistSimple> doInBackground(Void... voids) {
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(accessToken);
        SpotifyService spotify = api.getService();

        List<PlaylistSimple> ownedPlaylists = new ArrayList<>();
        List<PlaylistSimple> allMyPlaylists = spotify.getMyPlaylists().items;
        myID = spotify.getMe().id;

        // We only care about the playlists actually owned by the user, not the playlists the user follows.
        for (PlaylistSimple playlist : allMyPlaylists) {
            String playlistOwnerID = playlist.owner.id;
            if (playlistOwnerID.equals(myID)) {
                ownedPlaylists.add(playlist);
            }
        }

        return ownedPlaylists;
    }

    /**
     * Updates the UI with the info about all of the user's playlists.
     * @param myPlaylists the List of playlists returned by doInBackground
     */
    @Override
    public void onPostExecute(List<PlaylistSimple> myPlaylists) {
        for (PlaylistSimple playlist : myPlaylists) {
            LayoutInflater inflater = (LayoutInflater) context.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View playlistView = inflater.inflate(R.layout.playlist_user_chunk, parent.get(), false);

            ImageButton playlistCover = playlistView.findViewById(R.id.userPlaylistCover);
            TextView playlistTitle = playlistView.findViewById(R.id.userPlaylistTitle);
            ImageButton expand = playlistView.findViewById(R.id.userExpand);
            Button share = playlistView.findViewById(R.id.userShare);
            ProgressBar progress = playlistView.findViewById(R.id.userPlaylistCoverProgress);
            TextView score = playlistView.findViewById(R.id.userPlaylistScore);

            playlistTitle.setText(playlist.name);

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
            PopulateViewWithTracks populater = new PopulateViewWithTracks(trackList, context.get(), playlist.id, myID, accessToken);

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

            // If playlist has already been showed, show its score. Else, give the user the option to share.
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("shared-playlists");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(playlist.id)) {
                        showScore(dataSnapshot);
                    } else {
                        share.setVisibility(View.VISIBLE);
                        share.setOnClickListener(unused -> {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            ref.child(playlist.id).child("rating").setValue(0);
                            ref.child(playlist.id).child("firebase-user-id").setValue(user.getUid());
                            ref.child(playlist.id).child("firebase-screenname").setValue(user.getDisplayName());
                            ref.child(playlist.id).child("spotify-user-id").setValue(myID);
                            ref.child(playlist.id).child("playlist-id").setValue(playlist.id);
                            share.setVisibility(View.GONE);
                            showScore(dataSnapshot);
                            Toast.makeText(context.get(), "Shared!", Toast.LENGTH_LONG).show();
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Database Error", databaseError.getMessage());
                }

                private void showScore(@NonNull DataSnapshot dataSnapshot) {
                    Long rating = 0L;
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.getKey().equals(playlist.id)) {
                            rating = childSnapshot.child("rating").getValue(Long.class);
                            break;
                        }
                    }
                    score.setText(String.format(Locale.getDefault(), "%d", rating));
                    score.setVisibility(View.VISIBLE);
                }
            });

        }
    }
}
