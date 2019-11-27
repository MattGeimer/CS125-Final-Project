package com.example.playlisterforspotify;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;

/** Stores and returns information about any playlist given the user's ID and the playlist's ID. */
public class PlaylistInfo {
    /** The current playlist. */
    private Playlist playlist;
    /** A List of PlaylistTracks representing all of the tracks of the playlist. */
    private List<PlaylistTrack> tracks;
    /** If any of the songs in the playlist are explicit, the playlist is labeled as explicit. */
    private boolean isExplicit;
    /** A confidence measure from 0.0 to 1.0 of whether the track is acoustic. */
    private float acousticness;
    /** A value of 0.0 is least danceable and 1.0 is most danceable. */
    private float danceability;
    /** Energy is a measure from 0.0 to 1.0 and represents a perceptual measure of intensity and activity. */
    private float energy;
    /** Predicts whether a track contains no vocals. */
    private float instrumentalness;
    /** Detects the presence of an audience in the recording. */
    private float liveness;
    /** The overall loudness of a track in decibels (dB).*/
    private float loudness;
    /** Detects the presence of spoken words in a track.*/
    private float speechiness;
    /** The overall estimated tempo of a track in beats per minute (BPM)*/
    private float tempo;
    /** A measure from 0.0 to 1.0 describing the musical positiveness conveyed by a track.*/
    private float valence;
    /** The duration of the track in milliseconds. */
    private int durationMs;
    /** The estimated overall key of the track. 0 = C, 1 = C♯/D♭, 2 = D, ..., 11 = B♮*/
    private int key;
    /** An estimated overall time signature of a track. */
    private int timeSignature;

    /**
     * Creates a new PlaylistInfo to store relevant information about a Spotify playlist.
     *
     * @param userID the user's unique ID
     * @param playlistID the playlist's unique ID
     */
    PlaylistInfo(String userID, String playlistID) {
        SpotifyService spotify = new SpotifyApi().getService();
        playlist = spotify.getPlaylist(userID, playlistID);
        tracks = playlist.tracks.items;

        int numTracks = tracks.size();
        for (int i = 0; i < numTracks; i++) {
            String trackID = tracks.get(i).track.id;

            // If the trackID is null that means it is a local file and track analysis cannot be obtained.
            if (trackID == null) {
                continue;
            }

            TrackInfo track = new TrackInfo(trackID);

            if (track.isExplicit()) {
                isExplicit = true;
            }

            acousticness += track.getAcousticness();
            danceability += track.getDanceability();
            energy += track.getEnergy();
            instrumentalness += track.getInstrumentalness();
            liveness += track.getLiveness();
            loudness += track.getLoudness();
            speechiness += track.getSpeechiness();
            tempo += track.getTempo();
            valence += track.getValence();
            durationMs += track.getDurationMs();
            key += track.getKey();
            timeSignature += track.getTimeSignature();
        }
        if (numTracks > 1) {
            acousticness /= numTracks;
            danceability /= numTracks;
            energy /= numTracks;
            instrumentalness /= numTracks;
            liveness /= numTracks;
            loudness /= numTracks;
            speechiness /= numTracks;
            tempo /= numTracks;
            valence /= numTracks;
            durationMs = Math.round(durationMs / numTracks);
            key = Math.round(key / numTracks);
            timeSignature = Math.round(timeSignature / numTracks);
        }
    }

    /**
     * Gets the name of the playlist.
     * @return name of the playlist
     */
    public String getName() {
        return playlist.name;
    }

    /**
     * Gets a List of IDs of all of the tracks of the playlist.
     * @return list of track IDs
     */
    public List<String> getTracks() {
        List<String> trackIDs = new ArrayList<>(tracks.size());

        for (PlaylistTrack track : tracks) {
            trackIDs.add(track.track.id);
        }

        return trackIDs;
    }

    /**
     * Gets the playlist description.
     * @return playlist description
     */
    public String getDescription() {
        return playlist.description;
    }

    /**
     * Gets the unique ID of the playlist.
     * @return playlist ID
     */
    public String getID() {
        return playlist.id;
    }

    /**
     * Gets the unique ID of the user who made the playlist
     * @return owner's ID
     */
    public String getUserID() {
        return playlist.owner.id;
    }

    /**
     * Gets the URI link of the playlist
     * @return URI
     */
    public String getURI() {
        return playlist.uri;
    }

    /**
     * Gets whether or not the playlist is listed as public on Spotify
     * @return true if public, false if private
     */
    public boolean isPublic() {
        return playlist.is_public;
    }

    /**
     * Gets whether or not any of the songs on the playlist are explicit
     * @return true if explicit, false if not
     */
    public boolean isExplicit() {
        return isExplicit;
    }
    /**
     * A playlist can have either zero, one, or four different images.
     * <p>
     *     If the playlist is empty, it will have no image.
     *
     *     If the playlist has songs from less than four different albums,
     *     the image will be the album cover of the first song.
     *
     *     If the playlist has songs from four or more different albums,
     *     the playlist's image will be a collage from the first four albums (separated by the List).
     *
     *     The user can also set his/her own image.
     * </p>
     * @return a List of all of the Images associated with this playlist
     */
    public List<Image> getImages() {
        return playlist.images;
    }

    /** All of the methods below return the average audio features for all of the songs in the playlist. */
    public float getAcousticness() {
        return acousticness;
    }
    public float getDanceability() {
        return danceability;
    }
    public float getEnergy() {
        return energy;
    }
    public float getInstrumentalness() {
        return instrumentalness;
    }
    public float getLiveness() {
        return liveness;
    }
    public float getLoudness() {
        return loudness;
    }
    public float getSpeechiness() {
        return speechiness;
    }
    public float getTempo() {
        return tempo;
    }
    public float getValence() {
        return valence;
    }
    public int getDurationMs() {
        return durationMs;
    }
    public int getKey() {
        return key;
    }
    public int getTimeSignature() {
        return timeSignature;
    }
}
