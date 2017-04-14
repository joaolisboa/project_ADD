package ipleiria.project.add;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

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
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.shared_prefs_user), MODE_PRIVATE);
        //preferences.edit().remove(SettingsActivity.DROPBOX_PREFS_KEY).apply();
        //preferences.edit().remove(SettingsActivity.MEO_PREFS_KEY).apply();
        ApplicationData.getInstance().setSharedPreferences(preferences);
        ApplicationData.getInstance().fillTestData();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    ApplicationData.getInstance().setUserUID(user.getUid());
                    writeFirebaseData();
                    Log.d(AUTH_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
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
                                    }

                                    // ...
                                }
                            });
                    Log.d(AUTH_TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        if(!preferences.getString("dropbox_access_token", "").isEmpty()){
            DropboxClientFactory.init(preferences.getString("dropbox_access_token", ""));
        }
        if(!preferences.getString("meo_access_token", "").isEmpty()){
            MEOCloudClient.init(preferences.getString("meo_access_token", ""));
        }

    }

    private void writeFirebaseData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // TODO: 14-Apr-17 add offline capability with firebase
        String userUid = ApplicationData.getInstance().getUserUID();
        DatabaseReference userRef = database.getReference().child("users").child(userUid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object value = dataSnapshot.getValue();
                Log.d(TAG, "Value is: " + value.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                System.out.println(error.getDetails());
                System.out.println(error.getMessage());
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        userRef.child("name").setValue(firebaseAuth.getCurrentUser().getDisplayName());

        userRef.child("emails").setValue(ApplicationData.getInstance().getEmails());

        for(Dimension dimension: ApplicationData.getInstance().getDimensions()){
            userRef.child("categories").child(dimension.getName()).child("reference").setValue(dimension.getReference());
            for(Area area: dimension.getAreas()){
                userRef.child("categories")
                        .child(dimension.getName())
                        .child("areas")
                        .child(area.getName())
                        .child("reference")
                        .setValue(area.getReference());
                for(Criteria criteria: area.getCriterias()){
                    userRef.child("categories")
                            .child(dimension.getName())
                            .child("areas")
                            .child(area.getName())
                            .child("criteria")
                            .child(criteria.getName())
                            .setValue(criteria.getReference());
                }
            }
        }

    }

    public void goToSettings(View view) {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    public void listFiles(View view){
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
}
