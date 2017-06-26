package ipleiria.project.add.view.categories;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.widget.FrameLayout;

import ipleiria.project.add.BaseDrawerActivity;
import ipleiria.project.add.R;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.ActivityUtils;

import static ipleiria.project.add.view.categories.CategoriesPresenter.LIST_DELETED_KEY;


/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesActivity extends BaseDrawerActivity {

    private static final String TAG = "CATEGORIES_ACTIVITY";

    private CategoriesFragment categoriesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_activity);

        boolean listDeleted = getIntent().getBooleanExtra(LIST_DELETED_KEY, false);

        categoriesFragment = (CategoriesFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (categoriesFragment == null) {
            // Create the fragment
            categoriesFragment = CategoriesFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), categoriesFragment, R.id.contentFrame);
        }

        CategoriesPresenter categoriesPresenter = new CategoriesPresenter(categoriesFragment, this,
                CategoryRepository.getInstance(), ItemsRepository.getInstance(),
                UserService.getInstance(), listDeleted);
        categoriesPresenter.setIntentInfo(getIntent());
    }

    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (!categoriesFragment.onBackPressed()) {
            // Selected fragment did not consume the back press event.
            super.onBackPressed();
        }
    }

}
