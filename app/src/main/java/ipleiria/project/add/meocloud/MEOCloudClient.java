package ipleiria.project.add.meocloud;

import ipleiria.project.add.meocloud.exceptions.MissingAccessTokenException;

import static ipleiria.project.add.data.source.UserService.MEO_PREFS_KEY;


/**
 * Created by J on 03/04/2017.
 */

public class MEOCloudClient {

    private static String accessToken;

    public static void init(String accessToken){
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
