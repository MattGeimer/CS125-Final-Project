package com.example.playlisterforspotify;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class PopulateViewWithMyPlaylists extends AsyncTask<Void, Void, List<PlaylistSimple>> {
    private WeakReference<ViewGroup> parent;
    private WeakReference<Context> context;
    private String accessToken;

    PopulateViewWithMyPlaylists(ViewGroup view, Context c, String token) {
        parent = new WeakReference<>(view);
        accessToken = token;
        context = new WeakReference<>(c);
    }

    public List<PlaylistSimple> doInBackground(Void... voids) {
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(accessToken);
        SpotifyService spotify = api.getService();

        List<PlaylistSimple> ownedPlaylists = new ArrayList<>();
        List<PlaylistSimple> allMyPlaylists = spotify.getMyPlaylists().items;
        String myID = spotify.getMe().id;

        // We only care about the playlists actually owned by the user, not the playlists the user follows.
        for (PlaylistSimple playlist : allMyPlaylists) {
            String playlistOwnerID = playlist.owner.id;
            if (playlistOwnerID.equals(myID)) {
                ownedPlaylists.add(playlist);
            }
        }

        return ownedPlaylists;
    }

    public void onPostExecute(List<PlaylistSimple> myPlaylists) {
        for (PlaylistSimple playlist : myPlaylists) {
            LayoutInflater inflater = (LayoutInflater) context.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View playlistView = inflater.inflate(R.layout.playlist_chunk, parent.get(), false);

            ImageButton playlistCover = playlistView.findViewById(R.id.playlistCover);
            TextView playlistTitle = playlistView.findViewById(R.id.playlistTitle);
            TextView playlistAuthor = playlistView.findViewById(R.id.playlistAuthor);
            ImageButton expand = playlistView.findViewById(R.id.expand);
            ImageButton rateUp = playlistView.findViewById(R.id.rateUp);
            ImageButton rateDown = playlistView.findViewById(R.id.rateDown);

            playlistTitle.setText(playlist.name);
            String authorText = "Shared by: " + playlist.owner.display_name;
            playlistAuthor.setText(authorText);

            parent.get().addView(playlistView);
        }
    }
}
