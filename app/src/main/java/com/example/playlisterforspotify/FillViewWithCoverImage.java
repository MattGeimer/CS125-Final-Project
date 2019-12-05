package com.example.playlisterforspotify;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.PlaylistBase;

public class FillViewWithCoverImage extends AsyncTask<Object, Void, Drawable> {
    private WeakReference<ImageView> view;

    FillViewWithCoverImage(ImageView setView) {
        view = new WeakReference<>(setView);
    }

    @Override
    public Drawable doInBackground(Object... playlistOrAlbum) {
        Drawable noImage = new ColorDrawable(Color.TRANSPARENT);

        try {
            String illegalArgMsg = "Argument must be single instance of PlaylistBase or AlbumSimple";

            if (playlistOrAlbum.length != 1) {
                throw new IllegalArgumentException(illegalArgMsg);
            } else if (playlistOrAlbum[0] instanceof PlaylistBase) {
                PlaylistBase playlist = (PlaylistBase) playlistOrAlbum[0];
                List<Image> images = playlist.images;

                if (images.size() > 0) {
                    String coverURL = images.get(0).url;
                    InputStream coverStream = new URL(coverURL).openStream();
                    return Drawable.createFromStream(coverStream, "Playlist Cover");
                } else {
                    return noImage;
                }

            } else if (playlistOrAlbum[0] instanceof AlbumSimple) {
                AlbumSimple album = (AlbumSimple) playlistOrAlbum[0];
                List<Image> images = album.images;

                if (images.size() > 0) {
                    String coverURL = album.images.get(0).url;
                    InputStream coverStream = new URL(coverURL).openStream();
                    return Drawable.createFromStream(coverStream, "Album Cover");
                } else {
                    return noImage;
                }
            } else {
                throw new IllegalArgumentException(illegalArgMsg);
            }
        } catch(Exception e) {
            Log.e(e.getMessage(), e.getStackTrace()[0].toString());
            return noImage;
        }
    }

    @Override
    public void onPostExecute(Drawable image) {
        view.get().setImageDrawable(image);
    }
}
