package ronak.com.spotify.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class SpotifyContract {

  public static final String CONTENT_AUTHORITY = "ronak.com.spotify";

  public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

  public static final String PATH_SEARCH_ARTIST = "search_artist";
  public static final String PATH_TOP_TRACK = "top_track";

  public static final class SearchArtistEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
      BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_ARTIST).build();

    public static final String CONTENT_TYPE =
      ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEARCH_ARTIST;

    public static final String CONTENT_ITEM_TYPE =
      ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEARCH_ARTIST;

    public static final String TABLE_NAME = "search_artist";

    public static final String COLUMN_ARTIST_NAME = "artist_name";
    public static final String COLUMN_ARTIST_IMAGE_URL = "artist_image_url";
    public static final String COLUMN_ARTIST_ID = "artist_id";

    public static Uri buildSearchArtistUri(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }

  }

  public static final class TopTrackEntry implements BaseColumns {

    public static final Uri CONTENT_URI =
      BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOP_TRACK).build();

    public static final String CONTENT_TYPE =
      ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOP_TRACK;

    public static final String CONTENT_ITEM_TYPE =
      ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOP_TRACK;

    public static final String TABLE_NAME = "top_track";

    public static final String COLUMN_TRACK_NAME = "track_name";
    public static final String COLUMN_ALBUM_NAME = "album_name";
    public static final String COLUMN_TRACK_IMAGE_URL = "track_image_url";
    public static final String COLUMN_TRACK_ID = "track_id";

    public static Uri buildTopTrackUri(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }

  }

}
