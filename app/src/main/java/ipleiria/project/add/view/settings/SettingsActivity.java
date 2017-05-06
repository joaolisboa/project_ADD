package ipleiria.project.add.view.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;

import ipleiria.project.add.R;
import ipleiria.project.add.Utils.ActivityUtils;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.view.main.MainFragment;
import ipleiria.project.add.view.main.MainPresenter;

import static ipleiria.project.add.data.source.UserService.USER_DATA;

/**
 * Created by Lisboa on 06-May-17.
 */

public class SettingsActivity extends AppCompatActivity {

    static final String TAG = "SETTINGS_ACTIVITY";

    private SettingsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mainFragment == null) {
            // Create the fragment
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), mainFragment, R.id.contentFrame);
        }

        SharedPreferences preferences = getSharedPreferences(USER_DATA, MODE_PRIVATE);
        //presenter = new MainPresenter(UserService.initUserInstance(preferences), mainFragment, this);
    }

}
