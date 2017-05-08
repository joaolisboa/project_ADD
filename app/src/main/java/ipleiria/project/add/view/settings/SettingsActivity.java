package ipleiria.project.add.view.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ipleiria.project.add.R;
import ipleiria.project.add.Utils.ActivityUtils;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;

import static ipleiria.project.add.data.source.UserService.USER_DATA_KEY;

/**
 * Created by Lisboa on 06-May-17.
 */

public class SettingsActivity extends AppCompatActivity {

    static final String TAG = "SETTINGS_ACTIVITY";

    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (settingsFragment == null) {
            // Create the fragment
            settingsFragment = SettingsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), settingsFragment, R.id.contentFrame);
        }

        SettingsPresenter presenter = new SettingsPresenter(UserService.getInstance(), settingsFragment);
    }


    public void onDropboxClick(View view) {
        settingsFragment.onDropboxClick();
    }

    public void onMEOCloudClick(View view) {
        settingsFragment.onMEOCloudClick();
    }

    public void onGoogleAccountClick(View view){
        settingsFragment.onGoogleAccountClick();
    }
}
