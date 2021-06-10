package com.geeks.spotifyconnection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class MainActivity extends AppCompatActivity {
    private static final String REDIRECT_URI = "my-awesome-app-login://callback";
    private static final String CLIENT_ID = "4828bb77a76a498bb9268b7d71cc296c";
    private static final int REQUEST_CODE = 1337;
    private SpotifyAppRemote mSpotifyAppRemote;
    public static String BASE_IMG_URL="https://i.scdn.co/image/";
    ImageView trackImage;
    TextView txtTrackName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        trackImage=findViewById(R.id.imgTrackCover);
        txtTrackName=findViewById(R.id.txtTrackName);
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if result comes from the correct activity
        Log.e("MainActivity", "onActivityResult");

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    ConnectionParams connectionParams =
                            new ConnectionParams.Builder(CLIENT_ID)
                                    .setRedirectUri(REDIRECT_URI)
                                    .showAuthView(true)
                                    .build();
                    SpotifyAppRemote.connect(this, connectionParams, new Connector.ConnectionListener() {
                                @Override
                                public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                                    Log.e("MainActivity", "Connected");
                                    mSpotifyAppRemote = spotifyAppRemote;
                                    connected();
                                }
                                @Override
                                public void onFailure(Throwable error) {
                                    Log.e("MainActivity", error.getMessage().toString());

                                }
                            }
                    );

                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.e("MainActivity", "Error response");
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.e("MainActivity", "");

                    // Handle other cases
            }
        }
    }

    private void connected() {
        mSpotifyAppRemote.getPlayerApi().play("spotify:album:4l1MLKyDun3edi5lrDwtZG?si=e6n6W-0FREGxDRlpw9");
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        String imageId=track.imageUri.raw;
                        imageId=imageId.replace("spotify:image:","");
                      String url= BASE_IMG_URL+imageId;
                        Glide.with(this).load(url).into(trackImage);
                        txtTrackName.setText(track.name);
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });
    }

}