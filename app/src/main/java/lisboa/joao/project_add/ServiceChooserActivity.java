package lisboa.joao.project_add;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2Authorization;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import lisboa.joao.project_add.Dropbox.DropboxClientFactory;
import lisboa.joao.project_add.Dropbox.GetCurrentAccountTask;
import lisboa.joao.project_add.Dropbox.UploadFileTask;
import lisboa.joao.project_add.MEOCloud.Authentication;
import lisboa.joao.project_add.MEOCloud.MEOAuth;
import lisboa.joao.project_add.MEOCloud.MEOCloudAPI;

public class ServiceChooserActivity extends Activity {

    private static final String DROPBOX_PREFS_KEY = "dropbox_access_token";
    private static final String MEO_PREFS_KEY = "meo_access_token";

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_chooser);

        preferences = getSharedPreferences("services", MODE_PRIVATE);
        if(preferences.contains(DROPBOX_PREFS_KEY)){
            findViewById(R.id.sign_in_dropbox).setEnabled(false);
        }

        if(preferences.contains(MEO_PREFS_KEY)){
            findViewById(R.id.sign_in_meo).setEnabled(false);
        }

    }

    public void signInDropbox(View view){
        // open browser authentication for Dropbox
        Auth.startOAuth2Authentication(ServiceChooserActivity.this, getString(R.string.dropbox_app_key));
    }

    public void signInMEO(View view){
        OAuth20Service service = new ServiceBuilder()
                .apiKey(getString(R.string.meo_consumer_key))
                .apiSecret(getString(R.string.meo_app_secret))
                .build(MEOCloudAPI.instance());

        MEOCloudAPI.instance().setConsumerKey(getString(R.string.meo_consumer_key));



        //// TODO: 15-Mar-17
        //MEOAuth.startOAuth2Authentication(ServiceChooserActivity.this, getString(R.string.meo_consumer_key));
        //String token = MEOAuth.getOAuth2Token();
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(!preferences.contains(MEO_PREFS_KEY)){

        }

        if(!preferences.contains(DROPBOX_PREFS_KEY)){
            saveDropboxToken();
        }
    }

    private void saveDropboxToken(){
        String accessToken = Auth.getOAuth2Token();
        if(accessToken != null){
            preferences.edit().putString(DROPBOX_PREFS_KEY, accessToken).apply();
            findViewById(R.id.sign_in_dropbox).setEnabled(false);
            DropboxClientFactory.init(accessToken);
            new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback(){
                @Override
                public void onComplete(FullAccount result){
                    Toast.makeText(ServiceChooserActivity.this, "Connected account: " + result.getEmail(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e){
                    Log.e(getClass().getName(), "Failed to get account details", e);
                }
            }).execute();
            new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback(){
                @Override
                public void onUploadComplete(FileMetadata result){
                    Toast.makeText(ServiceChooserActivity.this, "Connected account: " + result.getName(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e){
                    Log.e(getClass().getName(), "Failed to get account details", e);
                }
            }).execute("test");
        }
    }
}
