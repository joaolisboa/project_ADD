package ipleiria.project.add;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
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
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.CircleTransformation;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.utils.MyProvider;
import ipleiria.project.add.utils.UriHelper;
import ipleiria.project.add.view.categories.CategoriesActivity;
import ipleiria.project.add.view.main.MainActivity;
import ipleiria.project.add.view.settings.SettingsActivity;

import static ipleiria.project.add.utils.FileUtils.DOC_FILENAME;
import static ipleiria.project.add.utils.FileUtils.SHEET_FILENAME;
import static ipleiria.project.add.view.categories.CategoriesPresenter.LIST_DELETED_KEY;

/**
 * Created by Lisboa on 25-Jun-17.
 */

public class BaseDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerView, BaseContract.View{

    static final String TAG = "BASE_DRAWER_ACTIVITY";

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    private EditText startDate;
    private EditText endDate;
    private TextView dateError;

    private ProgressDialog progressDialog;
    private AlertDialog createPeriodDialog;

    private BaseContract.Presenter basePresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set up the drawer layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        basePresenter = new DrawerPresenter(this, ItemsRepository.getInstance(),
                FilesRepository.getInstance(), UserService.getInstance());


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        createPeriodDialog = builder.setView(inflater.inflate(R.layout.create_new_period, null))
                // Add action buttons
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", null)
                .create();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        FrameLayout activityFrame = (FrameLayout) drawerLayout.findViewById(R.id.activity_frame);
        getLayoutInflater().inflate(layoutResID, activityFrame, true);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //to prevent current item select over and over
        if (item.isChecked()){
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }

        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.nav_categories:
                startActivity(new Intent(this, CategoriesActivity.class));
                break;

            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.nav_trash:
                Intent intent = new Intent(this, CategoriesActivity.class);
                intent.putExtra(LIST_DELETED_KEY, true);
                startActivity(intent);
                break;

            case R.id.export:
                //exportFiles();
                basePresenter.exportFiles();
                drawerLayout.closeDrawer(GravityCompat.START);
                return false; // don't select item

            case R.id.create_period:
                createPeriod();
                drawerLayout.closeDrawer(GravityCompat.START);
                return false; // don't select item

        }
        drawerLayout.closeDrawer(GravityCompat.START);
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
                startActivity(new Intent(BaseDrawerActivity.this, SettingsActivity.class));
            }
        });
    }

    @Override
    public void showProgressDialog() {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(BaseDrawerActivity.this);
            progressDialog.setMessage("Creating files and exporting...");
        }
        progressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void setStartDateText(String date) {
        startDate.setText(date);
    }

    @Override
    public void setEndDateText(String date) {
        endDate.setText(date);
    }

    @Override
    public void showDatesValid() {
        startDate.setError(null);
        endDate.setError(null);
        dateError.setVisibility(View.GONE);
    }

    @Override
    public void showDatesInvalid() {
        startDate.setError("Start date must come before end date");
        endDate.setError("End date must come after start date");
        dateError.setVisibility(View.VISIBLE);
    }

    @Override
    public void showOnlineFilesExported() {
        new AlertDialog.Builder(this).setTitle("Files exported")
                .setMessage("Your files have been exported to your cloud services in the folder:\n" +
                            "Apps/Avaliacao Desempenho Docente/exported")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void showOfflineFilesExported() {
        ListView exportedFilesListView = new ListView(this);
        String[] files = new String[]{"Evaluation grid", "Curriculum Vitae"};
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);
        exportedFilesListView.setAdapter(adapter);

        exportedFilesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filePath = Application.getAppContext().getFilesDir().getAbsolutePath();

                if (position == 0) { // evaluation grid
                    filePath += "/" + SHEET_FILENAME;
                } else if (position == 1) { // curriculum vitae
                    filePath += "/" + DOC_FILENAME;
                }

                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = filePath.substring(filePath.lastIndexOf(".") + 1);
                String type = mime.getMimeTypeFromExtension(ext);

                File file = new File(filePath);
                int start = Application.getAppContext().getFilesDir().getAbsolutePath().length();
                String path = file.getAbsolutePath().substring(start, file.getAbsolutePath().length());

                Uri fileUri = UriHelper.getUriFromAppfile(path);

                Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                shareIntent.setDataAndType(fileUri, type);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Open file"));
            }
        });

        new AlertDialog.Builder(this).setView(exportedFilesListView)
                .setTitle("Select file to open")
                .setNegativeButton("Close", null)
                .show();
    }

    @Override
    public void setPresenter(BaseContract.Presenter presenter) {
        basePresenter = presenter;
    }

    private void createPeriod(){
        // this way we can override when the dialog should close
        createPeriodDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (basePresenter.areDatesValid()) {
                            basePresenter.createPeriod();
                            createPeriodDialog.dismiss();
                        }
                    }
                });
            }
        });

        createPeriodDialog.show();

        startDate = (EditText) createPeriodDialog.findViewById(R.id.startDate);
        endDate = (EditText) createPeriodDialog.findViewById(R.id.endDate);
        dateError = (TextView) createPeriodDialog.findViewById(R.id.date_error);

        final Calendar myCalendar = Calendar.getInstance();

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(BaseDrawerActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        basePresenter.setStartDate(year, month, dayOfMonth);
                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(BaseDrawerActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        basePresenter.setEndDate(year, month, dayOfMonth);
                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
    }


}