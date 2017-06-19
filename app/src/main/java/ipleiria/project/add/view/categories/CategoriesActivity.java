package ipleiria.project.add.view.categories;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.ActivityUtils;
import ipleiria.project.add.utils.CircleTransformation;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.view.main.MainActivity;
import ipleiria.project.add.view.settings.SettingsActivity;

import static ipleiria.project.add.view.categories.CategoriesPresenter.LIST_DELETED_KEY;


/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerView {

    private static final String TAG = "CATEGORIES_ACTIVITY";

    private Date startDate_;
    private Date endDate_;

    private NavigationView navigationView;

    private CategoriesFragment categoriesFragment;
    private CategoriesPresenter categoriesPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.categories_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean listDeleted = getIntent().getBooleanExtra(LIST_DELETED_KEY, false);

        // set up the drawer layout
        //if(getIntent().getAction() == null){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //}

        categoriesFragment = (CategoriesFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (categoriesFragment == null) {
            // Create the fragment
            categoriesFragment = CategoriesFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), categoriesFragment, R.id.contentFrame);
        }

        categoriesPresenter = new CategoriesPresenter(categoriesFragment, this,
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.nav_categories:
                if(categoriesPresenter.isListingDeleted()){
                    Intent intent = new Intent(this, CategoriesActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.nav_trash:
                if(!categoriesPresenter.isListingDeleted()){
                    Intent intent = new Intent(this, CategoriesActivity.class);
                    intent.putExtra(LIST_DELETED_KEY, true);
                    startActivity(intent);
                }
                break;

            case R.id.export:
                progressDialog.show();
                progressDialog.setMessage("Creating files and exporting...");
                ItemsRepository.getInstance().getItems(false, new FilesRepository.Callback<List<Item>>() {
                    @Override
                    public void onComplete(List<Item> result) {
                        final Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            public void run() {
                                FileUtils.readExcel();
                                Uri sheet = Uri.fromFile(new File(Application.getAppContext().getFilesDir(), "ficha_avaliacao.xlsx"));
                                FilesRepository.getInstance().uploadFile(sheet, "export", "ficha_avaliacao.xlsx");
                                FileUtils.generateNote("relatorio.txt");
                                Uri doc = Uri.fromFile(new File(Application.getAppContext().getFilesDir(), "relatorio.txt"));
                                FilesRepository.getInstance().uploadFile(doc, "export", "relatorio.txt");
                                handler.post(new Runnable() {
                                    public void run() {

                                        progressDialog.dismiss();
                                        Snackbar.make(categoriesFragment.getView(), "Exported files", Snackbar.LENGTH_SHORT);
                                    }
                                });

                            }
                        };
                        new Thread(runnable).start();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, e.getMessage(), e);
                        progressDialog.dismiss();
                        Snackbar.make(categoriesFragment.getView(), "Error while exporting", Snackbar.LENGTH_SHORT);
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
                        new DatePickerDialog(CategoriesActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                        new DatePickerDialog(CategoriesActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                startActivity(new Intent(CategoriesActivity.this, SettingsActivity.class));
            }
        });
    }

}
