package ipleiria.project.add.view.settings;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ipleiria.project.add.Application;
import ipleiria.project.add.DrawerView;
import ipleiria.project.add.R;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.ActivityUtils;
import ipleiria.project.add.utils.CircleTransformation;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.view.categories.CategoriesActivity;
import ipleiria.project.add.view.main.MainActivity;

import static ipleiria.project.add.view.items.ItemsPresenter.LIST_DELETED_KEY;

/**
 * Created by Lisboa on 06-May-17.
 */

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerView {

    static final String TAG = "SETTINGS_ACTIVITY";

    private Date startDate_;
    private Date endDate_;

    private NavigationView navigationView;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

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
        navigationView.setCheckedItem(R.id.nav_home);

        settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (settingsFragment == null) {
            // Create the fragment
            settingsFragment = SettingsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), settingsFragment, R.id.contentFrame);
        }

        new SettingsPresenter(settingsFragment, this, UserService.getInstance());
    }

    public void onDropboxClick(View view) {
        settingsFragment.onDropboxClick();
    }

    public void onMEOCloudClick(View view) {
        settingsFragment.onMEOCloudClick();
    }

    public void onGoogleAccountClick(View view){
        settingsFragment.onGoogleAccountClick();
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
                startActivity(new Intent(this, CategoriesActivity.class));
                finish();
                return true;

            case R.id.nav_settings:
                break;

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
                        Snackbar.make(settingsFragment.getView(), "Exported file", Snackbar.LENGTH_SHORT);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, e.getMessage(), e);
                        progressDialog.dismiss();
                        Snackbar.make(settingsFragment.getView(), "Error while exporting", Snackbar.LENGTH_SHORT);
                    }
                });

                break;

            case R.id.create_period:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // Get the layout inflater
                LayoutInflater inflater = this.getLayoutInflater();

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(inflater.inflate(R.layout.create_new_period, null))
                        // Add action buttons
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EvaluationPeriod evaluationPeriod = new EvaluationPeriod();
                                evaluationPeriod.setStartDate(startDate_);
                                evaluationPeriod.setEndDate(endDate_);
                                UserService.getInstance().getUser().addEvaluationPeriod(evaluationPeriod);
                                UserService.getInstance().saveUserInfo();
                            }
                        })
                        .setNegativeButton("Cancel", null);
                AlertDialog dialog = builder.create();

                dialog.show();

                final String myFormat = "dd-MM-yyyy";
                final EditText startDate = (EditText) dialog.findViewById(R.id.startDate);
                final EditText endDate = (EditText) dialog.findViewById(R.id.endDate);
                final Calendar myCalendar = Calendar.getInstance();
                final SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
                startDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DatePickerDialog(SettingsActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, month);
                                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                startDate_ = myCalendar.getTime();
                                startDate.setText(sdf.format(myCalendar.getTime()));
                            }
                        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH))
                                .show();
                    }
                });

                endDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DatePickerDialog(SettingsActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, month);
                                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                endDate_ = myCalendar.getTime();
                                endDate.setText(sdf.format(myCalendar.getTime()));
                            }
                        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH))
                                .show();
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
                startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
            }
        });

    }

}
