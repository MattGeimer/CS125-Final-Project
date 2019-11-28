package com.example.playlisterforspotify;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/** Stores and returns information about any track given the track's ID. */
public class TrackInfo {
    /** The current track. */
    private Track track;
    /** Stores all of the audio features of the track. */
    private AudioFeaturesTrack audio;

    /**
     * Creates a new TrackInfo to store relevant information about a Spotify track.
     *
     * @param accessToken the access token acquired from authorization.
     * @param trackID the track's unique ID.
     */
    TrackInfo(String accessToken, String trackID) {
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(accessToken);
        SpotifyService spotify = api.getService();
        track = spotify.getTrack(trackID);
        audio = spotify.getTrackAudioFeatures(trackID);
    }

    /**
     * Gets the name of the track.
     * @return name of the track
     */
    public String getName() {
        return track.name;
    }

    /**
     * Gets the names of all of the artists of the track.
     * @return List of the names of all of the artists
     */
    public List<String> getArtistNames() {
        List<ArtistSimple> artists = track.artists;
        List<String> artistNames = new ArrayList<>(artists.size());

        for (ArtistSimple artist : artists) {
            artistNames.add(artist.name);
        }

        return artistNames;
    }

    /**
     * Gets the name of the album the track is from.
     * @return album name
     */
    public String getAlbumName() {
        return track.album.name;
    }

    /**
     * Gets the cover art of the album in various sizes, widest first.
     * @return List of cover art in various sizes
     */
    public List<Image> getAlbumImages() {
        return track.album.images;
    }

    /**
     * Gets the unique ID of the track.
     * @return track ID
     */
    public String getID() {
        return track.id;
    }

    /**
     * Gets the URI link of the track.
     * @return URI
     */
    public String getURI() {
        return track.uri;
    }

    /**
     * Gets whether or not the track is labeled as explicit.
     * @return true if explicit, false if not
     */
    public boolean isExplicit() {
        return track.explicit;
    }

    /**
     * Gets whether or not the track is playable (i.e. available in the user's country).
     * @return true if playable, false if not
     */
    public boolean isPlayable() {
        return track.is_playable;
    }

    /** All of the methods below return the audio features of the track.
     *  See https://developer.spotify.com/documentation/web-api/reference/tracks/get-audio-features/
     */
    public float getAcousticness() {
        return audio.acousticness;
    }
    public float getDanceability() {
        return audio.danceability;
    }
    public int getDurationMs() {
        return audio.duration_ms;
    }
    public float getEnergy() {
        return audio.energy;
    }
    public float getInstrumentalness() {
        return audio.instrumentalness;
    }
    public int getKey() {
        return audio.key;
    }
    public float getLiveness() {
        return audio.liveness;
    }
    public float getLoudness() {
        return audio.loudness;
    }
    public float getSpeechiness() {
        return audio.speechiness;
    }
    public float getTempo() {
        return audio.tempo;
    }
    public int getTimeSignature() {
        return audio.time_signature;
    }
    public float getValence() {
        return audio.valence;
    }
}
