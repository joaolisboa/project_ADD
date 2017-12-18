package ipleiria.project.add.view.google_sign_in;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ipleiria.project.add.R;
import ipleiria.project.add.utils.ActivityUtils;

/**
 * Created by J on 09/05/2017.
 */

public class GoogleSignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_activity);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GoogleSignInFragment googleSignInFragment = (GoogleSignInFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (googleSignInFragment == null) {
            // Create the fragment
            googleSignInFragment = GoogleSignInFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), googleSignInFragment, R.id.contentFrame);
        }

        new GoogleSignInPresenter(googleSignInFragment);
    }

}
