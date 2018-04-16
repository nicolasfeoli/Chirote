package com.exampleprueba.nicolasfeoli.chirote;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "ceff243eb1d945738f53c1e317ea2a84";
    private static final String REDIRECT_URI = "yourcustomprotocol://callback";

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    private Player mPlayer;

    private boolean isPlay = false;

    private Button botonCargar;
    private TextView textViewCancion;
    private TextView textViewNombPlay;
    private TextView textViewAlbum;
    private TextView textViewArtist;
    private TextView textViewDuration;
    private TextView textViewPlayState;
    private EditText editTextURI;
    private ImageView imageViewURL;
    private TableLayout tableLayoutInformation;
    private LinearLayout linearLayoutBotones;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        botonCargar = findViewById(R.id.botonCargar);
        botonCargar.setOnClickListener(listenerCargar);

        ImageButton botonPlay = findViewById(R.id.botonPlay);
        botonPlay.setOnClickListener(listenerPlay);

        ImageButton botonNext = findViewById(R.id.botonNext);
        botonNext.setOnClickListener(listenerNext);

        ImageButton botonPrevious = findViewById(R.id.botonPrevious);
        botonPrevious.setOnClickListener(listenerPrevious);

        textViewCancion = findViewById(R.id.textViewCancion);
        textViewNombPlay = findViewById(R.id.textViewNombPlay);
        textViewAlbum = findViewById(R.id.textViewAlbum);
        textViewDuration = findViewById(R.id.textViewDuration);
        textViewArtist = findViewById(R.id.textViewArtist);
        textViewPlayState = findViewById(R.id.textViewPlayState);

        editTextURI = findViewById(R.id.editTextURI);

        imageViewURL = findViewById(R.id.imageViewURL);

        tableLayoutInformation = findViewById(R.id.tableLayoutInformation);
        linearLayoutBotones = findViewById(R.id.linearLayoutBotones);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Este método se debe llamar para que no se desperdicien recursos.
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            case kSpPlaybackNotifyMetadataChanged:
                textViewCancion.setText(mPlayer.getMetadata().currentTrack.name);
                textViewAlbum.setText(mPlayer.getMetadata().currentTrack.albumName);
                textViewNombPlay.setText(mPlayer.getMetadata().contextName);
                textViewArtist.setText(mPlayer.getMetadata().currentTrack.artistName);
                long milliSeconds = mPlayer.getMetadata().currentTrack.durationMs;
                textViewDuration.setText(String.format("%02d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
                                        TimeUnit.MILLISECONDS.toSeconds(milliSeconds) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds))));
                try {
                    URL url = new URL(mPlayer.getMetadata().currentTrack.albumCoverWebUrl);
                    DescargarImagen di = new DescargarImagen();
                    imageViewURL.setImageBitmap(di.execute(url).get());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                //Los métodos de abajo no están implementados en el SDK aún.
                //textViewNext.setText(mPlayer.getMetadata().nextTrack.name);
                //textViewPrev.setText(mPlayer.getMetadata().prevTrack.name);
                break;
            case UNKNOWN:
                break;
            case kSpPlaybackNotifyPlay:
                textViewPlayState.setText("Reproduciendo...");
                break;
            case kSpPlaybackNotifyPause:
                textViewPlayState.setText("Pausa");
            case kSpPlaybackNotifyTrackChanged:
                break;
            case kSpPlaybackNotifyNext:
                break;
            case kSpPlaybackNotifyPrev:
                break;
            case kSpPlaybackNotifyShuffleOn:
                break;
            case kSpPlaybackNotifyShuffleOff:
                break;
            case kSpPlaybackNotifyRepeatOn:
                break;
            case kSpPlaybackNotifyRepeatOff:
                break;
            case kSpPlaybackNotifyBecameActive:
                break;
            case kSpPlaybackNotifyBecameInactive:
                break;
            case kSpPlaybackNotifyLostPermission:
                break;
            case kSpPlaybackEventAudioFlush:
                break;
            case kSpPlaybackNotifyAudioDeliveryDone:
                break;
            case kSpPlaybackNotifyContextChanged:
                break;
            case kSpPlaybackNotifyTrackDelivered:
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            case UNKNOWN:
                break;
            case kSpErrorOk:
                break;
            case kSpErrorFailed:
                break;
            case kSpErrorInitFailed:
                break;
            case kSpErrorWrongAPIVersion:
                break;
            case kSpErrorNullArgument:
                break;
            case kSpErrorInvalidArgument:
                break;
            case kSpErrorUninitialized:
                break;
            case kSpErrorAlreadyInitialized:
                break;
            case kSpErrorLoginBadCredentials:
                break;
            case kSpErrorNeedsPremium:
                break;
            case kSpErrorTravelRestriction:
                break;
            case kSpErrorApplicationBanned:
                break;
            case kSpErrorGeneralLoginError:
                break;
            case kSpErrorUnsupported:
                break;
            case kSpErrorNotActiveDevice:
                break;
            case kSpErrorAPIRateLimited:
                break;
            case kSpErrorPlaybackErrorStart:
                break;
            case kSpErrorGeneralPlaybackError:
                break;
            case kSpErrorPlaybackRateLimited:
                break;
            case kSpErrorPlaybackCappingLimitReached:
                break;
            case kSpErrorAdIsPlaying:
                break;
            case kSpErrorCorruptTrack:
                break;
            case kSpErrorContextFailed:
                break;
            case kSpErrorPrefetchItemUnavailable:
                break;
            case kSpAlreadyPrefetching:
                break;
            case kSpStorageReadError:
                break;
            case kSpStorageWriteError:
                break;
            case kSpPrefetchDownloadFailed:
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
        //mPlayer.playUri(null, "spotify:track:20JyLsaGV5UJ0RLXDL4KXG", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error i) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    View.OnClickListener listenerCargar = new View.OnClickListener() {
        public void onClick(View v) {
            botonCargar.setEnabled(false);
            String uri = editTextURI.getText().toString();
            mPlayer.playUri(null, uri, 0, 0);
            tableLayoutInformation.setVisibility(View.VISIBLE);
            textViewPlayState.setVisibility(View.VISIBLE);
            linearLayoutBotones.setVisibility(View.VISIBLE);
            //textViewCancion.setText(mPlayer.getMetadata().currentTrack.name);
        }
    };


    View.OnClickListener listenerPlay = new View.OnClickListener() {
        public void onClick(View v) {
            if(!mPlayer.getPlaybackState().isPlaying) {
                mPlayer.resume(null);
                //botonPlay.setImageResource();
            }
            else {
                mPlayer.pause(null);
            }
        }
    };

    View.OnClickListener listenerNext = new View.OnClickListener () {
        public void onClick(View v) {
            mPlayer.skipToNext(null);
        }
    };

    View.OnClickListener listenerPrevious = new View.OnClickListener () {
        public void onClick(View v) {
            mPlayer.skipToPrevious(null);
        }
    };

    private class DescargarImagen extends AsyncTask<URL, Void, Bitmap> {
        protected Bitmap doInBackground(URL... urls) {
            Bitmap bmp = null;
            try {
                bmp = BitmapFactory.decodeStream(urls[0].openConnection().getInputStream());
            } catch (IOException e) {
                Log.d("MainActivity","Failed to download album image.");
                e.printStackTrace();
            }
            return bmp;
        }
        /*
        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }
        */
        protected void onPostExecute(Long result) {
            //showDialog("Downloaded " + result + " bytes");
        }
    }
}


