package com.example.playlisterforspotify;

public class StoredPlaylist {
    private String playlistID;
    private String firebaseUID;
    private String firebaseScreenname;
    private String spotifyUID;
    private int score;

    public StoredPlaylist(String pID, String firebaseID, String screenname, String spotifyID, int rating) {
        playlistID = pID;
        firebaseUID = firebaseID;
        firebaseScreenname = screenname;
        spotifyUID = spotifyID;
        score = rating;
    }

    public String getPlaylistID() {return playlistID;}

    public String getFirebaseUID() {
        return firebaseUID;
    }

    public String getSceenname() {return firebaseScreenname;}

    public String getSpotifyUID() {
        return spotifyUID;
    }

    public int getScore() {return score;}
}
