package ipleiria.project.add.data.source;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.NetworkState;
import ipleiria.project.add.data.model.User;

/**
 * Created by Lisboa on 04-May-17.
 */

public class UserService {

    private static UserService INSTANCE = null;
    public static final String AUTH_TAG = "AUTHENTICATION";

    public static final String USER_DATA_KEY = "services";
    public static final String USER_UID_KEY = "user_uid";
    public static final String DROPBOX_PREFS_KEY = "dropbox_access_token";
    public static final String MEO_PREFS_KEY = "meo_access_token";

    private User user;

    private String dropboxToken;
    private String meoCloudToken;

    private FirebaseAuth firebaseAuth;

    private SharedPreferences preferences;

    private UserService() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.preferences = Application.getAppContext().getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE);

        if (NetworkState.isOnline()) {
            this.user = new User(preferences.getString(USER_UID_KEY, null));
        }

        dropboxToken = preferences.getString(DROPBOX_PREFS_KEY, null);
        meoCloudToken = preferences.getString(MEO_PREFS_KEY, null);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @return the {@link ItemsRepository} instance
     */
    public static UserService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserService();
        }
        return INSTANCE;
    }

    public void initUser(FirebaseUser firebaseUser) {
        User user = new User(firebaseUser.getUid());

        String displayName = firebaseUser.getDisplayName();
        Uri profileUri = firebaseUser.getPhotoUrl();

        for (UserInfo userInfo : firebaseUser.getProviderData()) {
            if (displayName == null && userInfo.getDisplayName() != null) {
                displayName = userInfo.getDisplayName();
            }
            if (profileUri == null && userInfo.getPhotoUrl() != null) {
                profileUri = userInfo.getPhotoUrl();
            }
        }
        if (firebaseUser.isAnonymous()) {
            displayName = "Anonymous";
        }

        user.setEmail(firebaseUser.getEmail());
        user.setPhoto_url(profileUri);
        user.setName(displayName);
        user.setAnonymous(firebaseUser.isAnonymous());

        preferences.edit().putString(USER_UID_KEY, firebaseUser.getUid()).apply();
        ItemsRepository.getInstance().initUser(firebaseUser.getUid());

        this.user = user;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public User getUser() {
        return user;
    }

    public Task<AuthResult> getAnonymousUser() {
        return firebaseAuth.signInAnonymously();
    }

    public String getDropboxToken() {
        return dropboxToken;
    }

    public void saveDropboxToken(String dropboxToken) {
        preferences.edit().putString(DROPBOX_PREFS_KEY, dropboxToken).apply();
        this.dropboxToken = dropboxToken;
    }

    public String getMeoCloudToken() {
        return meoCloudToken;
    }

    public void saveMEOCloudToken(String meoCloudToken) {
        preferences.edit().putString(MEO_PREFS_KEY, meoCloudToken).apply();
        this.meoCloudToken = meoCloudToken;
    }

    public void removeMEOCloudToken() {
        this.meoCloudToken = null;
        preferences.edit().remove(MEO_PREFS_KEY).apply();
    }

    public void removeDropboxToken() {
        this.dropboxToken = null;
        preferences.edit().remove(DROPBOX_PREFS_KEY).apply();
    }
}
