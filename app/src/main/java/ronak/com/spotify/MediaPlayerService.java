package ronak.com.spotify;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import java.io.IOException;

public class MediaPlayerService extends Service {

  public static final int MSG_START_SONG = 1;
  public static final int MSG_PAUSE_SONG = 2;
  public static final int MSG_PLAY_SONG = 3;
  public static final int MSG_CHANGE_SONG = 4;
  public static final int MSG_SEEK_TO = 5;


  public static MediaPlayer mMediaPlayer = new MediaPlayer();
  Handler handler;

  @Override
  public void onCreate() {
    super.onCreate();

    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


  }

  private void runOnUiThread(Runnable runnable) {
    handler.post(runnable);
  }

  class IncomingHandler extends Handler {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_START_SONG:
          Bundle bundle = msg.getData();
          String trackUrl = bundle.getString("data_source");
          handler = new Handler();

          try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(trackUrl);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

              @Override
              public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();

                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Intent local = new Intent();
                    local.setAction("com.play.action");
                    local.putExtra("duration", mMediaPlayer.getDuration());
                    local.putExtra("position", mMediaPlayer.getCurrentPosition());
                    sendBroadcast(local);

                    handler.postDelayed(this, 1000);

                  }
                });
              }
            });
          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        case MSG_PLAY_SONG:
          if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
          }
          mMediaPlayer.start();
          break;
        case MSG_PAUSE_SONG:
          mMediaPlayer.pause();
          break;
        case MSG_CHANGE_SONG:
          Bundle change_bundle = msg.getData();
          String change_track_url = change_bundle.getString("data_source");

          mMediaPlayer.reset();

          try {
            mMediaPlayer.setDataSource(change_track_url);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
              @Override
              public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
              }
            });

          } catch (IOException e) {
            e.printStackTrace();
          }
          break;
        case MSG_SEEK_TO:
          Bundle seek_to_data = msg.getData();
          int progress = seek_to_data.getInt("progress");
          mMediaPlayer.seekTo(progress * 1000);
          break;
        default:
          super.handleMessage(msg);
      }
    }
  }

  final Messenger mMessenger = new Messenger(new IncomingHandler());

  @Override
  public IBinder onBind(Intent intent) {

    return mMessenger.getBinder();
  }

  @Override
  public boolean onUnbind(Intent intent) {

    return false;
  }
}
