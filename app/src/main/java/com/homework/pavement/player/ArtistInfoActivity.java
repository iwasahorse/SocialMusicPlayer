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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ArtistInfoActivity extends AppCompatActivity {
    private List<ArtistParser.Artist> artists;
    private String artistText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        artistText =  getIntent().getStringExtra("Artist");
        new DownloadImageTask((ImageView) findViewById(R.id.artist_image))
                .execute();

    }

    // Uploads XML from stackoverflow.com, parses it, and combines it with
// HTML markup. Returns HTML string.
    private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        ArtistParser artistParser = new ArtistParser();

        try {
            stream = downloadUrl(urlString);
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
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
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
            Bitmap mIcon11 = null;

            try {
                String url = "http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&api_key=86d1159833df0c47af32383e37d33445&artist=";
                url += artistText;
                String result = loadXmlFromNetwork(url);
                Log.i("RESULT", result);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Test", "IOException");
            }

            try {
                InputStream in = new java.net.URL(artists.get(artists.size() - 1).image).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            ( (TextView) findViewById(R.id.artist_text) ).setText(artistText);
            ListView listView = (ListView) findViewById(R.id.list_view);
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ArtistInfoActivity.this, android.R.layout.simple_list_item_1, arrayList);
            for(ArtistParser.Artist artist: artists){
                if(!artist.name.equals(artistText))
                    arrayAdapter.add(artist.name);
                arrayAdapter.notifyDataSetChanged();
                listView.setAdapter(arrayAdapter);
            }
        }
    }
}
