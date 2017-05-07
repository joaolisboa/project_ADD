package ipleiria.project.add.data.source;

import android.support.annotation.NonNull;

import com.dropbox.core.v2.files.Metadata;

import ipleiria.project.add.Dropbox.DropboxCallback;
import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxRevokeToken;

/**
 * Created by Lisboa on 06-May-17.
 */

public class DropboxService implements FilesService<DropboxCallback> {

    private static DropboxService INSTANCE = null;

    private DropboxService(String token){
        if(token != null && !token.isEmpty()) {
            DropboxClientFactory.init(token);
        }
    }

    public static DropboxService getInstance() {
        return INSTANCE;
    }

    public static DropboxService getInstance(String token){
        if(INSTANCE == null){
            INSTANCE = new DropboxService(token);
        }
        return INSTANCE;
    }

    private void removeToken(){
        DropboxClientFactory.destroyClient();
        UserService.getInstance().removeDropboxToken();
    }

    @Override
    public void init(@NonNull String token) {
        INSTANCE = new DropboxService(token);
    }

    @Override
    public void revokeToken(final DropboxCallback callback) {
        new DropboxRevokeToken(DropboxClientFactory.getClient(), new DropboxCallback<Void>(){

            @Override
            public void onComplete(Void result) {
                removeToken();
                callback.onComplete(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }).execute();
    }

    @Override
    public void uploadFile(DropboxCallback callback) {

    }

    @Override
    public void downloadFile(DropboxCallback callback) {

    }

    @Override
    public void moveFile(DropboxCallback callback) {

    }

    @Override
    public void deleteFile() {

    }

    @Override
    public void downloadThumbnail(DropboxCallback callback) {

    }

}
