package ipleiria.project.add.view.settings;

import android.support.annotation.NonNull;
import android.util.Log;

import com.dropbox.core.android.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ipleiria.project.add.Dropbox.DropboxCallback;
import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.exceptions.HttpErrorException;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.DropboxService;
import ipleiria.project.add.data.source.MEOCloudService;
import ipleiria.project.add.data.source.UserService;

import static ipleiria.project.add.data.source.UserService.AUTH_TAG;

/**
 * Created by Lisboa on 06-May-17.
 */

public class SettingsPresenter implements SettingsContract.Presenter {

    private static final String TAG = "SETTINGS_PRESENTER";

    static final int REQUEST_TAKE_PHOTO = 2002;

    private final UserService userService;
    private final DropboxService dropboxService;
    private final MEOCloudService meoCloudService;
    private final SettingsContract.View settingsView;

    // both Dropbox and MEO Auth save their tokens when this activity resumes
    // and since the tokens no longer exist in the preferences and the clients
    // aren't initialized when returning to the activity after loging in the save***Token
    // method is run and since the Auth.result still has the token the app assumes the user actually
    // logged in again with an invalid token
    // this can be fixed with MEO but Dropbox would require change to their code which
    // could cause issues in the future
    private boolean loginIntent = false;

    public SettingsPresenter(@NonNull SettingsContract.View settingsView) {
        this.userService = UserService.getInstance();
        this.dropboxService = DropboxService.getInstance(userService.getDropboxToken());
        this.meoCloudService = MEOCloudService.getInstance(userService.getMeoCloudToken());

        this.settingsView = settingsView;
        this.settingsView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
        updateServicesStatus();
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
                Log.d(AUTH_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            }
            updateAccountInfo();
        }
    };


    private void updateAccountInfo() {
        User user = userService.getUser();
        if (user.isAnonymous()) {
            settingsView.setAnonymousUserInfo();
        } else {
            settingsView.setUserInfo(userService.getUser());
        }
    }

    @Override
    public void updateServicesStatus() {
        if (MEOCloudClient.isClientInitialized()) {
            settingsView.setMEOCloudStatus(true);
        }

        if (DropboxClientFactory.isClientInitialized()) {
            settingsView.setDropboxStatus(true);
        }
    }

    @Override
    public void onDropboxAction() {
        if (DropboxClientFactory.isClientInitialized()) {
            settingsView.showDropboxLogoutDialog();
        } else {
            settingsView.showDropboxLogin();
        }
    }

    @Override
    public void onMEOCloudAction() {
        if (MEOCloudClient.isClientInitialized()) {
            settingsView.showMEOCloudLogoutDialog();
        } else {
            settingsView.showMEOCloudLogin();
        }
    }

    @Override
    public void onActivityResume() {
        if (loginIntent) {
            if (!MEOCloudClient.isClientInitialized()) {
                String token = MEOCloudAPI.getOAuth2Token();
                meoCloudService.init(token);
                userService.setMEOCloudToken(token);
            }

            if (!DropboxClientFactory.isClientInitialized()) {
                String token = Auth.getOAuth2Token();
                dropboxService.init(token);
                userService.setDropboxToken(token);
            }

            updateServicesStatus();
            loginIntent = false;
        }
    }

    @Override
    public void signOutMEOCloud() {
        meoCloudService.revokeToken(new MEOCallback() {
            @Override
            public void onComplete(Object result) {
                settingsView.setMEOCloudStatus(false);
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                Log.e(TAG, httpE.getMessage(), httpE);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        });
    }

    @Override
    public void signOutDropbox() {
        dropboxService.revokeToken(new DropboxCallback<Void>() {
            @Override
            public void onComplete(Void result) {
                settingsView.setDropboxStatus(false);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        });
    }

    @Override
    public void setLoginIntention(boolean loginIntent) {
        this.loginIntent = loginIntent;
    }
}
