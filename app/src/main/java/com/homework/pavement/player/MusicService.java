package com.homework.pavement.player;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

//http://developer.android.com/intl/ko/guide/topics/media/mediaplayer.html
//Professional Android Application Development

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "MUSIC_PLAYER";
    private static final String ACTION_PLAY = "com.homework.pavement.musicplayer.action.PLAY";
    private static final String POSITION = "POSITION";
    private static final int ID_NOTIFICATION = 1004;
    private final IBinder binder = new MyBinder();
    private static NotificationCompat.Builder mBuilder;
    private MediaPlayer mMediaPlayer = null;
    private static Cursor mCursor;
    private String mArtistTitle;
    public String mArtist;
    private static int mPosition;
    public static boolean paused = false;

    //서비스가 시작되면 액티비티에서 전달한 노래 파일 정보를 이용해 MediaPlayer 가 음악 재생
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        mCursor = Utils.getTrackCursor(this);
        mPosition = intent.getIntExtra(POSITION, -1);
        mCursor.moveToPosition(mPosition);
        onStart();

        return super.onStartCommand(intent, flags, startId);
    }

    //재생이 모두 완료되면 미디어 플레이어 정지
    @Override
    public void onCompletion(MediaPlayer mp) {
        onStop();
    }

    //서비스가 종료될 때 MediaPlayer 객체의 자원 반환
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy Service");
        onStop();
        super.onDestroy();
    }

    //재생 요청에 대한 동작
    public void onStart() {
        String fileName = mCursor.getString(9);
        mArtistTitle = mCursor.getString(3) + " - " + mCursor.getString(4);
        mArtist = mCursor.getString(3);
        onStop();

        mMediaPlayer = MediaPlayer.create(this, Uri.parse("file://" + fileName));// initialize it here

        if(mMediaPlayer != null) {
            mMediaPlayer.start();

            if (mBuilder == null) {
                notifyNowPlaying(mArtistTitle);
            } else {
                mBuilder.setContentText(mArtistTitle);
                startForeground(ID_NOTIFICATION, mBuilder.build());
            }
            paused = false;
        }
    }

    //정지 요청에 대한 동작
    public void onStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    //재생 / 일시정지 전환 요청에 대한 동작
    public void onPlayPause() {
        if (!paused) {
            mMediaPlayer.pause();
            paused = true;
        } else {
            mMediaPlayer.start();
            paused = false;
        }
    }

    //이전 곡 요청에 대한 동작
    public void onPrev() {
        if (mPosition > 0) {
            mPosition--;
            mCursor.moveToPosition(mPosition);
            onStart();
        }
    }

    //다음 곡 요청에 대한 동작
    public void onNext() {
        if (mPosition < mCursor.getCount() - 1) {
            mPosition++;
            mCursor.moveToPosition(mPosition);
            onStart();
        }
    }

    //앨범아트를 가져옴
    public Bitmap getAlbumArt() {
        if (mCursor == null)
            mCursor = Utils.getTrackCursor(this);
        mCursor.moveToPosition(mPosition);
        return Utils.getAlbumArt(this, mCursor.getInt(1));
    }

    //현재 재생중인 노래의 가수와 제목 반환
    public String getArtistTitle() {
        return mArtistTitle;
    }

    //현재 재생하고 있는 노래에 대한 알림창을 활성화.
    public void notifyNowPlaying(String songName) {
        Intent upIntent = new Intent(this, PlayerActivity.class);
        PendingIntent pi = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(upIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_play_circle_outline_black_24dp)
                .setContentTitle("MusicPlayer")
                .setContentText(songName);
        mBuilder.setContentIntent(pi);
        mBuilder.setWhen(0);

        startForeground(ID_NOTIFICATION, mBuilder.build());
    }

    //본 서비스를 위해 재정의된 바인더 클래스
    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    //서비스 연결 요청
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind()");
        Log.i(TAG, intent + "");
    }

    //서비스 연결 해제 요청
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");

        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return true;
    }
}
