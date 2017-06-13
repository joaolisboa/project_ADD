package ipleiria.project.add.data.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ipleiria.project.add.Application;
import ipleiria.project.add.Callbacks;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.NetworkState;
import ipleiria.project.add.data.model.User;

/**
 * Created by Lisboa on 04-May-17.
 */

public class UserService {

    private static UserService INSTANCE = null;
    private static final String TAG = "USER_SERVICE";
    public static final String AUTH_TAG = "AUTHENTICATION";

    private static final String USER_REF = "users";
    public static final String USER_DATA_KEY = "services";
    public static final String USER_UID_KEY = "user_uid";
    public static final String DROPBOX_PREFS_KEY = "dropbox_access_token";
    public static final String MEO_PREFS_KEY = "meo_access_token";

    private User user;

    private String dropboxToken;
    private String meoCloudToken;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference;

    private SharedPreferences preferences;

    private UserService() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.preferences = Application.getAppContext().getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE);

        String userUid = preferences.getString(USER_UID_KEY, null);
        if(userUid != null) {
            this.userDatabaseReference = FirebaseDatabase.getInstance().getReference().child(USER_REF).child(userUid);
            this.userDatabaseReference.keepSynced(true);
            this.user = new User(userUid);
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

    public void initUser(FirebaseUser firebaseUser, final Callbacks.BaseCallback<User> callback) {
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
        user.setAnonymous(firebaseUser.isAnonymous());
        user.setPhotoUrl(profileUri);
        user.setName(displayName);
        this.user = user;

        userDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setAdditionalInfo(dataSnapshot);
                if(callback != null) {
                    callback.onComplete(getUser());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read user info from firebase db", databaseError.toException());
            }
        });

        preferences.edit().putString(USER_UID_KEY, firebaseUser.getUid()).apply();
    }

    @SuppressLint("SimpleDateFormat")
    private void setAdditionalInfo(DataSnapshot snapshot){
        user.setName((String)snapshot.child("name").getValue());
        user.setDepartment((String)snapshot.child("department").getValue());

        EvaluationPeriod mostRecentStart = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        for(DataSnapshot periodsSnapshot: snapshot.child("evaluationPeriods").getChildren()){
            EvaluationPeriod evaluationPeriod = new EvaluationPeriod(periodsSnapshot.getKey());
            try {
                Date startDate = dateFormat.parse((String)periodsSnapshot.child("startDate").getValue());
                evaluationPeriod.setStartDate(startDate);
                evaluationPeriod.setEndDate(dateFormat.parse((String)periodsSnapshot.child("endDate").getValue()));
                if(mostRecentStart == null || startDate.compareTo(mostRecentStart.getStartDate()) > 0){
                    mostRecentStart = evaluationPeriod;
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date from firebase", e);
            }

            user.addEvaluationPeriod(evaluationPeriod);
        }

        try {
            EvaluationPeriod evaluationPeriod = new EvaluationPeriod("test_dbkey");
            evaluationPeriod.setStartDate(dateFormat.parse("01-09-2013"));
            evaluationPeriod.setEndDate(dateFormat.parse("31-08-2016"));
            user.addEvaluationPeriod(evaluationPeriod);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ItemsRepository.getInstance().initUser(getUser().getUid());
        ItemsRepository.getInstance().initCurrentPeriod(mostRecentStart);
    }

    @SuppressLint("SimpleDateFormat")
    public void saveUserInfo(){
        userDatabaseReference.child("name").setValue(user.getName());
        userDatabaseReference.child("department").setValue(user.getDepartment());

        DatabaseReference ref = userDatabaseReference.child("evaluationPeriods");
        Map<String, Object> periodsMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        for(EvaluationPeriod evaluationPeriod: user.getEvaluationPeriods()){
            Map<String, Object> period = new HashMap<>();
            if (evaluationPeriod.getDbKey() == null || evaluationPeriod.getDbKey().isEmpty()) {
                ref = ref.push();
                evaluationPeriod.setDbKey(ref.getKey());
            } else {
                ref = ref.child(evaluationPeriod.getDbKey());
            }
            period.put("startDate", dateFormat.format(evaluationPeriod.getStartDate()));
            period.put("endDate", dateFormat.format(evaluationPeriod.getEndDate()));
            periodsMap.put(evaluationPeriod.getDbKey(), period);
        }
        userDatabaseReference.child("evaluationPeriods").setValue(periodsMap);
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
