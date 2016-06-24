package com.homework.pavement.player;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.IOException;

public class TestActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    private Cursor cursor;
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final String track_id = MediaStore.Audio.Media._ID; //0
        final String track_no = MediaStore.Audio.Media.TRACK;   //1
        final String artist = MediaStore.Audio.Media.ARTIST;    //2
        final String track_name = MediaStore.Audio.Media.TITLE; //3
        final String album = MediaStore.Audio.Media.ALBUM;  //4
        final String duration = MediaStore.Audio.Media.DURATION;    //5
        final String year = MediaStore.Audio.Media.YEAR;    //6
        final String composer = MediaStore.Audio.Media.COMPOSER;    //7
        final String path = MediaStore.Audio.Media.DATA;    //8
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        ContentResolver cr = getBaseContext().getContentResolver();
        final String[] columns = {track_id, track_no, artist, track_name, album, duration, year, composer, path};

        cursor = cr.query(uri, columns, null, null, null);
        if (cursor != null) {
            cursor.moveToPosition(4);
        }
    }

    public void onClickTest(View view) {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;

            } else {
//                mMediaPlayer = MediaPlayer.create(this, Uri.parse("file://" + cursor.getString(8)));// initialize it here
//                mMediaPlayer.start();
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnPreparedListener(onPreparedListener);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource("file://" + cursor.getString(8));
                mMediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close();
    }
}

