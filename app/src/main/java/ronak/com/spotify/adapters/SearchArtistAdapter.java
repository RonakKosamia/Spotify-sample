package ronak.com.spotify.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ronak.com.spotify.R;
import ronak.com.spotify.model.SpotifyContract;
import ronak.com.spotify.ui.TopTracksActivity;
import ronak.com.spotify.ui.TopTracksActivityFragment;

public class SearchArtistAdapter extends CursorAdapter {

  Context mContext;
  android.support.v4.app.FragmentManager mFm;

  public static class ViewHolder {

    public final TextView mSearchArtistNameTextView;
    public final ImageView mSearchArtistImageView;
    public String mArtistId;

    public ViewHolder(View view) {
      mSearchArtistNameTextView = (TextView) view.findViewById(R.id.searchArtistNameTextView);
      mSearchArtistImageView = (ImageView) view.findViewById(R.id.searchArtistImageView);
      mArtistId = "";

    }

  }

  public SearchArtistAdapter(Context context, Cursor c, int flags, android.support.v4.app.FragmentManager fm) {
    super(context, c, flags);
    mContext = context;
    mFm = fm;
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {

    int layoutId = R.layout.search_artist_list_item;

    View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

    ViewHolder viewHolder = new ViewHolder(view);
    view.setTag(viewHolder);

    return view;
  }


  @Override
  public void bindView(View view, Context context, final Cursor cursor) {
    ViewHolder viewHolder = (ViewHolder) view.getTag();

    int idx_artist_name = cursor.getColumnIndex(SpotifyContract.SearchArtistEntry.COLUMN_ARTIST_NAME);
    int idx_artist_image_url = cursor.getColumnIndex(SpotifyContract.SearchArtistEntry.COLUMN_ARTIST_IMAGE_URL);
    int idx_artist_id = cursor.getColumnIndex(SpotifyContract.SearchArtistEntry.COLUMN_ARTIST_ID);

    final String artistName = cursor.getString(idx_artist_name);
    final String artistImageUrl = cursor.getString(idx_artist_image_url);
    final String artist_id = cursor.getString(idx_artist_id);

    viewHolder.mSearchArtistNameTextView.setText(artistName);
    viewHolder.mArtistId = artist_id;


    try {
      Picasso.with(context).load(artistImageUrl).into(viewHolder.mSearchArtistImageView);
    }
    catch (IllegalArgumentException e){
                viewHolder.mSearchArtistImageView.setImageResource(
                  context.getResources()
                    .getIdentifier(
                      context.getString(R.string.placeholder_image),
                      context.getString(R.string.drawable),
                      context.getPackageName()));
    }

    view.setOnClickListener(new View.OnClickListener() {



      @Override
      public void onClick(View v) {

        int screenSize = mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {

          Bundle bundle = new Bundle();
          bundle.putString("artist_id", artist_id);
          bundle.putString("artist_name", artistName);

          TopTracksActivityFragment fragment = new TopTracksActivityFragment();
          fragment.setArguments(bundle);

          mFm.beginTransaction()
            .replace(R.id.top_track_container, fragment, "TTFTAG")
            .commit();

        } else {
          Intent intent = new Intent(v.getContext(), TopTracksActivity.class)
            .putExtra(Intent.EXTRA_TEXT, artist_id)
            .putExtra("artist_name", artistName);
          v.getContext().startActivity(intent);
        }
      }
    });

  }


}
