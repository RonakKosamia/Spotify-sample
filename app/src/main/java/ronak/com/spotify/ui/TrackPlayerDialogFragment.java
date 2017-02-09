package ronak.com.spotify.ui;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import ronak.com.spotify.MediaPlayerService;
import ronak.com.spotify.R;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by horatio on 7/15/15.
 */
public class TrackPlayerDialogFragment extends DialogFragment {

  public static TrackPlayerDialogFragment newInstance(String trackId, int pos) {
    TrackPlayerDialogFragment trackPlayerDialogFragment = new TrackPlayerDialogFragment();

    Bundle args = new Bundle();
    args.putString("track_id", trackId);
    args.putInt("position", pos);

    trackPlayerDialogFragment.setArguments(args);

    return trackPlayerDialogFragment;
  }


  String trackId;
  String artistId;
  int position;
  TextView artistName;
  TextView albumName;
  ImageView albumImage;
  TextView trackName;
  SeekBar scrubBar;
  Button previousButton;
  Button playButton;
  Button pauseButton;
  Button nextButton;
  TextView playerPosition;
  TextView playerLength;
  String trackUrl;
  MediaPlayer mediaPlayer = new MediaPlayer();


  Messenger mService = null;
  boolean mBound;

  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

      mService = new Messenger(service);


      mBound = true;

      Message msg = Message.obtain(null, MediaPlayerService.MSG_START_SONG, 0, 0);

      Bundle bundle = new Bundle();
      bundle.putString("data_source", trackUrl);
      msg.setData(bundle);

