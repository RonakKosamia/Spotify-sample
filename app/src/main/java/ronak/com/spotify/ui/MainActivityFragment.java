package ronak.com.spotify.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Vector;

import ronak.com.spotify.R;
import ronak.com.spotify.Utility;
import ronak.com.spotify.adapters.SearchArtistAdapter;
import ronak.com.spotify.model.SpotifyContract;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
  //Global vars
  private SearchArtistAdapter mSearchArtistAdapter;
  private ListView mListView;
  private Cursor mCursor;
  private Vector<ContentValues> cVVector;

  final Uri searchArtistUri = SpotifyContract.BASE_CONTENT_URI.buildUpon().appendPath(SpotifyContract.PATH_SEARCH_ARTIST).build();

  public MainActivityFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {


    //initialize views
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);
    final EditText searchArtistEditText = (EditText) rootView.findViewById(R.id.searchArtistEditText);
    mListView = (ListView) rootView.findViewById(R.id.searchArtistListView);

    final Cursor cursor = getActivity().getContentResolver().query(searchArtistUri,
      null, null, null, null);

    mSearchArtistAdapter = new SearchArtistAdapter(getActivity(), cursor, 0, getActivity().getSupportFragmentManager());

    mListView.setAdapter(mSearchArtistAdapter);


    searchArtistEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        if (Utility.isNetworkAvailable(getActivity())) {

          FetchArtistTask fetchArtistTask = new FetchArtistTask();
          String searchQuery = String.valueOf(s);
          Log.v("search query", searchQuery);
          fetchArtistTask.execute(searchQuery);


        } else {
          Toast.makeText(getActivity(), getActivity().getString(R.string.network_unavailable_message), Toast.LENGTH_SHORT).show();
        }
      }


    });


    return rootView;
  }

  private class FetchArtistTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {

      //configure spotify api wrapper
      SpotifyApi api = new SpotifyApi();
      SpotifyService spotify = api.getService();
      //search for artist
      spotify.searchArtists(params[0], new Callback<ArtistsPager>() {
        @Override
        public void success(ArtistsPager artistsPager, Response response) {

          cVVector = new Vector<ContentValues>(artistsPager.artists.items.size());

          for (int i = 0; i < artistsPager.artists.items.size(); i++) {

            String artistImageUrl;
            String artistName;
            String artistSpotifyId;


            if (artistsPager.artists.items.get(i).images.size() == 0) {
              artistImageUrl = getActivity().getString(R.string.no_image_found_url);
            } else {
              artistImageUrl = artistsPager.artists.items.get(i).images.get(1).url;
            }
            artistName = artistsPager.artists.items.get(i).name;
            artistSpotifyId = artistsPager.artists.items.get(i).id;

            ContentValues searchArtistValues = new ContentValues();
            Log.v("artist name", artistName);
            searchArtistValues.put(SpotifyContract.SearchArtistEntry.COLUMN_ARTIST_IMAGE_URL, artistImageUrl);
            searchArtistValues.put(SpotifyContract.SearchArtistEntry.COLUMN_ARTIST_NAME, artistName);
            searchArtistValues.put(SpotifyContract.SearchArtistEntry.COLUMN_ARTIST_ID, artistSpotifyId);

            cVVector.add(searchArtistValues);
          }

          if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getActivity().getContentResolver().delete(SpotifyContract.SearchArtistEntry.CONTENT_URI, null, null);
            getActivity().getContentResolver().bulkInsert(SpotifyContract.SearchArtistEntry.CONTENT_URI, cvArray);

          } else {
            getActivity().getContentResolver().delete(SpotifyContract.SearchArtistEntry.CONTENT_URI, null, null);
            Toast.makeText(getActivity(), "Sorry that artist cannot be found.", Toast.LENGTH_SHORT).show();
          }

          mCursor = getActivity().getContentResolver().query(searchArtistUri,
            null, null, null, null);

          mSearchArtistAdapter.swapCursor(mCursor);
          mListView.setAdapter(mSearchArtistAdapter);

        }

        @Override
        public void failure(RetrofitError error) {
          getActivity().getContentResolver().delete(SpotifyContract.SearchArtistEntry.CONTENT_URI, null, null);

          mCursor = getActivity().getContentResolver().query(searchArtistUri,
            null, null, null, null);

          mSearchArtistAdapter.swapCursor(mCursor);
          mListView.setAdapter(mSearchArtistAdapter);
        }


      });

      return null;
    }

  }
}
