package ipleiria.project.add.view.categories;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import ipleiria.project.add.Application;
import ipleiria.project.add.DrawerView;
import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.ActivityUtils;
import ipleiria.project.add.utils.CircleTransformation;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.view.main.MainActivity;
import ipleiria.project.add.view.settings.SettingsActivity;

import static ipleiria.project.add.view.items.ItemsPresenter.LIST_DELETED_KEY;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerView {

    private static final String TAG = "CATEGORIES_ACTIVITY";

    private NavigationView navigationView;

    private CategoriesFragment categoriesFragment;
    private CategoriesPresenter categoriesPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.categories_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set up the drawer layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        categoriesFragment = (CategoriesFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (categoriesFragment == null) {
            // Create the fragment
            categoriesFragment = CategoriesFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), categoriesFragment, R.id.contentFrame);
        }

        boolean listDeleted = getIntent().getBooleanExtra(LIST_DELETED_KEY, false);
        categoriesPresenter = new CategoriesPresenter(categoriesFragment, this,
                CategoryRepository.getInstance(), ItemsRepository.getInstance(), listDeleted);
        categoriesPresenter.setIntentInfo(getIntent());

        if(listDeleted) {
            navigationView.setCheckedItem(R.id.nav_trash);
        }else{
            navigationView.setCheckedItem(R.id.nav_categories);
        }
    }

    public void onBackPressed() {
        if(!categoriesFragment.onBackPressed()) {
            // Selected fragment did not consume the back press event.
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;

            case R.id.nav_categories:
                break;

            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;

            case R.id.nav_trash:
                Intent intent = new Intent(this, CategoriesActivity.class);
                intent.putExtra(LIST_DELETED_KEY, true);
                startActivity(intent);
                finish();
                return true;

            case R.id.export:
                progressDialog.show();
                progressDialog.setTitle("Creating sheet...");
                ItemsRepository.getInstance().getItems(false, new FilesRepository.Callback<List<Item>>() {
                    @Override
                    public void onComplete(List<Item> result) {
                        Uri sheet = Uri.fromFile(new File(Application.getAppContext().getFilesDir(), "ficha_avaliacao.xlsx"));
                        progressDialog.setTitle("Uploading sheet...");
                        FilesRepository.getInstance().uploadFile(sheet, "export", "ficha_avaliacao.xlsx");
                        progressDialog.setTitle("Uploading report...");
                        FileUtils.generateNote("relatorio.txt");
                        Uri doc = Uri.fromFile(new File(Application.getAppContext().getFilesDir(), "relatorio.txt"));
                        FilesRepository.getInstance().uploadFile(doc, "export", "relatorio.txt");
                        progressDialog.dismiss();
                        Snackbar.make(categoriesFragment.getView(), "Exported file", Snackbar.LENGTH_SHORT);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, e.getMessage(), e);
                        progressDialog.dismiss();
                        Snackbar.make(categoriesFragment.getView(), "Error while exporting", Snackbar.LENGTH_SHORT);
                    }
                });

                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void setUserInfo(User user) {
        View navHeader = navigationView.getHeaderView(0);
        ((TextView) navHeader.findViewById(R.id.user_name)).setText(user.getName());
        ((TextView) navHeader.findViewById(R.id.user_mail)).setText(user.getEmail());
        ImageView profilePicView = (ImageView) navHeader.findViewById(R.id.profile_pic);
        Picasso.with(this)
                .load(user.getPhotoUrl())
                .resize(100, 100)
                .transform(new CircleTransformation())
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .into(profilePicView);

        profilePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CategoriesActivity.this, SettingsActivity.class));
            }
        });

    }

}
