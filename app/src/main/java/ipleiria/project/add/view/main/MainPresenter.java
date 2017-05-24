package ipleiria.project.add.view.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.RequestMailsTask;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.view.items.ItemsActivity;

import static ipleiria.project.add.data.source.UserService.AUTH_TAG;
import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PHOTO;
import static ipleiria.project.add.view.google_sign_in.GoogleSignInPresenter.SCOPES;
import static ipleiria.project.add.view.main.MainFragment.REQUEST_AUTHORIZATION;

/**
 * Created by Lisboa on 05-May-17.
 */

class MainPresenter implements MainContract.Presenter {

    private static final String TAG = "MAIN_PRESENTER";

    static final int REQUEST_TAKE_PHOTO = 2002;

    private final UserService userService;
    private final MainContract.View mainView;
    private final MainContract.DrawerView drawerView;

    private GoogleApiClient googleApiClient;
    private Gmail mService;

    private final FilesRepository filesRepository;
    private List<ItemFile> pendingFiles;

    private Uri photoUri;
    // ensure we want to sign to avoid consequent calls to authStateListener
    private boolean authFlag = false;

    MainPresenter(@NonNull UserService userService, @NonNull MainContract.View mainView, @NonNull MainContract.DrawerView drawerView) {
        this.userService = userService;
        this.mainView = mainView;
        this.mainView.setPresenter(this);
        this.drawerView = drawerView;

        this.filesRepository = FilesRepository.getInstance();
        this.pendingFiles = new LinkedList<>();
    }

    @Override
    public void subscribe() {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

        filesRepository.getRemotePendingFiles(new FilesRepository.ServiceCallback<List<ItemFile>>() {
            @Override
            public void onMEOComplete(List<ItemFile> result) {
                for (ItemFile file : result) {
                    if (!pendingFiles.contains(file)) {
                        pendingFiles.add(file);
                        mainView.addPendingFile(file);
                    }
                }
            }

            @Override
            public void onMEOError() {
            }

            @Override
            public void onDropboxComplete(List<ItemFile> result) {
                for (ItemFile file : result) {
                    if (!pendingFiles.contains(file)) {
                        pendingFiles.add(file);
                        mainView.addPendingFile(file);
                    }
                }
            }

            @Override
            public void onDropboxError() {
            }
        });

        processPendingFiles();
    }

    @Override
    public void unsubscribe() {
        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }
    }

    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                //prevent subsequent calls if auth fails and signs in anonymously below
                if (!authFlag) {
                    // user signed in successfully
                    Log.d(AUTH_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    authFlag = true;
                    UserService.getInstance().initUser(user);
                    ItemsRepository.getInstance().initUser(user.getUid());
                    if (!user.isAnonymous()) {
                        // if user is not anonymous get google credentials and fetch emails
                        checkForCachedCredentials();
                    }
                }
                drawerView.setUserInfo(UserService.getInstance().getUser());
            } else {
                // User is signed out or there's no credentials
                // try to sign in anonymously
                userSignInAnonymous();
                Log.d(AUTH_TAG, "onAuthStateChanged:signed_out");
            }
        }
    };

    private void userSignInAnonymous() {
        userService.getAnonymousUser().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Log.d(AUTH_TAG, "onAuthStateChanged:signed_in_anonymously:" + user.getUid());
                    authFlag = true;
                    UserService.getInstance().initUser(user);
                    drawerView.setUserInfo(UserService.getInstance().getUser());
                } else {
                    Log.d(AUTH_TAG, "onAuthStateChanged:auth_failed");
                }
            }
        });
    }

    @Override
    public void result(int requestCode, int resultCode, Context context) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                Intent photo = new Intent(context, ItemsActivity.class);
                photo.putExtra("photo_uri", photoUri.toString());
                context.startActivity(photo.setAction(SENDING_PHOTO));
            }
        }
    }

    @Override
    public void buildGoogleClient(FragmentActivity fragment,
                                  GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener,
                                  String webClientID) {
        if (googleApiClient == null) {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientID)
                    .requestEmail()
                    .build();

            googleApiClient = new GoogleApiClient.Builder(fragment)
                    .enableAutoManage(fragment, onConnectionFailedListener)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

        } else if (mService != null) {
            new RequestMailsTask(mService, filesRepository, userService.getUser().getEmail(), new RequestMailsTask.MailCallback() {
                @Override
                public void onComplete(List<ItemFile> result) {
                    for (ItemFile file : result) {
                        if (!pendingFiles.contains(file)) {
                            pendingFiles.add(file);
                            mainView.addPendingFile(file);
                        }
                    }
                }
            }).execute();
        }

    }

    private void checkForCachedCredentials() {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                System.out.println(acct.getAccount());
                GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(Application.getAppContext(), Arrays.asList(SCOPES));
                credential.setSelectedAccount(acct.getAccount());
                HttpTransport transport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                mService = new Gmail.Builder(transport, jsonFactory, credential)
                        .setApplicationName("Project ADD")
                        .build();

                new RequestMailsTask(mService, filesRepository, userService.getUser().getEmail(), new RequestMailsTask.MailCallback() {
                    @Override
                    public void onComplete(List<ItemFile> pendingFiles) {
                        System.out.println(pendingFiles);
                        mainView.addPendingFiles(pendingFiles);
                    }
                }).execute();
            }
        }
    }

    @Override
    public void onSwipeRefresh() {
        filesRepository.getRemotePendingFiles(new FilesRepository.ServiceCallback<List<ItemFile>>() {
            @Override
            public void onMEOComplete(List<ItemFile> result) {
                for (ItemFile file : result) {
                    if (!pendingFiles.contains(file)) {
                        pendingFiles.add(file);
                        mainView.addPendingFile(file);
                    }
                }
            }

            @Override
            public void onMEOError() {
            }

            @Override
            public void onDropboxComplete(List<ItemFile> result) {
                addFiles(pendingFiles);
            }

            @Override
            public void onDropboxError() {
            }
        });

        new RequestMailsTask(mService, filesRepository, userService.getUser().getEmail(), new RequestMailsTask.MailCallback() {
            @Override
            public void onComplete(List<ItemFile> pendingFiles) {
                if (!pendingFiles.isEmpty()) {
                    addFiles(pendingFiles);
                }
            }
        }).execute();

        mainView.setLoadingIndicator(true);
        processPendingFiles();
        mainView.setLoadingIndicator(false);

    }

    private void addFiles(List<ItemFile> files){
        for(ItemFile file: files){
            addFile(file);
        }
    }

    private void addFile(ItemFile file){
        if (!pendingFiles.contains(file)) {
            pendingFiles.add(file);
            mainView.addPendingFile(file);
        }
    }

    private void processPendingFiles() {
        if (pendingFiles.isEmpty()) {
            mainView.showNoPendingFiles();
        } else {
            mainView.showPendingFiles(filesRepository.getPendingFiles());
        }
    }

    @Override
    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }
}
