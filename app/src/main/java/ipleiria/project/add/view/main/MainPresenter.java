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
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Application;
import ipleiria.project.add.Callbacks;
import ipleiria.project.add.DrawerView;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.RequestMailsTask;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.view.categories.CategoriesActivity;

import static ipleiria.project.add.data.source.UserService.AUTH_TAG;
import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PHOTO;
import static ipleiria.project.add.view.google_sign_in.GoogleSignInPresenter.SCOPES;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM;

/**
 * Created by Lisboa on 05-May-17.
 */

class MainPresenter implements MainContract.Presenter {

    private static final String TAG = "MAIN_PRESENTER";

    static final int REQUEST_TAKE_PHOTO = 2002;

    private final UserService userService;
    private final MainContract.View mainView;
    private final DrawerView drawerView;

    private GoogleApiClient googleApiClient;
    private Gmail mService;

    private final FilesRepository filesRepository;
    private List<PendingFile> pendingFiles;
    private List<PendingFile> selectedPendingFiles;

    private File sharedFile;
    private Uri photoUri;
    // ensure we want to sign to avoid consequent calls to authStateListener
    private boolean authFlag = false;

    MainPresenter( UserService userService, MainContract.View mainView, DrawerView drawerView) {
        this.userService = userService;
        this.mainView = mainView;
        this.mainView.setPresenter(this);
        this.drawerView = drawerView;

        this.filesRepository = FilesRepository.getInstance();
        this.pendingFiles = new LinkedList<>();
        this.selectedPendingFiles = new LinkedList<>();
    }

    @Override
    public void subscribe() {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

        filesRepository.getRemotePendingFiles(new FilesRepository.ServiceCallback<List<PendingFile>>() {
            @Override
            public void onMEOComplete(List<PendingFile> result) {
                addFiles(result);
            }

            @Override
            public void onMEOError() {
            }

            @Override
            public void onDropboxComplete(List<PendingFile> result) {
                addFiles(result);
            }

            @Override
            public void onDropboxError() {
            }
        });

        // android doesn't seem to ever delete temp file or files with deleteOnExit()
        // so when activity resumes if a file was shared we delete it
        // ps: in case the user uses the app offline or already has a local file
        // we only delete the file if it start with tmp_ since that is the prefix added when
        // downloading the file
        // TODO: 08-Jun-17 Delete temp file only when leaving the activity(avoid repeated downloads(faster reopening) if user wants to review the file)
        if (sharedFile != null && sharedFile.exists() && sharedFile.getName().startsWith("tmp_")) {
            sharedFile.delete();
            sharedFile = null;
        }
        pendingFiles = filesRepository.getPendingFiles();
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
                    UserService.getInstance().initUser(user, new Callbacks.BaseCallback<User>() {
                        @Override
                        public void onComplete(User user) {
                            drawerView.setUserInfo(UserService.getInstance().getUser());
                        }
                    });
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
                    UserService.getInstance().initUser(user, new Callbacks.BaseCallback<User>() {
                        @Override
                        public void onComplete(User user) {
                            drawerView.setUserInfo(user);
                        }
                    });
                } else {
                    Log.e(AUTH_TAG, "onAuthStateChanged:auth_failed", task.getException());
                }
            }
        });
    }

    @Override
    public void result(int requestCode, int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                mainView.addPhotoURIToItems(photoUri.toString());
            }else if(requestCode == REQUEST_ADD_NEW_ITEM){
                mainView.showItemAddedMessage();
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
                public void onComplete(List<PendingFile> result) {
                    addFiles(result);
                }
            }).execute();
        }

    }

    @Override
    public void createThumbnail(final PendingFile file) {
        filesRepository.getPendingFileThumbnail(file.getFilename(), new FilesRepository.BaseCallback<File>() {
            @Override
            public void onComplete(File result) {
                mainView.setFileThumbnail(file, result);
            }
        });
    }

    @Override
    public void onFileClicked(final PendingFile clickedFile) {
        if (clickedFile.getFilename().substring(clickedFile.getFilename().lastIndexOf(".") + 1).equals("eml")) {
            File email = new File(Application.getAppContext().getFilesDir(), clickedFile.getFilename());
            sharedFile = email;
            mainView.openFileShare(filesRepository.getRelativePath(email));
        } else {
            mainView.showLoadingIndicator();
            filesRepository.getPendingFile(clickedFile, new FilesRepository.Callback<File>() {
                @Override
                public void onComplete(File result) {
                    mainView.hideLoadingIndicator();
                    sharedFile = result;
                    mainView.openFileShare(filesRepository.getRelativePath(result));
                }

                @Override
                public void onError(Exception e) {
                    mainView.hideLoadingIndicator();
                    // show error message for file not found
                    Log.d(TAG, "File not found - missing locally and/or remotely");
                    Log.e(TAG, e.getMessage(), e);
                }
            });
        }
    }

    @Override
    public boolean isFileSelected(PendingFile file) {
        return selectedPendingFiles.contains(file);
    }

    @Override
    public void onFileSelected(PendingFile file) {
        if(isFileSelected(file)){
            selectedPendingFiles.remove(file);
        }else{
            selectedPendingFiles.add(file);
        }

        if(!selectedPendingFiles.isEmpty()){
            mainView.setSelectMode(true);
            mainView.showAddToItemOption();
        }else{
            mainView.setSelectMode(false);
            mainView.hideAddToItemOption();
        }
    }

    @Override
    public void onFileRemoved(PendingFile file) {
        mainView.removePendingFile(file);
        // delete the file from selected list in case the file was deleted while selected
        selectedPendingFiles.remove(file);
    }

    @Override
    public void addPendingFilesToItems() {
        mainView.addFilesToItems(new ArrayList<>(selectedPendingFiles));
        pendingFiles.removeAll(selectedPendingFiles);
        selectedPendingFiles = new LinkedList<>();
    }

    @Override
    public void refreshPendingFiles() {
        mainView.showPendingFiles(filesRepository.getPendingFiles());
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
                buildGmailService(acct);
            }
        }
    }

    private void buildGmailService(GoogleSignInAccount acct){
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
            public void onComplete(List<PendingFile> result) {
                addFiles(result);
            }
        }).execute();
    }

    @Override
    public void onSwipeRefresh() {
        mainView.showLoadingIndicator();
        pendingFiles = new LinkedList<>();
        filesRepository.getRemotePendingFiles(new FilesRepository.ServiceCallback<List<PendingFile>>() {
            @Override
            public void onMEOComplete(List<PendingFile> result) {
                addFiles(result);
            }

            @Override
            public void onMEOError() {
            }

            @Override
            public void onDropboxComplete(List<PendingFile> result) {
                addFiles(result);
            }

            @Override
            public void onDropboxError() {
            }
        });

        if(mService == null){
            // will make sure credentials exist, sign in and build the gmail service
            // this fixes an issue where when returning to the app after signing into google
            // causes a crash when refreshing because the service wasn't initialized
            checkForCachedCredentials();
        }

        processPendingFiles();
        mainView.hideLoadingIndicator();
    }

    private void addFiles(List<PendingFile> files){
        filesRepository.addPendingFiles(files);
        for(PendingFile file: files){
            addFile(file);
        }
        processPendingFiles();
    }

    private void addFile(PendingFile file){
        if (!pendingFiles.contains(file)) {
            pendingFiles.add(file);
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
