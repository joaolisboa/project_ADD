package ipleiria.project.add;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.users.FullAccount;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxGetAccount;
import ipleiria.project.add.MEOCloud.Data.Account;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOGetAccount;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;

public class ServiceChooserActivity extends AppCompatActivity {

    private static final String DROPBOX_PREFS_KEY = "dropbox_access_token";
    private static final String MEO_PREFS_KEY = "meo_access_token";

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_chooser);

        // remove tokens while testing
        preferences = getSharedPreferences("services", MODE_PRIVATE);

        if (DropboxClientFactory.isClientInitialized()) {
            findViewById(R.id.sign_in_dropbox).setEnabled(false);
        }

        if (MEOCloudClient.isClientInitialized()) {
            findViewById(R.id.sign_in_meo).setEnabled(false);
        }

    }

    public void signInDropbox(View view) {
        // open authentication for Dropbox
        Auth.startOAuth2Authentication(ServiceChooserActivity.this, getString(R.string.dropbox_app_key));
    }

    public void signInMEO(View view) {
        MEOCloudAPI.startOAuth2Authentication(ServiceChooserActivity.this, getString(R.string.meo_consumer_key));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!preferences.contains(MEO_PREFS_KEY)) {
            saveMeoToken();
        }

        if (!preferences.contains(DROPBOX_PREFS_KEY)) {
            saveDropboxToken();
        }
    }

    private void saveMeoToken() {
        String accessToken = MEOCloudAPI.getOAuth2Token();
        if (accessToken != null) {
            MEOCloudClient.init(accessToken);
            preferences.edit().putString(MEO_PREFS_KEY, accessToken).apply();
            findViewById(R.id.sign_in_meo).setEnabled(false);

            new MEOGetAccount(new MEOCallback<Account>() {

                @Override
                public void onComplete(MEOCloudResponse<Account> result) {
                    Account account = result.getResponse();
                    Toast.makeText(ServiceChooserActivity.this,
                                "Connected account: " + account.getEmail(),
                                Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onRequestError(HttpErrorException httpE) {

                }

                @Override
                public void onError(Exception e) {

                }
            }).execute();
        }
    }

    private void saveDropboxToken() {
        String accessToken = Auth.getOAuth2Token();
        if (accessToken != null) {
            preferences.edit().putString(DROPBOX_PREFS_KEY, accessToken).apply();
            findViewById(R.id.sign_in_dropbox).setEnabled(false);
            DropboxClientFactory.init(accessToken);
            new DropboxGetAccount(DropboxClientFactory.getClient(), new DropboxGetAccount.Callback() {
                @Override
                public void onComplete(FullAccount result) {
                    Toast.makeText(ServiceChooserActivity.this, "Connected account: " + result.getEmail(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(getClass().getName(), "Failed to get account details", e);
                }
            }).execute();
        }
    }
}
