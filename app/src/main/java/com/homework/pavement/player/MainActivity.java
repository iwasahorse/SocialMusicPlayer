package com.homework.pavement.player;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

//http://developer.android.com/intl/ko/guide/topics/ui/notifiers/notifications.html

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MUSIC_PLAYER";
    private static final String ACTION_PLAY = "com.homework.pavement.musicplayer.action.PLAY";
    private static final String POSITION = "POSITION";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    private Intent mIntentMusicService;
    private Intent mIntentPlayerActivity;
    private ListView listView;
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> arrayAdapter;
    private Cursor mCursor;
    private int prevPosition = -1;

    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

        //리스트뷰의 아이템이 클릭됐을 때 뮤직서비스를 시작하면서 플레이어 액티비티를 실행
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (prevPosition != position) {

                prevPosition = position;
                mIntentMusicService.setAction(ACTION_PLAY);
                mIntentMusicService.putExtra(POSITION, position);
                startService(mIntentMusicService);
                startActivity(mIntentPlayerActivity);
            } else {
                startActivity(mIntentPlayerActivity);
            }
        }
    };

    //레이아웃 적용, 탐색한 음악에 대한 정보들 리스트뷰에 추가
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else
            init();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    //커서를 만들 때 사용한 자원은 액티비티 종료될 때 반환
    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy MainActivity");
        super.onDestroy();
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    init();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    finish();
                }
                //return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void init(){

        listView = (ListView) findViewById(R.id.list_music);
        arrayList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);

        mIntentPlayerActivity = new Intent(MainActivity.this, PlayerActivity.class);
        mIntentMusicService = new Intent(MainActivity.this, MusicService.class);

        if(listView != null) {
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(onClickListItem);
        }

        mCursor = Utils.getTrackCursor(this);
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                String textArtistTitle = mCursor.getString(3) + " - " + mCursor.getString(4);
                arrayList.add(textArtistTitle);
            }
            mCursor.close();
            mCursor = null;
        }
    }

}

