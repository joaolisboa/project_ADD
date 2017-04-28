package ipleiria.project.add;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Utils.CircleTransformation;
import ipleiria.project.add.Utils.CloudHandler;
import ipleiria.project.add.Utils.FileUtils;
import ipleiria.project.add.Utils.NetworkState;

import static ipleiria.project.add.AddItemActivity.SENDING_PHOTO;
import static ipleiria.project.add.FirebaseHandler.FIREBASE_UID_KEY;
import static ipleiria.project.add.SettingsActivity.DROPBOX_PREFS_KEY;
import static ipleiria.project.add.SettingsActivity.MEO_PREFS_KEY;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String AUTH_TAG = "AnonymousAuth";
    private static final String TAG = "MainActivity";

    static final int REQUEST_TAKE_PHOTO = 1;

    private FirebaseAuth firebaseAuth;
    private Boolean authFlag = false;
    private FirebaseAuth.AuthStateListener authListener;

    private NavigationView navigationView;
    String mCurrentPhotoPath;
    Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 23-Apr-17 open intent to take picture
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //       .setAction("Action", null).show();
                addItemOpeningCam();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (DatabaseException e) {
            // if any subsequent calls to setPersistence occur it won't crash...
            Log.d(TAG, e.getMessage());
        }
        SharedPreferences preferences = getSharedPreferences(getString(R.string.shared_prefs_user), MODE_PRIVATE);
        ApplicationData.getInstance().setSharedPreferences(preferences);
        FirebaseHandler.newInstance();
        FirebaseHandler.getInstance().readCategories();

        String userUID = preferences.getString(FIREBASE_UID_KEY, null);
        if (userUID != null) {
            ApplicationData.getInstance().setUserUID(userUID);
            // firebase should keep data offline so we don't need a connection to read bd
            FirebaseHandler.getInstance().readUserData(this);
            FirebaseHandler.getInstance().readEmails(this);
            FirebaseHandler.getInstance().readItems();
            FirebaseHandler.getInstance().readDeletedItems();
        } else {
            Log.e(TAG, "User is offline and has no UID stored in app - first time opening and offline?");
        }

        if (NetworkState.isOnline(this)) {
            firebaseAuth = FirebaseAuth.getInstance();
            initAuthListener();
            if (!preferences.getString(DROPBOX_PREFS_KEY, "").isEmpty()) {
                DropboxClientFactory.init(preferences.getString(DROPBOX_PREFS_KEY, null));
            }
            if (!preferences.getString(MEO_PREFS_KEY, "").isEmpty()) {
                MEOCloudClient.init(preferences.getString(MEO_PREFS_KEY, null));
            }
            // delay to guarantee dimensions will be read before scanning the dirs
            // TODO: 27-Apr-17 upload local files with background service while app is running?
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    List<File> localFiles = FileUtils.getLocalFiles(MainActivity.this);
                    if (!localFiles.isEmpty()) {
                        if (NetworkState.isOnline(MainActivity.this)) {
                            for (int i = 0; i < localFiles.size(); i++) {
                                System.out.println("LOCAL FILE: " + localFiles.get(i).getName());
                                CloudHandler.uploadFileToCloud(MainActivity.this, localFiles.get(i));
                            }
                            localFiles.clear();
                        }
                    }
                    List<File> localDeletedFiles = FileUtils.getLocalDeletedFiles(MainActivity.this);
                    if (!localDeletedFiles.isEmpty()) {
                        if (NetworkState.isOnline(MainActivity.this)) {
                            for (int i = 0; i < localDeletedFiles.size(); i++) {
                                System.out.println("LOCAL FILE: " + localDeletedFiles.get(i).getName());
                                CloudHandler.uploadFileToCloudTrash(MainActivity.this, localDeletedFiles.get(i));
                            }
                            localDeletedFiles.clear();
                        }
                    }
                    ApplicationData.getInstance().setLocalPendingFiles(localFiles);
                }
            }, 3000);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void listFiles(View view) {
        startActivity(new Intent(this, ListItemActivity.class));
    }

    public void selectCriteria(View view) {
        startActivity(new Intent(this, SelectCategoryActivity.class));
    }

    public void addItemOpeningCam() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                // Error occurred while creating the File
                Log.e(TAG, ex.getMessage(), ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "ipleiria.project.add.store",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    ;

    private File createImageFile() throws Exception {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_TAKE_PHOTO) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Intent photo = new Intent(MainActivity.this, ListItemActivity.class);
                photo.putExtra("photo_uri", photoURI.toString());
                startActivity(photo.setAction(SENDING_PHOTO));

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (NetworkState.isOnline(this)) {
            if (firebaseAuth == null) {
                firebaseAuth = FirebaseAuth.getInstance();
            }
            if (firebaseAuth != null) {
                if (authListener == null) {
                    initAuthListener();
                }
                firebaseAuth.addAuthStateListener(authListener);
            }
        }
    }


    private void initAuthListener() {
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (!authFlag) {
                        // User is signed in
                        ApplicationData.getInstance().setUserUID(user.getUid());
                        Log.d(AUTH_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                        String displayName = user.getDisplayName();
                        Uri profileUri = user.getPhotoUrl();

                        // If the above were null, iterate the provider data
                        // and set with the first non null data
                        for (UserInfo userInfo : user.getProviderData()) {
                            if (displayName == null && userInfo.getDisplayName() != null) {
                                displayName = userInfo.getDisplayName();
                            }
                            if (profileUri == null && userInfo.getPhotoUrl() != null) {
                                profileUri = userInfo.getPhotoUrl();
                            }
                        }
                        if (displayName == null || displayName.isEmpty()) {
                            displayName = "Anonymous";
                        }
                        ApplicationData.getInstance().setDisplayName(displayName);
                        ApplicationData.getInstance().setProfileUri(profileUri);
                        authFlag = true;
                        FirebaseHandler.getInstance().initReferences();
                        FirebaseHandler.getInstance().writeUserInfo();
                        View navHeader = navigationView.getHeaderView(0);
                        ((TextView) navHeader.findViewById(R.id.user_name)).setText(displayName);
                        ((TextView) navHeader.findViewById(R.id.user_mail)).setText(user.getEmail());
                        Picasso.with(MainActivity.this)
                                .load(ApplicationData.getInstance().getProfileUri())
                                .resize(150, 150)
                                .transform(new CircleTransformation())
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .error(R.drawable.ic_profile_placeholder)
                                .into((ImageView) navHeader.findViewById(R.id.profile_pic));
                    }
                } else {
                    // User is signed out or there's no credentials
                    firebaseAuth.signInAnonymously()
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(AUTH_TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Log.w(AUTH_TAG, "signInAnonymously", task.getException());
                                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        authFlag = true;
                                    }

                                    // ...
                                }
                            });
                    Log.d(AUTH_TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            firebaseAuth.removeAuthStateListener(authListener);
        }
    }

    public void updateUserInfo() {
        View navHeader = navigationView.getHeaderView(0);
        ((TextView) navHeader.findViewById(R.id.user_name)).setText(ApplicationData.getInstance().getDisplayName());
        if(!ApplicationData.getInstance().getEmails().isEmpty()){
            String text = ApplicationData.getInstance().getEmails().get(0).getEmail();
            if(ApplicationData.getInstance().getEmails().size() > 1){
                text += " (+" + String.valueOf(ApplicationData.getInstance().getEmails().size()-1) + ")";
            }
            ((TextView) navHeader.findViewById(R.id.user_mail)).setText(text);
        }
    }
}
