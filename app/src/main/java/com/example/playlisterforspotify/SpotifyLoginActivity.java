package com.example.playlisterforspotify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class SpotifyLoginActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 2;
    private static final String CLIENT_ID = "fc196430995343bab7b692075b6ab8f2";
    private static final String REDIRECT_URI = "playlisterforspotify://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_login);

        makeAuthRequest();
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

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            switch(response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    Intent intent = new Intent(this, LaunchActivity.class);
                    intent.putExtra("accessToken", response.getAccessToken());
                    startActivity(intent);
                    finish();
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.e("Spotify auth flow error", response.getError());
                    Toast.makeText(this, "An error has occurred. Please retry.", Toast.LENGTH_LONG).show();

                // Auth flow probably cancelled
                default:
                    Button spotifyLoginButton = findViewById(R.id.spotifyLogin);
                    spotifyLoginButton.setVisibility(View.VISIBLE);
                    spotifyLoginButton.setOnClickListener(unused -> {
                        Log.i("Spotify Log In Button", "Spotify log in button pressed");
                        makeAuthRequest();
                    });
            }
        }
    }

    /**
     * Makes the Spotify authorization request.
     */
    private void makeAuthRequest() {

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN,
                        REDIRECT_URI);
        // These "scopes" represent the permissions the user grants the app
        builder.setScopes(new String[]{"playlist-read-private", "playlist-modify-public",
                "playlist-modify-private"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }
}
