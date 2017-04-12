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
import ipleiria.project.add.Model.Category;

public class MainActivity extends AppCompatActivity {

    private static final String AUTH_TAG = "AnonymousAuth";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.shared_prefs_user), MODE_PRIVATE);
        ApplicationData.getInstance().setSharedPreferences(preferences);

        firebaseAuth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    ApplicationData.getInstance().setUserUID(user.getUid());
                    Log.d(AUTH_TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    // User is signed out
                    Log.d(AUTH_TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        firebaseAnonymousLogin();

        if(!preferences.getString("dropbox_access_token", "").isEmpty()){
            DropboxClientFactory.init(preferences.getString("dropbox_access_token", ""));
        }
        if(!preferences.getString("meo_access_token", "").isEmpty()){
            MEOCloudClient.init(preferences.getString("meo_access_token", ""));
        }

        testCategory();
    }

    private void testCategory() {
        Category c1 = new Category("root1", 1);
        Category c2 = new Category("root2", 2);
        for(int i = 1; i < 4; i++){
            Category child = new Category("child"+i, i);
            c1.addChild(child);
            c2.addChild(child);
        }
        for(Category cat: c1.getChildren()){
            Category child1_1 = new Category("child1.1", 1);
            Category child1_2 = new Category("child1.2", 2);
            cat.addChildren(child1_1, child1_2);
        }
        for(Category cat: c2.getChildren()){
            Category child1_1 = new Category("child1.1", 1);
            Category child1_2 = new Category("child1.2", 2);
            cat.addChildren(child1_1, child1_2);
        }

        ApplicationData.getInstance().addDimensions(c1, c2);
        System.out.println(ApplicationData.getInstance().getDimensions().size());
        System.out.println(ApplicationData.getInstance().getAreas().size());
        System.out.println(ApplicationData.getInstance().getCategories().size());
    }

    private void firebaseAnonymousLogin() {
        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
    }

    public void listFiles(View view){
        startActivity(new Intent(this, ListActivity.class));
    }

    public void goToAccounts(View view) {
        startActivity(new Intent(this, ServiceChooserActivity.class));
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
}
