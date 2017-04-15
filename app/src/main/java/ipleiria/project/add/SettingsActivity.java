package ipleiria.project.add;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.audiofx.BassBoost;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.users.FullAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.List;
import java.util.Map;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxGetAccount;
import ipleiria.project.add.MEOCloud.Data.Account;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOGetAccount;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Utils.CircleTransformation;

public class SettingsActivity extends AppCompatActivity {

    public static final String DROPBOX_PREFS_KEY = "dropbox_access_token";
    public static final String MEO_PREFS_KEY = "meo_access_token";

    private SharedPreferences preferences;
    private ImageView meocloudState;
    private ImageView dropboxState;
    private LinearLayout dropboxL;
    private LinearLayout meoCloudL;

    private ImageView profileImageView;

    // both Dropbox and MEO Auth save their tokens when this activity resumes
    // and since the tokens no longer exist in the preferences and the clients
    // aren't initialized when returning to the activity after loging in the save***Token
    // method is run and since the Auth.result still has the token the app assumes the user actually
    // logged in again with an invalid token
    // this can be fixed with MEO but Dropbox would require change to their code which
    // could cause issues in the future
    private boolean loginIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("services", MODE_PRIVATE);

        profileImageView = (ImageView) findViewById(R.id.profile_pic);


        /*((TextView)findViewById(R.id.account_name)).setText(ApplicationData.getInstance().getDisplayName());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(!user.isAnonymous()){
            ((TextView)findViewById(R.id.account_description)).setText(getString(R.string.account_syncing_status, user.getEmail()));
        }*/

        dropboxL = (LinearLayout) findViewById(R.id.dropbox);
        meoCloudL = (LinearLayout) findViewById(R.id.meocloud);
        dropboxState = (ImageView) findViewById(R.id.dropbox_state);
        meocloudState = (ImageView) findViewById(R.id.meocloud_state);

        setButtonActions();
    }

    private void setButtonActions() {
        if (!DropboxClientFactory.isClientInitialized()) {
            dropboxState.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.ic_link_black_64dp));
            dropboxL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Auth.startOAuth2Authentication(SettingsActivity.this, getString(R.string.dropbox_app_key));
                    loginIntent = true;
                }
            });
        } else {
            dropboxState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check_black_64dp));
            loginIntent = false;
            dropboxL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setMessage("Do you want to disconnect Dropbox?")
                            .setTitle("Confirm");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DropboxClientFactory.revokeToken(SettingsActivity.this);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.create().show();
                }
            });
        }

        if (!MEOCloudClient.isClientInitialized()) {
            meocloudState.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.ic_link_black_64dp));
            meoCloudL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MEOCloudAPI.startOAuth2Authentication(SettingsActivity.this, getString(R.string.meo_consumer_key));
                }
            });
        } else {
            meocloudState.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check_black_64dp));
            meoCloudL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setMessage("Do you want to disconnect MEO Cloud?")
                            .setTitle("Confirm");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MEOCloudClient.revokeToken(SettingsActivity.this);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, null);
                    builder.create().show();
                }
            });
        }
    }

    public void updateView() {
        setButtonActions();
    }

    public void saveMeoToken() {
        String accessToken = MEOCloudAPI.getOAuth2Token();
        if (accessToken != null) {
            preferences.edit().putString(MEO_PREFS_KEY, accessToken).apply();
            MEOCloudClient.init(accessToken);
            setButtonActions();
        }
    }

    public void saveDropboxToken() {
        String accessToken = Auth.getOAuth2Token();
        if (accessToken != null) {
            preferences.edit().putString(DROPBOX_PREFS_KEY, accessToken).apply();
            DropboxClientFactory.init(accessToken);
            setButtonActions();
        }
    }

    public void openListEmailActivity(View view) {
        startActivity(new Intent(SettingsActivity.this, ListEmailActivity.class));
    }

    public void googleSignIn(View view) {
        startActivity(new Intent(SettingsActivity.this, GoogleSignInActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!preferences.contains(MEO_PREFS_KEY)) {
            saveMeoToken();
        }

        if (!preferences.contains(DROPBOX_PREFS_KEY) && loginIntent) {
            saveDropboxToken();
        }

        ((TextView) findViewById(R.id.num_emails)).setText(getString(R.string.number_of_emails,
                ApplicationData.getInstance().getEmails().size()));

        ((TextView) findViewById(R.id.account_name)).setText(ApplicationData.getInstance().getDisplayName());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (!user.isAnonymous()) {
            ((TextView) findViewById(R.id.account_description)).setText(getString(R.string.account_syncing_status, user.getEmail()));
            System.out.println("loading picasso profile pic");
            Picasso.with(SettingsActivity.this)
                    .load(ApplicationData.getInstance().getProfileUri())
                    .resize(100, 100)
                    .transform(new CircleTransformation())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImageView);
        } else {
            ((TextView) findViewById(R.id.account_description)).setText(getString(R.string.google_sign_in_helper));
            profileImageView.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.ic_profile_placeholder));
        }
    }
}
