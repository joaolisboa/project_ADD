package ipleiria.project.add.view.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import ipleiria.project.add.BaseDrawerActivity;
import ipleiria.project.add.R;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.utils.ActivityUtils;

/**
 * Created by Lisboa on 06-May-17.
 */

public class SettingsActivity extends BaseDrawerActivity {

    static final String TAG = "SETTINGS_ACTIVITY";
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.settings_activity, (FrameLayout)findViewById(R.id.activity_frame));

        settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (settingsFragment == null) {
            // Create the fragment
            settingsFragment = SettingsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), settingsFragment, R.id.contentFrame);
        }

        new SettingsPresenter(settingsFragment, this, UserService.getInstance(), CategoryRepository.getInstance());
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
