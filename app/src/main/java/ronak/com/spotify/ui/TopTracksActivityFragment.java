package ronak.com.spotify.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import ronak.com.spotify.R;
import ronak.com.spotify.adapters.TopTracksAdapter;
import ronak.com.spotify.model.SpotifyContract;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {
    //Global vars
    private ListView mTopTracksListView;
    String artistId;
    String artistName;
    final Uri topTrackUri = SpotifyContract.BASE_CONTENT_URI.buildUpon().appendPath(SpotifyContract.PATH_TOP_TRACK).build();
    android.support.v7.app.ActionBar mActionBar;
    TopTracksAdapter mTopTracksAdapter;

    public TopTracksActivityFragment() {
    }



  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //create views
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        mTopTracksListView = (ListView) rootView.findViewById(R.id.topTracksListView);

        mTopTracksAdapter = new TopTracksAdapter(getActivity(), null, 0, getActivity().getFragmentManager());

        Intent intent = getActivity().getIntent();
        Bundle arguments = getArguments();
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setTitle("Top 10 Tracks");
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
          artistId = intent.getStringExtra(Intent.EXTRA_TEXT);
          artistName = intent.getStringExtra("artist_name");
          mActionBar.setSubtitle(artistName);
          FetchTopTrackTask fetchTopTrackTask = new FetchTopTrackTask(getActivity());
          fetchTopTrackTask.execute(artistId);
        }
        else if(arguments != null) {
          artistId = arguments.getString("artist_id");
          artistName = arguments.getString("artist_name");
          mActionBar.setSubtitle(artistName);
          FetchTopTrackTask fetchTopTrackTask = new FetchTopTrackTask(getActivity());
          fetchTopTrackTask.execute(artistId);
        }
        else {
          Cursor cursor = getActivity().getContentResolver().query(topTrackUri,
            null, null, null, null);

          mTopTracksAdapter.swapCursor(cursor);

          mTopTracksListView.setAdapter(mTopTracksAdapter);
        }



        return rootView;

    }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  private class FetchTopTrackTask extends AsyncTask<String, Void, Void> {

    private FetchTopTrackTask(Context context) {
      mContext = context;
    }

    Context mContext;


    @Override
    protected Void doInBackground(String... params) {

      //configure spotify api wrapper
      SpotifyApi api = new SpotifyApi();
      SpotifyService spotify = api.getService();
      Map countryQueryMap = new HashMap<String,String>();
      countryQueryMap.put(getActivity().getString(R.string.country_key), getActivity().getString(R.string.country_value));

      //handle response
      spotify.getArtistTopTrack(params[0], countryQueryMap, new Callback<Tracks>() {
        @Override
        public void success(Tracks tracks, Response response) {

          Vector<ContentValues> cVVector = new Vector<ContentValues>(tracks.tracks.size());

          for (int i = 0; i < tracks.tracks.size(); i++) {

            String trackName = tracks.tracks.get(i).name;
            String trackAlbumName = tracks.tracks.get(i).album.name;
            String track_id = tracks.tracks.get(i).id;
            String trackImageUrl;
            if (tracks.tracks.get(i).album.images.size() == 0) {
              trackImageUrl = mContext.getString(R.string.no_image_found_url);
            } else {
              trackImageUrl = tracks.tracks.get(i).album.images.get(1).url;
            }

            ContentValues topTrackValues = new ContentValues();

            topTrackValues.put(SpotifyContract.TopTrackEntry.COLUMN_TRACK_NAME, trackName);
            topTrackValues.put(SpotifyContract.TopTrackEntry.COLUMN_ALBUM_NAME, trackAlbumName);
            topTrackValues.put(SpotifyContract.TopTrackEntry.COLUMN_TRACK_ID, track_id);
            topTrackValues.put(SpotifyContract.TopTrackEntry.COLUMN_TRACK_IMAGE_URL, trackImageUrl);

            cVVector.add(topTrackValues);
          }

          mContext.getContentResolver().delete(SpotifyContract.TopTrackEntry.CONTENT_URI, null, null);

          if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(SpotifyContract.TopTrackEntry.CONTENT_URI, cvArray);
          } else {
            Toast.makeText(mContext, "There are no tracks available for " + artistName, Toast.LENGTH_LONG).show();
          }

          Cursor cursor = mContext.getContentResolver().query(topTrackUri,
            null, null, null, null);

          mTopTracksAdapter.swapCursor(cursor);

          mTopTracksListView.setAdapter(mTopTracksAdapter);

        }

        @Override
        public void failure(RetrofitError error) {

        }
      });

      return null;

    }

  }


}
