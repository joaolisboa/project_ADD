package ipleiria.project.add.data.source;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;

import ipleiria.project.add.dropbox.DropboxCallback;
import ipleiria.project.add.dropbox.DropboxClientFactory;
import ipleiria.project.add.dropbox.DropboxDeleteFile;
import ipleiria.project.add.dropbox.DropboxDownloadFile;
import ipleiria.project.add.dropbox.DropboxGetMetadata;
import ipleiria.project.add.dropbox.DropboxGetThumbnail;
import ipleiria.project.add.dropbox.DropboxListFolder;
import ipleiria.project.add.dropbox.DropboxMoveFile;
import ipleiria.project.add.dropbox.DropboxRevokeToken;
import ipleiria.project.add.dropbox.DropboxUploadFile;

/**
 * Created by Lisboa on 06-May-17.
 */

public class DropboxService implements RemoteFileService<DropboxCallback> {

    private static final String TAG = "DROPBOX_SERVICE";

    private static DropboxService INSTANCE = null;

    public DropboxService(String token){
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
    public boolean isAvailable() {
        return DropboxClientFactory.isClientInitialized();
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
                removeToken();
                callback.onError(e);
            }
        }).execute();
    }

    @Override
    public void getMetadata(String path, final FilesRepository.Callback callback) {
        new DropboxListFolder(DropboxClientFactory.getClient(), new DropboxCallback<ListFolderResult>() {
            @Override
            public void onComplete(ListFolderResult result) {
                callback.onComplete(result);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, e.getMessage(), e);
                callback.onError(e);
            }
        }).execute(path);
    }

    @Override
    public void downloadTempFile(String path, String to, final FilesRepository.Callback<File> callback) {
        new DropboxDownloadFile(DropboxClientFactory.getClient(), new DropboxCallback<File>() {
            @Override
            public void onComplete(File result) {
                callback.onComplete(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }).execute(path, null);
    }

    @Override
    public void downloadFile(String path, final FilesRepository.Callback<File> callback) {
        new DropboxDownloadFile(DropboxClientFactory.getClient(), new DropboxCallback<File>() {
            @Override
            public void onComplete(File result) {
                callback.onComplete(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }).execute(path);
    }

    @Override
    public void uploadFile(Uri uri, String path, /*unused by dropbox*/String filename) {
        new DropboxUploadFile(DropboxClientFactory.getClient(), new DropboxCallback<FileMetadata>() {
            @Override
            public void onComplete(FileMetadata result) {
                Log.d(TAG, "DROPBOX - uploaded " + result.getPathLower());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(uri.toString(), path);
    }

    @Override
    public void moveFile(String from, String to) {
        new DropboxMoveFile(DropboxClientFactory.getClient(), new DropboxCallback<Metadata>() {
            @Override
            public void onComplete(Metadata result) {
                Log.d(TAG, "DROPBOX - moved file" + result.getName());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(from, to);
    }

    @Override
    public void deleteFile(String path) {
        new DropboxDeleteFile(DropboxClientFactory.getClient(), new DropboxCallback<Metadata>() {
            @Override
            public void onComplete(Metadata result) {
                Log.d(TAG, "DROPBOX - deleted file" + result.getName());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(path);
    }

    @Override
    public void downloadThumbnail(String path, final FilesRepository.Callback<File> callback) {
        new DropboxGetThumbnail(DropboxClientFactory.getClient(), new DropboxCallback<File>() {
            @Override
            public void onComplete(File result) {
                callback.onComplete(result);
                Log.d(TAG, "DROPBOX - downloaded thumbnail " + result.getName());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(path);
    }

}
