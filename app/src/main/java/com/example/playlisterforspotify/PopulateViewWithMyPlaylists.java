package com.example.playlisterforspotify;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        context = new WeakReference<>(c);
        accessToken = token;
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
            View playlistView = inflater.inflate(R.layout.playlist_user_chunk, parent.get(), false);

            ImageButton playlistCover = playlistView.findViewById(R.id.userPlaylistCover);
            TextView playlistTitle = playlistView.findViewById(R.id.userPlaylistTitle);
            ImageButton expand = playlistView.findViewById(R.id.userExpand);
            Button share = playlistView.findViewById(R.id.userShare);

            playlistTitle.setText(playlist.name);

            new FillViewWithCoverImage(playlistCover).execute(playlist);

            parent.get().addView(playlistView);
        }
    }
}
