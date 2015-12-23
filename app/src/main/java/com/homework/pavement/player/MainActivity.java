package com.homework.pavement.player;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private Intent mIntentMusicService;
    private Intent mIntentPlayerActivity;
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

        ListView listView = (ListView) findViewById(R.id.list);
        ArrayList<String> arrayList = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);

        mIntentPlayerActivity = new Intent(MainActivity.this, PlayerActivity.class);
        mIntentMusicService = new Intent(MainActivity.this, MusicService.class);

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(onClickListItem);

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
}

