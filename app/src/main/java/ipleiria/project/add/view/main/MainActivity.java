package ipleiria.project.add.view.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.MEOCloudService;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.ActivityUtils;
import ipleiria.project.add.utils.CircleTransformation;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.view.categories.CategoriesActivity;
import ipleiria.project.add.view.categories.CategoriesFragment;
import ipleiria.project.add.view.categories.CategoriesPresenter;
import ipleiria.project.add.view.items.ItemsActivity;
import ipleiria.project.add.view.items.ItemsFragment;
import ipleiria.project.add.view.items.ItemsPresenter;
import ipleiria.project.add.view.settings.SettingsActivity;

import static ipleiria.project.add.view.items.ItemsPresenter.LIST_DELETED_KEY;


/**
 * Created by Lisboa on 04-May-17.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MainContract.DrawerView {

    static final String TAG = "MAIN_ACTIVITY";

    private NavigationView navigationView;
    private MainFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setTitle("Pending Files");

        // Set up the toolbar.
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

        mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mainFragment == null) {
            // Create the fragment
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), mainFragment, R.id.contentFrame);
        }

        new MainPresenter(UserService.getInstance(), mainFragment, this);

        /*BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // handle desired action here
                // One possibility of action is to replace the contents above the nav bar
                // return true if you want the item to be displayed as the selected item
                Fragment selectedFragment = null;
                switch(item.getItemId()){
                    case R.id.menu_home:
                        MainFragment mainFragment1 = MainFragment.newInstance();
                        new MainPresenter(UserService.getInstance(), mainFragment1, MainActivity.this);
                        selectedFragment = mainFragment1;
                        break;

                    case R.id.menu_all:
                        CategoriesFragment fragment = CategoriesFragment.newInstance();
                        new CategoriesPresenter(fragment, CategoryRepository.getInstance(), ItemsRepository.getInstance());
                        selectedFragment = fragment;
                        break;

                    case R.id.menu_trash:
                        ItemsFragment itemsFragment = ItemsFragment.newInstance();
                        new ItemsPresenter(ItemsRepository.getInstance(), itemsFragment, true);
                        selectedFragment = itemsFragment;
                        break;
                }

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.contentFrame, selectedFragment);
                transaction.commit();
                return true;
            }
        });*/
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.categories:
                startActivity(new Intent(this, CategoriesActivity.class));
                return true;

            case R.id.list_items:
                startActivity(new Intent(this, ItemsActivity.class));
                return true;

            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.nav_trash:
                Intent intent = new Intent(this, ItemsActivity.class);
                intent.putExtra(LIST_DELETED_KEY, true);
                startActivity(intent);
                return true;

            case R.id.export:
                progressDialog.show();
                progressDialog.setTitle("Creating sheet...");
                ItemsRepository.getInstance().getItems(new FilesRepository.Callback<List<Item>>() {
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
                        Snackbar.make(mainFragment.getView(), "Exported file", Snackbar.LENGTH_SHORT);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, e.getMessage(), e);
                        progressDialog.dismiss();
                        Snackbar.make(mainFragment.getView(), "Error while exporting", Snackbar.LENGTH_SHORT);
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
                .load(user.getPhoto_url())
                .resize(100, 100)
                .transform(new CircleTransformation())
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .into(profilePicView);

        profilePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

    }
}
