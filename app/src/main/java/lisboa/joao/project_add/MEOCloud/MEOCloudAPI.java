package lisboa.joao.project_add.MEOCloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Lisboa on 14-Mar-17.
 */

public class MEOCloudAPI {

    static final String AUTHORIZE_URL = "https://meocloud.pt/oauth2/authorize?client_id=%s&response_type=token&state=%s";
    static final String ACCESS_URL = "https://meocloud.pt/oauth2/token";

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

    public static String getOAuth2Token(final Context context) {
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
