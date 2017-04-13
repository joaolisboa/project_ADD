package ipleiria.project.add;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.users.FullAccount;

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

public class SettingsActivity extends AppCompatActivity {

    public static final String DROPBOX_PREFS_KEY = "dropbox_access_token";
    public static final String MEO_PREFS_KEY = "meo_access_token";

    private SharedPreferences preferences;
    private ImageView meocloudState;
    private ImageView dropboxState;
    private LinearLayout dropboxL;
    private LinearLayout meoCloudL;

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

        dropboxL = (LinearLayout)findViewById(R.id.dropbox);
        meoCloudL = (LinearLayout)findViewById(R.id.meocloud);
        dropboxState = (ImageView)findViewById(R.id.dropbox_state);
        meocloudState = (ImageView)findViewById(R.id.meocloud_state);

        setButtonActions();
    }

    private void setButtonActions(){
        if(!DropboxClientFactory.isClientInitialized()){
            dropboxState.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.ic_link_black_64dp));
            dropboxL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Auth.startOAuth2Authentication(SettingsActivity.this, getString(R.string.dropbox_app_key));
                    loginIntent = true;
                }
            });
        }else{
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

        if(!MEOCloudClient.isClientInitialized()) {
            meocloudState.setImageDrawable(ContextCompat.getDrawable(SettingsActivity.this, R.drawable.ic_link_black_64dp));
            meoCloudL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MEOCloudAPI.startOAuth2Authentication(SettingsActivity.this, getString(R.string.meo_consumer_key));
                }
            });
        }else{
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

    public void updateView(){
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

    @Override
    protected void onResume() {
        super.onResume();

        if (!preferences.contains(MEO_PREFS_KEY)) {
            saveMeoToken();
        }

        if (!preferences.contains(DROPBOX_PREFS_KEY) && loginIntent) {
            saveDropboxToken();
        }
    }

}
