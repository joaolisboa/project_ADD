package ipleiria.project.add.view.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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

import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import ipleiria.project.add.R;
import ipleiria.project.add.Utils.ActivityUtils;
import ipleiria.project.add.Utils.CircleTransformation;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.view.items.ItemsActivity;
import ipleiria.project.add.view.settings.SettingsActivity;

import static ipleiria.project.add.data.source.UserService.USER_DATA_KEY;
import static ipleiria.project.add.view.items.ItemsActivity.LIST_DELETED_KEY;

/**
 * Created by Lisboa on 04-May-17.
 */

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener, MainContract.DrawerView {

    static final String TAG = "MAIN_ACTIVITY";

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

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

        MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mainFragment == null) {
            // Create the fragment
            mainFragment = MainFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), mainFragment, R.id.contentFrame);
        }

        new MainPresenter(UserService.getInstance(), mainFragment, this);
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }else if(id == R.id.nav_trash) {
            Intent intent = new Intent(this, ItemsActivity.class);
            intent.putExtra(LIST_DELETED_KEY, true);
            startActivity(intent);
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
        Picasso.with(this)
                .load(user.getPhoto_url())
                .resize(100, 100)
                .transform(new CircleTransformation())
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .into((ImageView) navHeader.findViewById(R.id.profile_pic));
    }
}
