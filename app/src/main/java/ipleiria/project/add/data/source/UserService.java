package ipleiria.project.add.data.source;

import android.content.SharedPreferences;
import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.User;

import static ipleiria.project.add.FirebaseHandler.FIREBASE_UID_KEY;

/**
 * Created by Lisboa on 04-May-17.
 */

public class UserService {

    private static UserService INSTANCE = null;
    public static final String USER_DATA = "user_data_key";
    public static final String AUTH_TAG = "AUTHENTICATION";

    private User user;

    private FirebaseAuth firebaseAuth;

    private UserService(SharedPreferences preferences){
        this();
        String userUID = preferences.getString(FIREBASE_UID_KEY, null);
        user = new User(userUID);
    }

    private UserService(){
        this.firebaseAuth = FirebaseAuth.getInstance();
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

    public static UserService initUserInstance(SharedPreferences preferences){
        if (INSTANCE == null) {
            INSTANCE = new UserService(preferences);
        }
        return INSTANCE;
    }

    public void initUser(FirebaseUser firebaseUser){
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

        this.user = user;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public User getUser() {
        return user;
    }

    public Task<AuthResult> getAnonymousUser(){
        return firebaseAuth.signInAnonymously();
    }

}
