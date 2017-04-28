package ipleiria.project.add;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import ipleiria.project.add.Model.ApplicationData;

/**
 * Created by Lisboa on 13-Apr-17.
 */

public class GoogleSignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog mProgressDialog;
    private TextView mStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    ApplicationData.getInstance().setUserUID(user.getUid());
                    String displayName = user.getDisplayName();
                    Uri profileUri = user.getPhotoUrl();

                    // If the above were null, iterate the provider data
                    // and set with the first non null data
                    for (UserInfo userInfo : user.getProviderData()) {
                        if (displayName == null && userInfo.getDisplayName() != null) {
                            displayName = userInfo.getDisplayName();
                        }
                        if (profileUri == null && userInfo.getPhotoUrl() != null) {
                            profileUri = userInfo.getPhotoUrl();
                        }
                    }
                    if(displayName == null || displayName.isEmpty()){
                        displayName = "Anonymous";
                    }
                    ApplicationData.getInstance().setDisplayName(displayName);
                    ApplicationData.getInstance().setProfileUri(profileUri);
                    System.out.println("Starting new firebaseHandler instance");
                    // when signing out of Google write data to new firebase user
                    FirebaseHandler.getInstance().initReferences();
                    FirebaseHandler.getInstance().writeUserInfo();
                    FirebaseHandler.getInstance().writeItems();
                    FirebaseHandler.getInstance().writeEmails();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void downgrade(){
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            mAuth.signInAnonymously();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            updateUI(false);
                            if (status.isSuccess()) {
                                System.out.println("Google sign out successfull");
                            }
                        }
                    });
        }else{
            System.out.println("Firebase user null?");
        }
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GoogleSignInActivity.this);
        builder.setMessage("Signing out of Google means data will no longer be synced across devices.")
                .setTitle("Confirm");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                System.out.println("Downgrading account to anonymous");
                downgrade();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText("Signed out");

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isAnonymous()) {
                firebaseAuthWithGoogle(acct);
            }else{
                AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(GoogleSignInActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "reauthenticate user:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Toast.makeText(GoogleSignInActivity.this, "Reauthentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, task.getException().getMessage(), task.getException());
                        }
                    }
                });
            }
            mStatusTextView.setText("signed in: " + acct.getDisplayName());
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(GoogleSignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.e(TAG, task.getException().getMessage(), task.getException());
                        mAuth.signOut();
                        // user is already signed in another device
                        // so we need to log in with the credential
                        // instead of linking with the anonymous account
                        mAuth.signInWithCredential(credential);
                    } else {
                        System.out.println(mAuth.getCurrentUser().getUid());
                    }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
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
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
