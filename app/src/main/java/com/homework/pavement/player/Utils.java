package com.homework.pavement.player;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class Utils {

    //  http://stackoverflow.com/questions/22837474/get-all-tracks-from-mediastore
    //URI와 커서를 이용해 음악 파일에 대한 가상의 테이블을 반환. 커서는 그 테이블을 row 단위로 탐색
    public static Cursor getTrackCursor(Context context) {
        final String _ID = MediaStore.Audio.Media._ID; //0  getString
        final String albumID = MediaStore.Audio.Media.ALBUM_ID; //0  getString
        final String track = MediaStore.Audio.Media.TRACK;   //2
        final String artist = MediaStore.Audio.Media.ARTIST;    //3
        final String title = MediaStore.Audio.Media.TITLE; //4
        final String album = MediaStore.Audio.Media.ALBUM;  //5
        final String duration = MediaStore.Audio.Media.DURATION;    //6
        final String year = MediaStore.Audio.Media.YEAR;    //7
        final String composer = MediaStore.Audio.Media.COMPOSER;    //8
        final String data = MediaStore.Audio.Media.DATA;    //9
        final String[] columns = {_ID, albumID, track, artist, title, album, duration, year, composer, data};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        ContentResolver cr = context.getContentResolver();

        return cr.query(uri, columns, null, null, null);
    }

    //컨텐츠 폴더의 URI를 이용해 앨범아트를 비트맵 객체로 생성
    public static Bitmap getAlbumArt(Context context, int albumID) {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, albumID);
        ContentResolver cr = context.getContentResolver();
        InputStream in = null;
        try {
            in = cr.openInputStream(uri);
            return BitmapFactory.decodeStream(in);
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            Log.i("WARNING", "album cover is null");
            return null;
        }
    }
}
