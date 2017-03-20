package lisboa.joao.project_add;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;

import lisboa.joao.project_add.Dropbox.DropboxClientFactory;
import lisboa.joao.project_add.Dropbox.GetCurrentAccountTask;
import lisboa.joao.project_add.Dropbox.UploadFileTask;
import lisboa.joao.project_add.MEOCloud.MEOCloudAPI;

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
        preferences.edit().remove("dropbox_access_token").apply();
        preferences.edit().remove("meo_access_token").apply();

        if(preferences.contains(DROPBOX_PREFS_KEY)){
            findViewById(R.id.sign_in_dropbox).setEnabled(false);
        }

        if(preferences.contains(MEO_PREFS_KEY)){
            findViewById(R.id.sign_in_meo).setEnabled(false);
        }

    }

    public void signInDropbox(View view){
        // open authentication for Dropbox
        Auth.startOAuth2Authentication(ServiceChooserActivity.this, getString(R.string.dropbox_app_key));
    }

    public void signInMEO(View view){
        MEOCloudAPI.startOAuth2Authentication(ServiceChooserActivity.this, getString(R.string.meo_consumer_key));
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(!preferences.contains(MEO_PREFS_KEY)){
            saveMeoToken();
        }

        if(!preferences.contains(DROPBOX_PREFS_KEY)){
            saveDropboxToken();
        }
    }

    private void saveMeoToken(){
        String accessToken = MEOCloudAPI.getOAuth2Token(ServiceChooserActivity.this);
        if(accessToken != null){
            preferences.edit().putString(MEO_PREFS_KEY, accessToken).apply();
            Toast.makeText(ServiceChooserActivity.this, "Connected account: " + accessToken, Toast.LENGTH_SHORT).show();
            findViewById(R.id.sign_in_meo).setEnabled(false);
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
