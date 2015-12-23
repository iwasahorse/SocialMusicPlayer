package com.homework.pavement.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "MUSIC_PLAYER";
    private Intent mIntentMusicService;
    private MusicService mMusicService;    // Reference to the service
    private ImageButton mButtonPlayPause;

    //서비스와 액티비티를 연결하는 매개
    private ServiceConnection mConnection = new ServiceConnection() {
        // 연결 됐을 때의 동작. 액티비티의 뷰가 그려진 후에 호출되기 때문에
        // onCreate()에서 mMusicService 객체를 참조하면 에러가 발생할 수 있다.
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mMusicService = ((MusicService.MyBinder) service).getService();
            textUpdate();
            albumArtUpdate();
        }

        // 서비스 연결이 종료 됐을 때
        public void onServiceDisconnected(ComponentName className) {
            mMusicService = null;
        }
    };

    //레이아웃 적용
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mButtonPlayPause = (ImageButton) findViewById(R.id.button_play_pause);
        mIntentMusicService = new Intent(PlayerActivity.this, MusicService.class);
    }

    //바인딩
    @Override
    protected void onResume() {
        super.onResume();
        setPlayPause();
        if (mMusicService == null)
            bindService(mIntentMusicService, mConnection, Context.BIND_AUTO_CREATE);

    }

    //액티비티가 종료되면 연결도 종료
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy PlayerActivity ");
        unbindService(mConnection);
    }

    //액션바에서 상위 액티비티로 이동하거나 정지 버튼을 눌렀을 때의 동작
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_stop:
                Log.i(TAG, "Stop");
                mMusicService.onStop();
                stopService(mIntentMusicService);
                finish();
                return true;


            case R.id.action_info:
                Log.i(TAG, "Info");
                Intent intent = new Intent(PlayerActivity.this, ArtistInfoActivity.class);
                intent.putExtra("Artist", mMusicService.mArtist);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //액션바에 들어갈 커스텀 메뉴를 적용
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_menu, menu);
        return true;
    }

    //디바이스의 백 버튼을 눌렀을 때 상위 액티비티로 이동
    @Override
    public void onBackPressed() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        TaskStackBuilder.create(this)
                // Add all of this activity's parents to the back stack
                .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent
                .startActivities();
    }

    // <<  버튼 클릭시 재생목록에서 현재 재생하고 있는 노래의 직전 아이템을 재생
    public void onClickPrevious(View view) {
        mMusicService.onPrev();
        textUpdate();
        setPlayPause();
        albumArtUpdate();
    }

    //재생 / 일시정지 버튼 클릭 시 재생 / 일시정지
    public void onClickPlayPause(View view) {
        mMusicService.onPlayPause();
        setPlayPause();
    }
    // >> 버튼 클릭 시 재생목록에서 현재 재생하고 있는 노래의 다음 아이템
    public void onClickNext(View view) {
        mMusicService.onNext();
        textUpdate();
        setPlayPause();
        albumArtUpdate();
    }

    //앨범 아트를 업데이트한다.
    public void albumArtUpdate() {
        ImageView imageView = (ImageView) findViewById(R.id.image_background);
        imageView.setImageDrawable(new BitmapDrawable(getResources(), mMusicService.getAlbumArt()));
    }

    //현재 재생중인 곡을 텍스뷰에 표시
    public void textUpdate() {
        TextView textArtistTitle = (TextView) findViewById(R.id.text_artist_title);
        textArtistTitle.setText(mMusicService.getArtistTitle());
    }

    //재생 / 일시정지 버튼을 눌렀을 때 UI 상의 변화
    public void setPlayPause() {
        if (!MusicService.paused)
            mButtonPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
        else
            mButtonPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
    }
}
