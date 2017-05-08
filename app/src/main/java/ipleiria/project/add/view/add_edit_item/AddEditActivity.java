package ipleiria.project.add.view.add_edit_item;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ipleiria.project.add.R;
import ipleiria.project.add.Utils.ActivityUtils;
import ipleiria.project.add.data.source.ItemsRepository;
import ipleiria.project.add.data.source.UserService;

import static ipleiria.project.add.data.source.UserService.USER_DATA_KEY;

/**
 * Created by Lisboa on 07-May-17.
 */

public class AddEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_activity);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AddEditFragment addEditFragment = (AddEditFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (addEditFragment == null) {
            // Create the fragment
            addEditFragment = AddEditFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), addEditFragment, R.id.contentFrame);
        }

        new AddEditPresenter(addEditFragment, ItemsRepository.getInstance());

    }

}
