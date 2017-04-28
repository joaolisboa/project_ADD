package ipleiria.project.add.Dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.SettingsActivity;

/**
 * Singleton instance of {@link DbxClientV2} and friends
 */
public class DropboxClientFactory {

    private static DbxClientV2 sDbxClient;

    public static void init(String accessToken) {
        if (sDbxClient == null) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("Projecto ADD v1.0")
                .withHttpRequestor(OkHttp3Requestor.INSTANCE)
                .build();

            sDbxClient = new DbxClientV2(requestConfig, accessToken);
        }
    }

    public static DbxClientV2 getClient() {
        if (sDbxClient == null) {
            throw new IllegalStateException("Client not initialized.");
        }
        return sDbxClient;
    }

    public static boolean isClientInitialized(){
        return sDbxClient != null;
    }

    public static void revokeToken(final Context context){
        new DropboxRevokeToken(sDbxClient, new DropboxRevokeToken.Callback(){
            @Override
            public void onComplete() {
                SharedPreferences prefs = ApplicationData.getInstance().getSharedPreferences();
                prefs.edit().remove(SettingsActivity.DROPBOX_PREFS_KEY).apply();
                sDbxClient = null;
                ((SettingsActivity)context).updateView();
            }

            @Override
            public void onError(Exception e) {
                Log.e("DROPBOX_REVOKE_ERROR", e.getMessage(), e);
            }
        }).execute();

    }
}
