package ipleiria.project.add.view.setup;

import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import ipleiria.project.add.R;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.utils.ActivityUtils;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_activity);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SetupFragment setupFragment = (SetupFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (setupFragment == null) {
            // Create the fragment
            setupFragment = SetupFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), setupFragment, R.id.contentFrame);
        }

    }

    @Override
    public void onBackPressed() {
    }
}
