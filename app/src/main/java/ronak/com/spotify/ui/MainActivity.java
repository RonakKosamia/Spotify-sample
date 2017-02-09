package ronak.com.spotify.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import ronak.com.spotify.R;


public class MainActivity extends ActionBarActivity {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.top_track_container) != null) {
          mTwoPane =true;

          if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
              .replace(R.id.top_track_container ,new TopTracksActivityFragment(), "TTFTAG")
              .commit();
          } else {
            mTwoPane = false;
          }

        }

    }




}
