package ipleiria.project.add.MEOCloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Tasks.MEORevokeToken;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.SettingsActivity;

import static ipleiria.project.add.SettingsActivity.MEO_PREFS_KEY;

/**
 * Created by J on 03/04/2017.
 */

public class MEOCloudClient {

    private static String accessToken;

    public static void init(String accessToken){
        if(accessToken == null){
            accessToken = ApplicationData.getInstance().getSharedPreferences().getString(MEO_PREFS_KEY, null);
        }
        if(MEOCloudClient.accessToken == null || MEOCloudClient.accessToken.isEmpty()){
            MEOCloudClient.accessToken = accessToken;
        }
    }

    public static String getAccessToken() throws MissingAccessTokenException {
        if(accessToken == null || accessToken.isEmpty()){
            throw new MissingAccessTokenException();
        }
        return accessToken;
    }

    public static boolean isClientInitialized(){
        return accessToken != null && !accessToken.isEmpty();
    }

    public static void removeToken(Context context){
        SharedPreferences prefs = ApplicationData.getInstance().getSharedPreferences();
        prefs.edit().remove(MEO_PREFS_KEY).apply();
        accessToken = null;
        ((SettingsActivity)context).updateView();
    }

    public static void revokeToken(final Context context) {
        new MEORevokeToken(new MEOCallback<Void>() {
            @Override
            public void onComplete(Void result) {
                removeToken(context);
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                removeToken(context);
            }

            @Override
            public void onError(Exception e) {
                Log.e("REVOKE_TOKEN_ERROR", e.getMessage(), e);
            }
        }).execute();
    }
}
