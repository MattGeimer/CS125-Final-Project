package com.example.playlisterforspotify;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;

/** Populates a given view with a playlist's tracks. */
public class PopulateViewWithTracks extends AsyncTask<Void, Void, List<PlaylistTrack>> {
    private WeakReference<ViewGroup> parent;
    private WeakReference<Context> context;
    private String playlistID;
    private String userID;
    private String accessToken;

    /**
     * Initializes the view, context, playlist id, user id, and access token.
     * @param view view to populate with tracks
     * @param c the application context
     * @param plistID the ID of the playlist
     * @param uID the ID of the user who owns the playlist
     * @param token the access token
     */
    PopulateViewWithTracks(ViewGroup view, Context c, String plistID, String uID, String token) {
        parent = new WeakReference<>(view);
        context = new WeakReference<>(c);
        playlistID = plistID;
        userID = uID;
        accessToken = token;
    }

    /**
     * Gets info from SpotifyApi about the playlist's tracks.
     * @param voids no arguments
     * @return a List of all the tracks in the playlist
     */
    @Override
    public List<PlaylistTrack> doInBackground(Void... voids) {
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(accessToken);
        SpotifyService spotify = api.getService();

        return spotify.getPlaylistTracks(userID, playlistID).items;
    }

    /**
     * Updates UI with info about each track.
     * @param tracks the list of tracks returned by doInBackground
     */
    @Override
    public void onPostExecute(List<PlaylistTrack> tracks) {
        for (PlaylistTrack playlistTrack : tracks) {
            LayoutInflater inflater = (LayoutInflater) context.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View tracklistView = inflater.inflate(R.layout.track_chunk, parent.get(), false);

            ImageButton albumCover = tracklistView.findViewById(R.id.albumCover);
            TextView songTitle = tracklistView.findViewById(R.id.songTitle);
            TextView songArtist = tracklistView.findViewById(R.id.songArtist);
            ProgressBar progress = tracklistView.findViewById(R.id.albumCoverProgress);
            Track track = playlistTrack.track;
            List<ArtistSimple> artists = track.artists;

            songTitle.setText(track.name);

            // Using a StringBuilder as concatenating within a for loop is not recommended
            StringBuilder artistString = new StringBuilder(artists.get(0).name);
            if (artists.size() == 2) {
                String artist = " and " + artists.get(1).name;
                artistString.append(artist);
            } else if (artists.size() > 2) {
                for (int i = 1; i < artists.size() - 1; i++) {
                    String artist = ", " + artists.get(i).name;
                    artistString.append(artist);
                }
                String artist = ", and " + artists.get(artists.size() - 1).name;
                artistString.append(artist);
            }
            songArtist.setText(artistString);

            new FillViewWithCoverImage(albumCover, progress).execute(track.album);
            albumCover.setOnClickListener(unused -> {
                try {
                    String url = track.external_urls.get("spotify");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.get().startActivity(intent);
                } catch (NullPointerException e) {
                    Log.e("No URL found for track", track.name);
                }
            });

            parent.get().addView(tracklistView);
        }
    }
}
