package ipleiria.project.add.view.categories;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.ActivityUtils;
import ipleiria.project.add.view.items.ItemsFragment;
import ipleiria.project.add.view.items.ItemsPresenter;

import static ipleiria.project.add.view.items.ItemsPresenter.LIST_DELETED_KEY;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesActivity extends AppCompatActivity {

    private CategoriesFragment categoriesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.categories_activity);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        categoriesFragment = (CategoriesFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (categoriesFragment == null) {
            // Create the fragment
            categoriesFragment = CategoriesFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), categoriesFragment, R.id.contentFrame);
        }

        CategoriesPresenter categoriesPresenter = new CategoriesPresenter(categoriesFragment,
                CategoryRepository.getInstance(),
                ItemsRepository.getInstance());
    }

    public void onBackPressed() {
        if(!categoriesFragment.onBackPressed()) {
            // Selected fragment did not consume the back press event.
            super.onBackPressed();
        }
    }

}
