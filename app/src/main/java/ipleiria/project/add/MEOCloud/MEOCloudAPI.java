package ipleiria.project.add.MEOCloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import ipleiria.project.add.MEOCloud.Data.Account;

/**
 * Created by Lisboa on 14-Mar-17.
 */

public class MEOCloudAPI {

    static final String AUTHORIZE_URL = "https://meocloud.pt/oauth2/authorize?client_id=%s&response_type=token&state=%s";
    static final String ACCESS_URL = "https://meocloud.pt/oauth2/token";

    static final String API_ENDPOINT = "api.meocloud.pt";
    static final String API_CONTENT_ENDPOINT = "api-content.meocloud.pt";
    static final String API_VERSION = "1";

    // "meocloud" mode has full access to the user files
    // "sandbox" mode has restricted access to only a specific folder
    static final String API_MODE = "sandbox";

    private static String accessToken;

    public static void startOAuth2Authentication(Context context, String consumerKey) {
        Intent intent =  MEOAuth.makeIntent(context, consumerKey);
        if (!(context instanceof Activity)) {
            // If starting the intent outside of an Activity, must include
            // this. See startActivity(). Otherwise, we prefer to stay in
            // the same task.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static String getOAuth2Token() {
        Intent data = MEOAuth.result;

        if (data == null) {
            return null;
        }

        accessToken = data.getStringExtra(MEOAuth.EXTRA_ACCESS_TOKEN);
        int tokenExpiresIn = Integer.valueOf(data.getStringExtra(MEOAuth.EXTRA_TOKEN_EXPIRE));
        //// TODO: 20-Mar-17 if token is expiring soon refresh token

        if(accessToken != null && !accessToken.isEmpty()){
            return accessToken;
        }

        return null;
    }

}
