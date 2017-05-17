package ipleiria.project.add.view.google_sign_in;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.ContactsContract;
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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;

import ipleiria.project.add.Manifest;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.data.source.UserService;

import static ipleiria.project.add.view.google_sign_in.GoogleSignInFragment.RC_SIGN_IN;

/**
 * Created by J on 09/05/2017.
 */

public class GoogleSignInPresenter implements GoogleSignInContract.Presenter {

    private static final String TAG = "GoogleSignInPresenter";

    private final GoogleSignInContract.View signInView;

    private GoogleApiClient googleApiClient;
    private FirebaseAuth firebaseAuth;

    public GoogleSignInPresenter(@NonNull GoogleSignInContract.View signInView) {
        this.signInView = signInView;
        this.firebaseAuth = FirebaseAuth.getInstance();
        signInView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void unsubscribe() {
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void buildGoogleClient(FragmentActivity fragment,
                                  GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener,
                                  String webClientID) {
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

        checkForCachedCredentials();
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
            signInView.showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    signInView.hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                UserService.getInstance().initUser(user);
                ItemsRepository.getInstance().moveItemsToNewUser();
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void signIn() {
        signInView.signIn(googleApiClient);
    }

    @Override
    public void downgradeAccount() {
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
            firebaseAuth.signInAnonymously();
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            signInView.showUnauthenticatedUser();
                            if (status.isSuccess()) {
                                Log.d(TAG, "Google sign out successfull");
                            }
                            signInView.hideProgressDialog();
                        }
                    });
        } else {
            // probably shouldn't happen
            // user opened app for the first time offline and got here
            Log.d(TAG, "Firebase user null?");
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isAnonymous()) {
                firebaseAuthWithGoogle(acct);
            } else {
                AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                firebaseAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "reauthenticate user:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.e(TAG, task.getException().getMessage(), task.getException());
                        }
                        signInView.hideProgressDialog();
                    }
                });
            }
            signInView.showAuthenticatedUser(acct.getDisplayName());
        } else {
            // Signed out, show unauthenticated UI.
            signInView.showUnauthenticatedUser();
        }
    }

    @Override
    public void createContactAlias() {
        String DisplayName = "Add ESTG";

        String[] emailAux = firebaseAuth.getCurrentUser().getEmail().split("@");
        String emailAlias = emailAux[0] + "+addestg@" + emailAux[1];

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        DisplayName).build());


        //------------------------------------------------------ Email
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailAlias)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .build());

        signInView.requestNewContactCreation(ops);
    }

    // user is anonymous and upgrading to a Google Account
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.e(TAG, task.getException().getMessage(), task.getException());
                            firebaseAuth.signOut();

                            // user is already signed in another device
                            // so we need to log in with the credential
                            // instead of linking with the anonymous account
                            firebaseAuth.signInWithCredential(credential);
                            signInView.requestContactsPermission();
                            // TODO: 07-May-17 delete previous anon user and his old data
                        } else {
                            Log.d(TAG, firebaseAuth.getCurrentUser().getUid());
                        }
                        signInView.hideProgressDialog();
                    }
                });
    }
}
