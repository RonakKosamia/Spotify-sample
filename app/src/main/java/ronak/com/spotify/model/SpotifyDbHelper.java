package ronak.com.spotify.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SpotifyDbHelper extends SQLiteOpenHelper {

  private static final int DATABASE_VERSION = 2;

  static final String DATABASE_NAME = "spotify.db";

  public SpotifyDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {

    final String SQL_CREATE_SEARCH_ARTIST_TABLE = "CREATE TABLE " + SpotifyContract.SearchArtistEntry.TABLE_NAME + " (" +
      SpotifyContract.SearchArtistEntry._ID + " INTEGER PRIMARY KEY," +
      SpotifyContract.SearchArtistEntry.COLUMN_ARTIST_NAME + " TEXT NOT NULL," +
      SpotifyContract.SearchArtistEntry.COLUMN_ARTIST_IMAGE_URL + " TEXT NOT NULL," +
      SpotifyContract.SearchArtistEntry.COLUMN_ARTIST_ID + " TEXT NOT NULL" +
      ");";

    final String SQL_CREATE_TOP_TRACK_TABLE = "CREATE TABLE " + SpotifyContract.TopTrackEntry.TABLE_NAME + " (" +
      SpotifyContract.TopTrackEntry._ID + " INTEGER PRIMARY KEY," +
      SpotifyContract.TopTrackEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL," +
      SpotifyContract.TopTrackEntry.COLUMN_ALBUM_NAME + " TEXT NOT NULL," +
      SpotifyContract.TopTrackEntry.COLUMN_TRACK_IMAGE_URL + " TEXT NOT NULL," +
      SpotifyContract.TopTrackEntry.COLUMN_TRACK_ID + " TEXT NOT NULL" +
      ");";

    db.execSQL(SQL_CREATE_SEARCH_ARTIST_TABLE);
    db.execSQL(SQL_CREATE_TOP_TRACK_TABLE);

  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }
}
