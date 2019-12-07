package com.example.playlisterforspotify;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class LaunchActivity extends AppCompatActivity {
    private static String accessToken;
    private static String myID;

    private static class GetNewUserData extends AsyncTask<Void, Void, List<String>> {
        @Override
        public List<String> doInBackground(Void ... Voids) {
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(accessToken);
            SpotifyService spotify = api.getService();

            myID = spotify.getMe().id;

            List<String> ownedPlaylistIDs = new ArrayList<>();
            List<PlaylistSimple> allMyPlaylists = spotify.getMyPlaylists().items;
            // We only care about the playlists actually owned by the user, not the playlists the user follows.
            for (PlaylistSimple playlist : allMyPlaylists) {
                String playlistOwnerID = playlist.owner.id;
                if (playlistOwnerID.equals(myID)) {
                    ownedPlaylistIDs.add(playlist.id);
                }
            }

            return ownedPlaylistIDs;
        }

        @Override
        public void onPostExecute(List<String> playlistIDs) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
            mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(myID);
        }
    }

    /**Request code.*/
    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        FirebaseAuth.getInstance().signOut();
        accessToken = getIntent().getStringExtra("accessToken");

        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(unused -> {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build()
            );
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
            finish();
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("accessToken", accessToken);
            startActivity(intent);
            finish();
        } else {
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build()
            );

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    /**
     * Runs when request is completed.
     * @param requestCode The code of the request that was send out.
     * @param resultCode the result as a coded integer value.
     * @param data extra data.
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                if (response.isNewUser()) {
                    new GetNewUserData().execute();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("accessToken", accessToken);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("accessToken", accessToken);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }
}
