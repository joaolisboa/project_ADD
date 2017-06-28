package ipleiria.project.add.view.settings;

import android.support.annotation.NonNull;
import android.util.Log;

import com.dropbox.core.android.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.DrawerView;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.dropbox.DropboxCallback;
import ipleiria.project.add.dropbox.DropboxClientFactory;
import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudAPI;
import ipleiria.project.add.meocloud.MEOCloudClient;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
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

    private final UserService userService;
    private final DropboxService dropboxService;
    private final MEOCloudService meoCloudService;
    private final CategoryRepository categoryRepository;

    private final SettingsContract.View settingsView;
    private final DrawerView drawerView;

    private final FirebaseAuth firebaseAuth;

    // both Dropbox and MEO Auth save their tokens when this activity resumes
    // and since the tokens no longer exist in the preferences and the clients
    // aren't initialized when returning to the activity after loging in the save***Token
    // method is run and since the Auth.result still has the token the app assumes the user actually
    // logged in again with an invalid token
    // this can be fixed with MEO but Dropbox would require change to their code
    private boolean loginIntent = false;

    // when the dimensions are edited we need to track if all are valid
    // before altering the dimension weight itself so we use a map to keep track of the changes
    private Map<Dimension, Integer> weightsChangeValid;

    public SettingsPresenter(SettingsContract.View settingsView, DrawerView drawerView,
                             UserService userService, CategoryRepository categoryRepository) {
        this.userService = userService;
        this.dropboxService = DropboxService.getInstance(userService.getDropboxToken());
        this.meoCloudService = MEOCloudService.getInstance(userService.getMeoCloudToken());
        this.categoryRepository = categoryRepository;

        this.drawerView = drawerView;
        this.settingsView = settingsView;
        this.settingsView.setPresenter(this);

        this.firebaseAuth = FirebaseAuth.getInstance();

        this.weightsChangeValid = new LinkedHashMap<>();
    }

    @Override
    public void subscribe() {
        firebaseAuth.addAuthStateListener(authStateListener);
        updateServicesStatus();

        categoryRepository.readData(new FilesRepository.Callback<List<Dimension>>() {
            @Override
            public void onComplete(List<Dimension> dimensions) {
                settingsView.setDimensionViews(dimensions, userService.getUser());
                // initialize map with default values
                for(Dimension dimension: dimensions){
                    weightsChangeValid.put(dimension, dimension.getWeight());
                }
            }

            @Override
            public void onError(Exception e) {
                // ignore
            }
        });
    }

    @Override
    public void unsubscribe() {
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(AUTH_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                updateAccountInfo();
            }
        }
    };


    private void updateAccountInfo() {
        User user = userService.getUser();
        if (user.isAnonymous()) {
            settingsView.setAnonymousUserInfo(user);
        } else {
            settingsView.setUserInfo(user);
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
    public boolean isWeightValid(Dimension dimension, int weightInserted) {
        // keep track of all changes to weights before commiting to the dimension
        weightsChangeValid.put(dimension, weightInserted);
        return weightInserted >= dimension.getMinWeight() && weightInserted <= dimension.getMaxWeight();
    }

    @Override
    public boolean areWeightsValid() {
        // make sure all weights are valid
        for(Map.Entry<Dimension, Integer> entry: weightsChangeValid.entrySet()){
            int weight = entry.getValue();
            Dimension dimension = entry.getKey();
            if(weight < dimension.getMinWeight() || weight > dimension.getMaxWeight()){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSumOfWeightsValid() {
        // make sure the sum of all weights is equal to 100
        int sum = 0;
        for(Map.Entry<Dimension, Integer> entry: weightsChangeValid.entrySet()){
             sum += entry.getValue();
        }
        return sum == 100;
    }

    @Override
    public void setWeight(Dimension dimension, int weightInserted) {
        userService.getUser().setDimensionWeightLimit(dimension.getDbKey(), weightInserted);
        userService.saveUserInfo();
    }

    @Override
    public void commitDimensionWeightChanges() {
        for(Map.Entry<Dimension, Integer> entry: weightsChangeValid.entrySet()){
            userService.getUser().setDimensionWeightLimit(entry.getKey().getDbKey(), entry.getValue());
        }
        userService.saveUserInfo();
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
                userService.saveMEOCloudToken(token);
            }

            if (!DropboxClientFactory.isClientInitialized()) {
                String token = Auth.getOAuth2Token();
                dropboxService.init(token);
                userService.saveDropboxToken(token);
            }

            updateServicesStatus();
            loginIntent = false;
        }
        drawerView.setUserInfo(UserService.getInstance().getUser());
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
                settingsView.setMEOCloudStatus(false);
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
