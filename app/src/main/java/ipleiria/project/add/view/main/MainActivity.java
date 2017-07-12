package ipleiria.project.add.view.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.widget.FrameLayout;

import ipleiria.project.add.BaseDrawerActivity;
import ipleiria.project.add.R;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.ActivityUtils;
import ipleiria.project.add.view.setup.SetupActivity;

import static ipleiria.project.add.data.source.UserService.USER_DATA_KEY;


/**
 * Created by Lisboa on 04-May-17.
 */

public class MainActivity extends BaseDrawerActivity {

    static final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setTitle("Pending Files");

        SharedPreferences sharedPreferences = getSharedPreferences(USER_DATA_KEY,0);
        //sharedPreferences.edit().putBoolean("my_first_time", false).commit();
        if (sharedPreferences.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, start setup
            startActivity(new Intent(this, SetupActivity.class));
        }

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mainFragment == null) {
            // Create the fragment
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), mainFragment, R.id.contentFrame);
        }

        new MainPresenter(UserService.getInstance(), mainFragment, this, ItemsRepository.getInstance());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
