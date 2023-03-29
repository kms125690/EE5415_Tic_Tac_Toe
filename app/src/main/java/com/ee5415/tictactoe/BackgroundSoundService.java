package com.ee5415.tictactoe;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;

import com.ee5415.tictactoe.R;

public class BackgroundSoundService extends Service {
    MediaPlayer backgroundMusic;
    IBinder mBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public BackgroundSoundService getServerInstance() {
            return BackgroundSoundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mBinder = new LocalBinder();
        backgroundMusic = MediaPlayer.create(this, R.raw.dungeon);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Playing music in the Background", Toast.LENGTH_SHORT).show();
        return startId;
    }

    @Override
    public void onDestroy() {
        backgroundMusic.stop();
        backgroundMusic.release();
    }

    @Override
    public void onLowMemory() {
    }

    public void startMusic() throws IOException {
        backgroundMusic = MediaPlayer.create(this, R.raw.dungeon);
        backgroundMusic.setLooping(true); // Set looping
        backgroundMusic.setVolume(0.4f, 0.4f);
        backgroundMusic.seekTo(0);
//        backgroundMusic.prepare();
        backgroundMusic.start();
    }

    public void stopMusic() {
        backgroundMusic.pause();
//        backgroundMusic.reset();
    }
}
