package com.homework.pavement.player;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ArtistInfoActivity extends AppCompatActivity {
    private static final String TAG = "SocialPlayer";
    private List<ArtistParser.Artist> artists;
    private String stringArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        stringArtist =  getIntent().getStringExtra("Artist");
        new GetInfoTask((ImageView) findViewById(R.id.artist_image))
                .execute();
    }

    private class GetInfoTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageArtist;

        public GetInfoTask(ImageView imageArtist) {
            this.imageArtist = imageArtist;
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmapArtist;
            InputStream streamImage;

            try {
                //String stringQuery = "http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&api_key=86d1159833df0c47af32383e37d33445&artist=";
                String stringQuery = "method=artist.getInfo&api_key=86d1159833df0c47af32383e37d33445&artist=" + stringArtist;
                URL url = new URI("http", "ws.audioscrobbler.com", "/2.0/", stringQuery, null).toURL();
                String result = getXmlFromNetwork(url);
                streamImage = new java.net.URL(artists.get(artists.size() - 1).getImage()).openStream();
                bitmapArtist = BitmapFactory.decodeStream(streamImage);
                streamImage.close();
                Log.i("RESULT", result);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getClass() + ": failed to get Artist Info");
                return null;
            }

            return bitmapArtist;
        }

        protected void onPostExecute(Bitmap result) {
            ListView listRelatedArtists = (ListView) findViewById(R.id.list_view);
            TextView textArtist = (TextView) findViewById(R.id.artist_text);
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ArtistInfoActivity.this, android.R.layout.simple_list_item_1, arrayList);

            if(result != null)
                imageArtist.setImageBitmap(result);
            else
                imageArtist.setImageResource(android.R.color.transparent);
            if( textArtist != null )
                textArtist.setText(stringArtist);
            for(ArtistParser.Artist artist: artists){
                if(!artist.getName().equals(stringArtist))
                    arrayAdapter.add(artist.getName());
                arrayAdapter.notifyDataSetChanged();
                if(listRelatedArtists != null)
                    listRelatedArtists.setAdapter(arrayAdapter);
            }
        }
    }

    private String getXmlFromNetwork(URL url) throws XmlPullParserException, IOException {
        InputStream stream;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        ArtistParser artistParser = new ArtistParser();
        HttpURLConnection connectionArtist;

        factory.setNamespaceAware(true);
        connectionArtist = getConnection(url);
        stream = connectionArtist.getInputStream();
        if(stream != null) {
            artists = artistParser.parse(stream);
            stream.close();
            connectionArtist.disconnect();
        }

        return artistParser.toString();
    }

    // Given a string representation of a URL, sets up a connection and gets
// an input stream.
    private HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(5000);    /* milliseconds */
        connection.setConnectTimeout(500 );    /* milliseconds */
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();
        return connection;
    }
}
