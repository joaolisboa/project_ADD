package ipleiria.project.add.MEOCloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import ipleiria.project.add.MEOCloud.exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.tasks.MEORevokeToken;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.view.settings.SettingsActivity;

import static ipleiria.project.add.data.source.UserService.MEO_PREFS_KEY;


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

    public static void destroyClient() {
        accessToken = null;
    }
}
