package ronak.com.spotify.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ronak.com.spotify.R;
import ronak.com.spotify.model.SpotifyContract;
import ronak.com.spotify.ui.TrackPlayerActivity;
import ronak.com.spotify.ui.TrackPlayerDialogFragment;

public class TopTracksAdapter extends CursorAdapter {

  Context mContext;
  android.app.FragmentManager mFragmentManager;

  public static class ViewHolder {
    public final TextView mTrackNameTextView;
    public final TextView mAlbumNameTextView;
    public final ImageView mAlbumImageView;

    public ViewHolder(View view) {
      mTrackNameTextView = (TextView) view.findViewById(R.id.topTrackNameTextView);
      mAlbumNameTextView = (TextView) view.findViewById(R.id.topTrackAlbumNameTextView);
      mAlbumImageView = (ImageView) view.findViewById(R.id.topTrackAlbumImageView);
    }
  }

  public TopTracksAdapter(Context context, Cursor c, int flags, android.app.FragmentManager fm) {
    super(context, c, flags);
    mContext = context;
    mFragmentManager = fm;
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {

    int layoutId = R.layout.top_tracks_list_item;

    View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

    ViewHolder viewHolder = new ViewHolder(view);
    view.setTag(viewHolder);

    return view;

  }

  @Override
  public void bindView(View view, final Context context, final Cursor cursor) {

    ViewHolder viewHolder = (ViewHolder) view.getTag();

    int idx_track_name = cursor.getColumnIndex(SpotifyContract.TopTrackEntry.COLUMN_TRACK_NAME);
    int idx_album_name = cursor.getColumnIndex(SpotifyContract.TopTrackEntry.COLUMN_ALBUM_NAME);
    int idx_album_image_url = cursor.getColumnIndex(SpotifyContract.TopTrackEntry.COLUMN_TRACK_IMAGE_URL);
    int idx_track_id = cursor.getColumnIndex(SpotifyContract.TopTrackEntry.COLUMN_TRACK_ID);

    final String trackName = cursor.getString(idx_track_name);
    final String albumName = cursor.getString(idx_album_name);
    final String albumImageURl = cursor.getString(idx_album_image_url);
    final String trackId = cursor.getString(idx_track_id);

    viewHolder.mTrackNameTextView.setText(trackName);
    viewHolder.mAlbumNameTextView.setText(albumName);

    try {
      Picasso.with(context).load(albumImageURl).into(viewHolder.mAlbumImageView);
    }
    catch (IllegalArgumentException e) {
      viewHolder.mAlbumImageView.setImageResource(
        context.getResources()
          .getIdentifier(
            context.getString(R.string.placeholder_image),
            context.getString(R.string.drawable),
            context.getPackageName())
      );
    }

    final int pos = cursor.getPosition();

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        int screenSize = mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
          TrackPlayerDialogFragment trackPlayerDialogFragment = TrackPlayerDialogFragment.newInstance(trackId, pos);
          trackPlayerDialogFragment.show(mFragmentManager, "track_player");
        } else {
          Intent intent = new Intent(v.getContext(), TrackPlayerActivity.class)
            .putExtra(Intent.EXTRA_TEXT, trackId)
            .putExtra("position", pos);
          v.getContext().startActivity(intent);
        }
      }
    });

  }
}
