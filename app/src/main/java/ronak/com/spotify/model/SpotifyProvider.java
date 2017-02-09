package ronak.com.spotify.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class SpotifyProvider extends ContentProvider {

  public static final UriMatcher sUriMatcher = buildUriMatcher();
  private SpotifyDbHelper mOpenHelper;

  public static final int SEARCH_ARIST = 100;
  public static final int TOP_TRACKS = 101;

  static UriMatcher buildUriMatcher() {

    final UriMatcher matcher  = new UriMatcher(UriMatcher.NO_MATCH);
    final String authority = SpotifyContract.CONTENT_AUTHORITY;

    matcher.addURI(authority, SpotifyContract.PATH_SEARCH_ARTIST, SEARCH_ARIST);
    matcher.addURI(authority, SpotifyContract.PATH_TOP_TRACK, TOP_TRACKS);

    return matcher;

  }

  @Override
  public boolean onCreate() {
    mOpenHelper = new SpotifyDbHelper(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

    Cursor retCursor;
    switch (sUriMatcher.match(uri)) {
      case SEARCH_ARIST:
      {
        retCursor = mOpenHelper.getReadableDatabase().query(
          SpotifyContract.SearchArtistEntry.TABLE_NAME,
          projection,
          selection,
          selectionArgs,
          null,
          null,
          sortOrder
        );
        break;
      }
      case TOP_TRACKS:
      {
        retCursor = mOpenHelper.getReadableDatabase().query(
          SpotifyContract.TopTrackEntry.TABLE_NAME,
          projection,
          selection,
          selectionArgs,
          null,
          null,
          sortOrder
        );
        break;
      }
      default:
        throw new UnsupportedOperationException("Unkown uri: " + uri );
    }
    retCursor.setNotificationUri(getContext().getContentResolver(), uri);
    return retCursor;

  }

  @Override
  public String getType(Uri uri) {
    final int match = sUriMatcher.match(uri);

    switch (match) {
      case SEARCH_ARIST:
        return SpotifyContract.SearchArtistEntry.CONTENT_TYPE;
      case TOP_TRACKS:
        return SpotifyContract.TopTrackEntry.CONTENT_TYPE;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    Uri returnUri;

    switch (match) {
      case SEARCH_ARIST:
      {
        long _id = db.insert(SpotifyContract.SearchArtistEntry.TABLE_NAME, null, values);
        if ( _id > 0 ) {
          returnUri = SpotifyContract.SearchArtistEntry.buildSearchArtistUri(_id);
        }
        else {
          throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        break;
      }
      case TOP_TRACKS:
        long _id = db.insert(SpotifyContract.TopTrackEntry.TABLE_NAME, null, values);
        if ( _id > 0 ) {
          returnUri = SpotifyContract.TopTrackEntry.buildTopTrackUri(_id);
        }
        else {
          throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        break;
      default:
        throw new UnsupportedOperationException("Unkown uri: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return returnUri;

  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int rowsDeleted;

    if (null == selection) selection = "1";
    switch (match) {
      case SEARCH_ARIST:
        rowsDeleted = db.delete(SpotifyContract.SearchArtistEntry.TABLE_NAME, selection, selectionArgs);
        break;
      case TOP_TRACKS:
        rowsDeleted = db.delete(SpotifyContract.TopTrackEntry.TABLE_NAME, selection, selectionArgs);
        break;
      default:
        throw new UnsupportedOperationException("Unkown uri: " + uri);
    }

    if (rowsDeleted != 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }

    return rowsDeleted;

  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int rowsUpdated;

    switch (match) {
      case SEARCH_ARIST:
        rowsUpdated = db.update(SpotifyContract.SearchArtistEntry.TABLE_NAME, values, selection, selectionArgs);
        break;
      case TOP_TRACKS:
        rowsUpdated = db.update(SpotifyContract.TopTrackEntry.TABLE_NAME, values, selection, selectionArgs);
        break;
      default:
        throw new UnsupportedOperationException("Unkown uri: " + uri);
    }

    if (rowsUpdated != 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }

    return rowsUpdated;

  }

  @Override
  public int bulkInsert(Uri uri, ContentValues[] values) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    switch (match) {
      case SEARCH_ARIST:
        db.beginTransaction();
        int returnCount = 0;
        try {
          for (ContentValues value : values ) {
            long _id = db.insert(SpotifyContract.SearchArtistEntry.TABLE_NAME, null, value);
            if (_id != -1) {
              returnCount++;
            }
          }
          db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
      case TOP_TRACKS:
        db.beginTransaction();
        returnCount = 0;
        try {
          for (ContentValues value : values) {
            long _id = db.insert(SpotifyContract.TopTrackEntry.TABLE_NAME, null, value);
            if (_id != -1) {
              returnCount++;
            }
          }
          db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
      default:
        return super.bulkInsert(uri, values);

    }
  }

}
