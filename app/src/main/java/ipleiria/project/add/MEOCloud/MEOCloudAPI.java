package ipleiria.project.add.MEOCloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Lisboa on 14-Mar-17.
 */

public class MEOCloudAPI {

    static final String AUTHORIZE_URL = "https://meocloud.pt/oauth2/authorize?client_id=%s&response_type=token&state=%s";
    static final String ACCESS_URL = "https://meocloud.pt/oauth2/token";

    //MEOCloud API endpoints
    public static final String API_METHOD_METADATA = "MEOMetadata";
    public static final String API_METHOD_METADATA_SHARE = "MetadataShare";
    public static final String API_METHOD_LIST_LINKS = "ListLinks";
    public static final String API_METHOD_LIST_UPLOAD_LINKS = "ListUploadLinks";
    public static final String API_METHOD_DELETE_LINK = "DeleteLink";
    public static final String API_METHOD_SHARES = "Shares";
    public static final String API_METHOD_UPLOAD_LINK = "UploadLink";
    public static final String API_METHOD_SHARE_FOLDER = "ShareFolder";
    public static final String API_METHOD_LIST_SHARED_FOLDERS = "ListSharedFolders";
    public static final String API_METHOD_THUMBNAILS = "Thumbnails";
    public static final String API_METHOD_SEARCH = "Search";
    public static final String API_METHOD_REVISIONS = "Revisions";
    public static final String API_METHOD_RESTORE = "Restore";
    public static final String API_METHOD_MEDIA = "Media";
    public static final String API_METHOD_FILES = "Files";
    public static final String API_METHOD_DELTA = "Delta";
    public static final String API_METHOD_COPY = "Fileops/Copy";
    public static final String API_METHOD_COPY_REF = "CopyRef";
    public static final String API_METHOD_DELETE = "Fileops/Delete";
    public static final String API_METHOD_MOVE = "Fileops/Move";
    public static final String API_METHOD_CREATE_FOLDER = "Fileops/CreateFolder";
    public static final String API_METHOD_UNDELETE_TREE = "UndeleteTree";
    public static final String API_METHOD_ACOUNT_INFO = "Account/Info";
    public static final String API_METHOD_DISABLE_TOKEN = "DisableAccessToken";



    public static final String API_ENDPOINT = "api.meocloud.pt";
    public static final String API_CONTENT_ENDPOINT = "api-content.meocloud.pt";
    public static final String API_VERSION = "1";

    // "meocloud" mode has full access to the user files
    // "sandbox" mode has restricted access to only a specific folder
    public static final String API_MODE = "sandbox";

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

        String accessToken = data.getStringExtra(MEOAuth.EXTRA_ACCESS_TOKEN);
        int tokenExpiresIn = Integer.valueOf(data.getStringExtra(MEOAuth.EXTRA_TOKEN_EXPIRE));
        MEOAuth.result = null;
        //// TODO: 20-Mar-17 if token is expiring soon refresh token

        if(accessToken != null && !accessToken.isEmpty()){
            return accessToken;
        }

        return null;
    }

}
