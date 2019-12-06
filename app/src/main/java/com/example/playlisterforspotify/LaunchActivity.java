package com.example.playlisterforspotify;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class LaunchActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private String accessToken;

    private class getUserData extends AsyncTask<Void, Void, String> {
        @Override
        public String doInBackground(Void ... Voids) {
            accessToken = getIntent().getStringExtra("accessToken");

        }
    }

    /**Request code.*/
    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

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
            Intent intent = new Intent(this, SpotifyLoginActivity.class);
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
            //if (resultCode == RESULT_FIRST_USER) {
//                mDatabase = FirebaseDatabase.getInstance().getReference();
//                mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("playlists").setValue(
//                        //TODO: JSON OBJECT THAT HOLDS DATA
//
//                )
            //} else
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, SpotifyLoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
