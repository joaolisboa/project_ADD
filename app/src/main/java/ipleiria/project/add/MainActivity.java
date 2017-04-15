package ipleiria.project.add;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.Dropbox.DropboxDownloadFile;
import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.MEOCloud.Data.FileResponse;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Tasks.MEOGetMetadata;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.Tasks.MEODownloadFile;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Area;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Dimension;

public class MainActivity extends AppCompatActivity {

    private static final String AUTH_TAG = "AnonymousAuth";
    private static final String TAG = "MainActivity";

    private FirebaseAuth firebaseAuth;
    private Boolean authFlag = false;
    private FirebaseDatabase database;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.shared_prefs_user), MODE_PRIVATE);
        //preferences.edit().remove(SettingsActivity.DROPBOX_PREFS_KEY).apply();
        //preferences.edit().remove(SettingsActivity.MEO_PREFS_KEY).apply();
        ApplicationData.getInstance().setSharedPreferences(preferences);
        ApplicationData.getInstance().fillTestData(MainActivity.this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        if (hasNetwork()) {
            firebaseAuth = FirebaseAuth.getInstance();
            authListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        if(!authFlag){
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
                            if(displayName == null || displayName.isEmpty()){
                                displayName = "Anonymous";
                            }
                            ApplicationData.getInstance().setDisplayName(displayName);
                            ApplicationData.getInstance().setProfileUri(profileUri);
                            authFlag = true;
                            writeFirebaseData();
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
                                        }else{
                                            authFlag = true;
                                        }

                                        // ...
                                    }
                                });
                        Log.d(AUTH_TAG, "onAuthStateChanged:signed_out");
                    }
                }
            };

            if (!preferences.getString("dropbox_access_token", "").isEmpty()) {
                DropboxClientFactory.init(preferences.getString("dropbox_access_token", ""));
            }
            if (!preferences.getString("meo_access_token", "").isEmpty()) {
                MEOCloudClient.init(preferences.getString("meo_access_token", ""));
            }
        }

        database = FirebaseDatabase.getInstance();
        DatabaseReference categoryRef = database.getReference().child("categories");
        categoryRef.keepSynced(true);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Dimension> dimensions = new LinkedList<>();
                for (DataSnapshot dimensionSnap : dataSnapshot.getChildren()) {
                    Dimension dimension = new Dimension(dimensionSnap.child("name").getValue(String.class),
                            dimensionSnap.child("reference").getValue(Integer.class));
                    for (DataSnapshot areaSnap : dimensionSnap.child("areas").getChildren()) {
                        Area area = new Area(areaSnap.child("name").getValue(String.class),
                                areaSnap.child("reference").getValue(Integer.class));
                        for (DataSnapshot criteriaSnap : areaSnap.child("criterias").getChildren()) {
                            Criteria criteria = new Criteria(criteriaSnap.child("name").getValue(String.class),
                                    criteriaSnap.child("reference").getValue(Integer.class));
                            area.addCriteria(criteria);
                        }
                        dimension.addArea(area);
                    }
                    dimensions.add(dimension);
                }
                ApplicationData.getInstance().addDimensions(dimensions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public final void readExcel(View view) {
        try {
            List<Dimension> dimensions = new LinkedList<>();
            InputStream is = openFileInput("sample.csv");
            CSVReader reader = new CSVReader(new InputStreamReader(is, "ISO-8859-1"));
            Dimension lastDimension = null;
            Area lastArea = null;
            List<String[]> lines = reader.readAll();

            int x = 1, y = 1, z = 1;
            for (String[] nextLine : lines.subList(12, lines.size())) {
                String cell = nextLine[0];
                Dimension d;
                Area a;
                if (cell != null && !cell.isEmpty() && (!cell.equals("Total da componente técnico-científica")
                        && !cell.equals("Total da componente pedagógica")
                        && !cell.equals("Total da componente organizacional"))) {
                    cell = nextLine[1];
                    d = new Dimension(cell, x);
                    if (d.getName() == null || d.getName().isEmpty()) {
                        d = lastDimension;
                    } else {
                        lastDimension = d;
                        x++;
                        y=1;
                        z=1;
                        dimensions.add(d);
                    }
                    cell = nextLine[2];
                    a = new Area(cell, y);
                    if (a.getName() == null || a.getName().isEmpty()) {
                        a = lastArea;
                    } else {
                        lastArea = a;
                        y++;
                        z=1;
                        d.addArea(a);
                    }
                    cell = nextLine[3];
                    if (cell != null && !cell.isEmpty()) {
                        a.addCriteria(new Criteria(cell, z++));
                    }
                }
            }
            ApplicationData.getInstance().addDimensions(dimensions);
            System.out.println(dimensions);
            writeFirebaseData();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeFirebaseData() {
        String userUid = ApplicationData.getInstance().getUserUID();
        DatabaseReference userRef = database.getReference().child("users").child(userUid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "Value is: " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                System.out.println(error.getDetails());
                System.out.println(error.getMessage());
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        userRef.child("name").setValue(ApplicationData.getInstance().getDisplayName());
        for(Map.Entry<String, Boolean> email: ApplicationData.getInstance().getEmails().entrySet()) {
            DatabaseReference emailRef = userRef.child("emails").push();
            emailRef.child("email").setValue(email.getKey());
            emailRef.child("verified").setValue(email.getValue());
        }

        /*DatabaseReference categoryRef = database.getReference().child("categories");
        for (Dimension dimension : ApplicationData.getInstance().getDimensions()) {
            DatabaseReference dimRef = categoryRef.push();
            dimRef.child("name").setValue(dimension.getName());
            dimRef.child("reference").setValue(dimension.getReference());

            for (Area area : dimension.getAreas()) {
                DatabaseReference areaRef = dimRef.child("areas").push();
                areaRef.child("name").setValue(area.getName());
                areaRef.child("reference").setValue(area.getReference());

                for (Criteria criteria : area.getCriterias()) {
                    DatabaseReference criteriaRef = areaRef.child("criterias").push();
                    criteriaRef.child("name").setValue(criteria.getName());
                    criteriaRef.child("reference").setValue(criteria.getReference());
                }
            }
        }*/

    }

    public void goToSettings(View view) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    public void listFiles(View view) {
        startActivity(new Intent(this, ListActivity.class));
    }

    public void selectCriteria(View view) {
        startActivity(new Intent(this, SelectCategoryActivity.class));
    }

    public void downloadFile(View view) {
        new MEODownloadFile(MainActivity.this, new MEOCallback<FileResponse>() {

            @Override
            public void onComplete(MEOCloudResponse<FileResponse> result) {
                FileResponse fileResponse = result.getResponse();
                System.out.println(fileResponse.getPath());
                System.out.println(fileResponse.length());

            }

            @Override
            public void onRequestError(HttpErrorException httpE) {

            }

            @Override
            public void onError(Exception e) {
                Log.e("DownloadError", e.getMessage(), e);
            }
        }).execute("/exploring_luciddreaming.pdf");

        new MEOGetMetadata(new MEOCallback<MEOMetadata>() {

            @Override
            public void onComplete(MEOCloudResponse<MEOMetadata> result) {
                System.out.println(result.getResponse().toJson());
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {

            }

            @Override
            public void onError(Exception e) {
                Log.e("MetadataError", e.getMessage(), e);
            }
        }).execute("/");

        new DropboxDownloadFile(MainActivity.this, DropboxClientFactory.getClient(), new DropboxDownloadFile.Callback() {

            @Override
            public void onDownloadComplete(File result) {
                System.out.println(result.getName());
            }

            @Override
            public void onError(Exception e) {
                Log.e("ServiceError", e.getMessage(), e);
            }
        }).execute("/exploring_luciddreaming.pdf");

    }

    @Override
    public void onStart() {
        super.onStart();
        if(hasNetwork())
            firebaseAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            firebaseAuth.removeAuthStateListener(authListener);
        }
    }

    public void googleSignIn(View view) {
        startActivity(new Intent(MainActivity.this, GoogleSignInActivity.class));
    }

    private boolean hasNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
