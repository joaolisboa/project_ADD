package ipleiria.project.add.data.source;

import android.support.annotation.NonNull;

import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudClient;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
import ipleiria.project.add.meocloud.tasks.MEORevokeToken;

/**
 * Created by Lisboa on 06-May-17.
 */

public class MEOCloudService implements AccountService<MEOCallback> {

    private static MEOCloudService INSTANCE = null;

    private MEOCloudService(String token){
        if(token != null && !token.isEmpty()) {
            MEOCloudClient.init(token);
        }
    }

    public static MEOCloudService getInstance() {
        return INSTANCE;
    }

    public static MEOCloudService getInstance(String token){
        if(INSTANCE == null){
            INSTANCE = new MEOCloudService(token);
        }
        return INSTANCE;
    }

    private void removeToken(){
        UserService.getInstance().removeMEOCloudToken();
        MEOCloudClient.destroyClient();
    }

    @Override
    public void init(@NonNull String token) {
        INSTANCE = new MEOCloudService(token);
    }

    @Override
    public boolean isAvailable() {
        return MEOCloudClient.isClientInitialized();
    }

    @Override
    public void revokeToken(final MEOCallback callback) {
        new MEORevokeToken(new MEOCallback<Void>() {
            @Override
            public void onComplete(Void result) {
                removeToken();
                callback.onComplete(result);
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                removeToken();
                callback.onRequestError(httpE);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }).execute();
    }
}