      try {
        mService.send(msg);

      } catch (RemoteException e) {
        e.printStackTrace();
      }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      mService = null;
      mBound = false;
    }
  };


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    //mediaplayer


    View rootView = inflater.inflate(R.layout.fragment_track_player, container, false);
    //initialize views
    artistName = (TextView) rootView.findViewById(R.id.trackPlayerArtistName);
    albumName = (TextView) rootView.findViewById(R.id.trackPlayerAlbumName);
    albumImage = (ImageView) rootView.findViewById(R.id.trackPlayerAlbumImage);
    trackName = (TextView) rootView.findViewById(R.id.trackPlayerTrackName);
    scrubBar = (SeekBar) rootView.findViewById(R.id.scrubSeekBar);
    previousButton = (Button) rootView.findViewById(R.id.trackPlayerPreviousButton);
    playButton = (Button) rootView.findViewById(R.id.trackPlayerPlayButton);
    pauseButton = (Button) rootView.findViewById(R.id.trackPlayerPauseButton);
    nextButton = (Button) rootView.findViewById(R.id.trackPlayerNextButton);
    playerPosition = (TextView) rootView.findViewById(R.id.trackPlayerPosition);
    playerLength = (TextView) rootView.findViewById(R.id.trackPlayerLength);

    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


    //set listeners
    playButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        Message msg = Message.obtain(null, MediaPlayerService.MSG_PLAY_SONG, 0, 0);

        try {
          mService.send(msg);
        } catch (RemoteException e) {
          e.printStackTrace();
        }

        playButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);


      }
    });
    pauseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        Message msg = Message.obtain(null, MediaPlayerService.MSG_PAUSE_SONG, 0, 0);

        try {
          mService.send(msg);
        } catch (RemoteException e) {
          e.printStackTrace();
        }

        playButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
      }
    });
    nextButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //reset player
        playButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
        //check if at the end of list
        if (position != 9) {
          //set next track
          position++;
        }
        else {
          //first track
          position = 0;
        }

        SwitchTrackPlayerTask switchTrackPlayerTask = new SwitchTrackPlayerTask();
        switchTrackPlayerTask.execute();

      }
    });
    previousButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        playButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
        //check if not at first track
        if (position != 0) {
          //reset
          mediaPlayer.reset();
          //previous track
          position--;
          SwitchTrackPlayerTask switchTrackPlayerTask = new SwitchTrackPlayerTask();
          switchTrackPlayerTask.execute();
        } else {

          Message msg = Message.obtain(null, MediaPlayerService.MSG_CHANGE_SONG, 0, 0);

          Bundle bundle = new Bundle();
          bundle.putString("data_source", trackUrl);
          msg.setData(bundle);

          try {
            mService.send(msg);
          } catch (RemoteException e) {
            e.printStackTrace();
          }

        }
      }
    });

    Bundle arguments = getArguments();
    trackId = arguments.getString("track_id");
    position = arguments.getInt("position");
    //play track
    TrackPlayerTask trackPlayerTask = new TrackPlayerTask();
    trackPlayerTask.execute();


    IntentFilter filter = new IntentFilter();

    filter.addAction("com.play.action");

    BroadcastReceiver updateUIReciver = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {

        int maxDuration  = intent.getIntExtra("duration", 0) / 1000;

        //setup scrub bar
        scrubBar.setMax(maxDuration);
        int mCurrentPosition = intent.getIntExtra("position", 0) / 1000;
        scrubBar.setProgress(mCurrentPosition);

        //setup counter
        String mCurrentPositionString =
          String.format("%02d:%02d", (mCurrentPosition % 3600) / 60, (mCurrentPosition % 60));
        String maxDurationString =
          String.format("%02d:%02d", (maxDuration % 3600) / 60, (maxDuration % 60));
        playerPosition.setText(mCurrentPositionString);
        playerLength.setText(maxDurationString);

        //got to next track when current track finishes
        if (mCurrentPosition == maxDuration - 1) {
          if (position != 9) {
            position++;
            SwitchTrackPlayerTask switchTrackPlayerTask = new SwitchTrackPlayerTask();
            switchTrackPlayerTask.execute();
          }
        }

      }
    };
    getActivity().registerReceiver(updateUIReciver, filter);

    return rootView;
  }

  private class TrackPlayerTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {

      //configure spotify api wrapper
      SpotifyApi api = new SpotifyApi();
      SpotifyService spotify = api.getService();

      //handle response
      spotify.getTrack(trackId, new Callback<Track>() {
        @Override
        public void success(Track track, Response response) {
          //set values for views
          artistName.setText(track.artists.get(0).name);
          albumName.setText(track.album.name);
          artistId = track.artists.get(0).id;
          try {
            Picasso.with(getActivity()).load(track.album.images.get(1).url).into(albumImage);
          } catch (IllegalArgumentException e) {
            albumImage.setImageResource(
              getActivity().getResources().getIdentifier(
                getActivity().getString(R.string.placeholder_image),
                getActivity().getString(R.string.drawable),
                getActivity().getPackageName()
              )
            );
          }
          trackName.setText(track.name);
          //set track url
          trackUrl = track.preview_url;


          getActivity().bindService(
            new Intent(getActivity().getApplicationContext(), MediaPlayerService.class),
            mConnection,
            Context.BIND_AUTO_CREATE);


          //handle ui for seek bar
          scrubBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
              if (fromUser) {
                Message msg = Message.obtain(null, MediaPlayerService.MSG_SEEK_TO, 0, 0);

                Bundle bundle = new Bundle();
                bundle.putInt("progress", progress);
                msg.setData(bundle);

                try {
                  mService.send(msg);
                } catch (RemoteException e) {
                  e.printStackTrace();
                }
              }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
          });

        }

        @Override
        public void failure(RetrofitError error) {

        }
      });


      return null;
    }
  }

  private class SwitchTrackPlayerTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {

      Map countryQueryMap = new HashMap<String, String>();
      countryQueryMap.put(getActivity().getString(R.string.country_key), getActivity().getString(R.string.country_value));

      //configure spotify api wrapper
      SpotifyApi api = new SpotifyApi();
      SpotifyService spotify = api.getService();
      spotify.getArtistTopTrack(artistId, countryQueryMap, new Callback<Tracks>() {
        @Override
        public void success(Tracks tracks, Response response) {

          artistName.setText(tracks.tracks.get(position).artists.get(0).name);
          albumName.setText(tracks.tracks.get(position).album.name);
          try {
            Picasso.with(getActivity()).load(tracks.tracks.get(position).album.images.get(1).url).into(albumImage);
          } catch (IllegalArgumentException e) {
            albumImage.setImageResource(
              getActivity().getResources().getIdentifier(
                getActivity().getString(R.string.placeholder_image),
                getActivity().getString(R.string.drawable),
                getActivity().getPackageName()
              )
            );
          }
          trackName.setText(tracks.tracks.get(position).name);
          //set track url
          trackUrl = tracks.tracks.get(position).preview_url;

          Message msg = Message.obtain(null, MediaPlayerService.MSG_CHANGE_SONG, 0, 0);

          Bundle bundle = new Bundle();
          bundle.putString("data_source", trackUrl);
          msg.setData(bundle);

          try {
            mService.send(msg);
          } catch (RemoteException e) {
            e.printStackTrace();
          }

        }

        @Override
        public void failure(RetrofitError error) {

        }
      });

      return null;
    }
  }

}
