package com.example.playlisterforspotify;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;

public class PopulateViewWithTracks extends AsyncTask<Void, Void, List<PlaylistTrack>> {
    private WeakReference<ViewGroup> parent;
    private WeakReference<Context> context;
    private String playlistID;
    private String userID;
    private String accessToken;

    PopulateViewWithTracks(ViewGroup view, Context c, String plistID, String uID, String token) {
        parent = new WeakReference<>(view);
        context = new WeakReference<>(c);
        playlistID = plistID;
        userID = uID;
        accessToken = token;
    }

    @Override
    public List<PlaylistTrack> doInBackground(Void... voids) {
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(accessToken);
        SpotifyService spotify = api.getService();

        return spotify.getPlaylistTracks(userID, playlistID).items;
    }

    @Override
    public void onPostExecute(List<PlaylistTrack> tracks) {
        for (PlaylistTrack playlistTrack : tracks) {
            LayoutInflater inflater = (LayoutInflater) context.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View tracklistView = inflater.inflate(R.layout.track_chunk, parent.get(), false);

            ImageButton albumCover = tracklistView.findViewById(R.id.albumCover);
            TextView songTitle = tracklistView.findViewById(R.id.songTitle);
            TextView songArtist = tracklistView.findViewById(R.id.songArtist);
            Track track = playlistTrack.track;
            List<ArtistSimple> artists = track.artists;

            songTitle.setText(track.name);

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

            new FillViewWithCoverImage(albumCover).execute(track.album);

            parent.get().addView(tracklistView);
        }
    }
}