package lisboa.joao.project_add;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.json.JSONException;
import org.json.JSONObject;

import lisboa.joao.project_add.Dropbox.DropboxClientFactory;
import lisboa.joao.project_add.Dropbox.GetCurrentAccountTask;
import lisboa.joao.project_add.Dropbox.UploadFileTask;
import lisboa.joao.project_add.MEOCloud.GetAccessToken;
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

    private void openWebView(){
        WebView web;

        final Dialog auth_dialog = new Dialog(ServiceChooserActivity.this);
        auth_dialog.setContentView(R.layout.auth_dialog);
        web = (WebView)auth_dialog.findViewById(R.id.webv);
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(MEOCloudAPI.AUTHORIZE_URL+"?response_type=code&client_id="+getString(R.string.meo_consumer_key));
        web.setWebViewClient(new WebViewClient() {

            boolean authComplete = false;
            Intent resultIntent = new Intent();

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);

            }
            String authCode;
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.contains("?code=") && !authComplete) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    Log.i("", "CODE : " + authCode);
                    authComplete = true;
                    resultIntent.putExtra("code", authCode);
                    ServiceChooserActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    setResult(Activity.RESULT_CANCELED, resultIntent);

                    preferences.edit().putString(DROPBOX_PREFS_KEY, authCode).apply();
                    auth_dialog.dismiss();
                    new TokenGet().execute();
                    Toast.makeText(getApplicationContext(),"Authorization Code is: " +authCode, Toast.LENGTH_SHORT).show();
                }else if(url.contains("error=access_denied")){
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    authComplete = true;
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();

                    auth_dialog.dismiss();
                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Authorize Learn2Crack");
        auth_dialog.setCancelable(true);
    }

    private class TokenGet extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        String Code;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ServiceChooserActivity.this);
            pDialog.setMessage("Contacting Google ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = preferences.getString(MEO_PREFS_KEY, "");
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            GetAccessToken jParser = new GetAccessToken();
            return jParser.gettoken(MEOCloudAPI.ACCESS_URL,Code,getString(R.string.meo_consumer_key),CLIENT_SECRET,REDIRECT_URI,GRANT_TYPE);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            if (json != null){

                try {

                    String tok = json.getString("access_token");
                    String expire = json.getString("expires_in");
                    String refresh = json.getString("refresh_token");

                    Log.d("Token Access", tok);
                    Log.d("Expire", expire);
                    Log.d("Refresh", refresh);
                    auth.setText("Authenticated");
                    //Access.setText("Access Token:"+tok+"nExpires:"+expire+"nRefresh Token:"+refresh);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }else{
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        }
    }
}
