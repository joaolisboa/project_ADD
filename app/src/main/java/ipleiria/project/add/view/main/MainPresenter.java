package ipleiria.project.add.view.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.view.items.ItemsActivity;

import static ipleiria.project.add.AddItemActivity.SENDING_PHOTO;
import static ipleiria.project.add.data.source.UserService.AUTH_TAG;

/**
 * Created by Lisboa on 05-May-17.
 */

public class MainPresenter implements MainContract.Presenter {

    static final int REQUEST_TAKE_PHOTO = 2002;

    private final UserService userService;
    private final MainContract.View mainView;
    private final MainContract.DrawerView drawerView;

    private Uri photoUri;
    private boolean authFlag = false;

    public MainPresenter(@NonNull UserService userService, @NonNull MainContract.View mainView, @NonNull MainContract.DrawerView drawerView){
        this.userService = userService;
        this.mainView = mainView;
        this.mainView.setPresenter(this);
        this.drawerView = drawerView;
    }

    @Override
    public void start(){
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    public void subscribe() {
        // TODO: 05-May-17 subscribe to user info ?
    }

    @Override
    public void unsubscribe() {
        if(authStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }
    }

    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                //prevent subsequent calls if auth fails and signs in anonymously below
                if(!authFlag) {
                    // user signed in successfully
                    Log.d(AUTH_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    authFlag = true;
                    UserService.getInstance().initUser(user);
                    drawerView.setUserInfo(UserService.getInstance().getUser());
                }
            }else{
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
                if(task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Log.d(AUTH_TAG, "onAuthStateChanged:signed_in_anonymously:" + user.getUid());
                    authFlag = true;
                    UserService.getInstance().initUser(user);
                    drawerView.setUserInfo(UserService.getInstance().getUser());
                }else{
                    Log.d(AUTH_TAG, "onAuthStateChanged:auth_failed");
                }
            }
        });
    }

    @Override
    public void result(int requestCode, int resultCode, Context context) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                Intent photo = new Intent(context, ItemsActivity.class);
                photo.putExtra("photo_uri", photoUri.toString());
                context.startActivity(photo.setAction(SENDING_PHOTO));
            }
        }
    }

    @Override
    public void setPhotoUri(Uri photoUri){
        this.photoUri = photoUri;
    }
}
