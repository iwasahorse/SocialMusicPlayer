package com.homework.pavement.player;

import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ArtistParser {private static final String ns = null;
    private List<Artist> artists = new ArrayList<>();
    public List<Artist> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, "UTF-8");
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List<Artist> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "lfm");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("artist")) {
                artists.add(readArtist(parser));
            } else {
                skip(parser);
            }
        }
        return artists;
    }

    public static class Artist {
        private final String name;
        private final String url;
        private final String image;

        private Artist(String name, String image, String url) {
            this.name = name;
            this.url = url;
            this.image = image;
        }
        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getImage() {
            return image;
        }
    }

    // Parses the contents of an entry. If it encounters a name, image, or url tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private Artist readArtist(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "artist");
        String name = null;
        String image = null;
        String url = null;
        String content;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals("name")) {
                name = readName(parser);
            } else if (tagName.equals("image") && parser.getAttributeValue(0).equals("mega")) {
                image = readImage(parser);
            } else if (tagName.equals("url")) {
                url = readLink(parser);
            } else if (tagName.equals("similar")) {
                content = readContent(parser);
                Log.i("CONTENT", content);
            }
            else {
                skip(parser);
            }
        }
        return new Artist(name, image, url);
    }

    // Processes name tags in the feed.
    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return name;
    }

    // Processes url tags in the feed.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "url");
        String url = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "url");
        return url;
    }

    // Processes image tags in the feed.
    private String readImage(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "image");
        String image = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "image");

        return image;
    }

    private String readContent(XmlPullParser parser) throws IOException, XmlPullParserException {
        String content ="";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equals("artist")) {
                artists.add(readArtist(parser));
                Log.i("ADD", artists.size() + "");
            }
            else {
                skip(parser);
            }
        }

        return content;
    }

    // For the tags name and image, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    @Override
    public String toString() {
        String result ="";
        for (Artist artist : artists) {
            result = result + artist.name + " " + artist.url+ " " + artist.image;
        }

        return result;
    }
}
