package com.homework.pavement.player;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ArtistInfoActivity extends AppCompatActivity {
    private static final String TAG = "SocialPlayer";
    private List<ArtistParser.Artist> artists;
    private String stringArtist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        stringArtist =  getIntent().getStringExtra("Artist");
        new DownloadImageTask((ImageView) findViewById(R.id.artist_image))
                .execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Uploads XML from stackoverflow.com, parses it, and combines it with
// HTML markup. Returns HTML string.
    private String loadXmlFromNetwork(URL url) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        ArtistParser artistParser = new ArtistParser();

        try {
            stream = downloadUrl(url);
            if(stream != null)
                artists = artistParser.parse(stream);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return artistParser.toString();
    }

    // Given a string representation of a URL, sets up a connection and gets
// an input stream.
    private InputStream downloadUrl(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }


        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmapIcon;
            InputStream streamImage;

            try {
                //String stringURL = "http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&api_key=86d1159833df0c47af32383e37d33445&artist=";
                String stringQuery = "method=artist.getInfo&api_key=86d1159833df0c47af32383e37d33445&artist=" + stringArtist;
                URL url = new URI("http", "ws.audioscrobbler.com", "/2.0/", stringQuery, null).toURL();
                String result = loadXmlFromNetwork(url);
                Log.i("RESULT", result);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Log.e(TAG, "URISyntaxException: loadXmlFromNetwork");
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                Log.e(TAG, "XmlPullParserException: loadXmlFromNetwork");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException: loadXmlFromNetwork");
            }

            try {
                streamImage = new java.net.URL(artists.get(artists.size() - 1).getImage()).openStream();
                bitmapIcon = BitmapFactory.decodeStream(streamImage);
                streamImage.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException: failed to get Bitmap Icon");
                return null;
            }
            return bitmapIcon;
        }

        protected void onPostExecute(Bitmap result) {
            if(result != null)
                bmImage.setImageBitmap(result);
            else
                bmImage.setImageResource(android.R.color.transparent);
            TextView textArtist = (TextView) findViewById(R.id.artist_text);
            if( textArtist != null )
                textArtist.setText(stringArtist);
            ListView listRelatedArtists = (ListView) findViewById(R.id.list_view);
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ArtistInfoActivity.this, android.R.layout.simple_list_item_1, arrayList);
            for(ArtistParser.Artist artist: artists){
                if(!artist.getName().equals(stringArtist))
                    arrayAdapter.add(artist.getName());
                arrayAdapter.notifyDataSetChanged();
                if(listRelatedArtists != null)
                    listRelatedArtists.setAdapter(arrayAdapter);
            }
        }
    }
}
