package ipleiria.project.add.view.google_sign_in;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

import android.Manifest;

import ipleiria.project.add.R;
import ipleiria.project.add.view.*;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

/**
 * Created by J on 09/05/2017.
 */

public class GoogleSignInFragment extends Fragment implements GoogleSignInContract.View,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "GoogleSignInActivity";
    public static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    private GoogleSignInContract.Presenter presenter;

    private ProgressDialog progressDialog;
    private TextView statusTextView;
    private SignInButton signInButton;
    private LinearLayout signOutLayout;

    public GoogleSignInFragment() {
    }

    public static GoogleSignInFragment newInstance() {
        return new GoogleSignInFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.google_frag, container, false);

        // Views
        statusTextView = (TextView) root.findViewById(R.id.status);
        signOutLayout = (LinearLayout) root.findViewById(R.id.sign_out_and_disconnect);

        // Button listeners
        signInButton = (SignInButton) root.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);
        root.findViewById(R.id.sign_out_button).setOnClickListener(this);

        SignInButton signInButton = (SignInButton) root.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.subscribe();
        presenter.buildGoogleClient(getActivity(), this, getString(R.string.default_web_client_id));
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.unsubscribe();
        hideProgressDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setPresenter(GoogleSignInContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showUnauthenticatedUser() {
        statusTextView.setText(R.string.signed_out_text);
        signInButton.setVisibility(View.VISIBLE);
        signOutLayout.setVisibility(View.GONE);
    }

    @Override
    public void showAuthenticatedUser(String displayName) {
        statusTextView.setText("signed in: " + displayName);
        signInButton.setVisibility(View.GONE);
        signOutLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void signIn(GoogleApiClient googleApiClient){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void requestContactsPermission(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int hasWriteContactsPermission = getContext().checkSelfPermission(Manifest.permission.WRITE_CONTACTS);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.WRITE_CONTACTS},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int i = 0; i < permissions.length; i++) {
            if(permissions[i].equals(Manifest.permission.WRITE_CONTACTS)) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    presenter.createContactAlias();
                }
            }
        }
    }

    @Override
    public void requestNewContactCreation(ArrayList<ContentProviderOperation> ops) {
        try {
            getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Failed to create contact");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                presenter.signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Signing out of Google means data will no longer be synced across devices.")
                .setTitle("Confirm");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                presenter.downgradeAccount();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
