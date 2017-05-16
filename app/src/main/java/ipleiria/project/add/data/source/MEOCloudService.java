package ipleiria.project.add.data.source;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudAPI;
import ipleiria.project.add.meocloud.MEOCloudClient;
import ipleiria.project.add.meocloud.data.FileResponse;
import ipleiria.project.add.meocloud.data.MEOMetadata;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
import ipleiria.project.add.meocloud.tasks.MEOCreateFolderTree;
import ipleiria.project.add.meocloud.tasks.MEODeleteFile;
import ipleiria.project.add.meocloud.tasks.MEODownloadFile;
import ipleiria.project.add.meocloud.tasks.MEOGetMetadata;
import ipleiria.project.add.meocloud.tasks.MEOGetThumbnail;
import ipleiria.project.add.meocloud.tasks.MEOMoveFile;
import ipleiria.project.add.meocloud.tasks.MEORevokeToken;
import ipleiria.project.add.meocloud.tasks.MEOUploadFile;
import ipleiria.project.add.utils.HttpStatus;

/**
 * Created by Lisboa on 06-May-17.
 */

public class MEOCloudService implements RemoteFileService<MEOCallback> {

    private static final String TAG = "MEOCLOUD_SERVICE";

    private static MEOCloudService INSTANCE = null;

    private MEOCloudService(String token) {
        if (token != null && !token.isEmpty()) {
            MEOCloudClient.init(token);
        }
    }

    public static MEOCloudService getInstance() {
        return INSTANCE;
    }

    public static MEOCloudService getInstance(String token) {
        if (INSTANCE == null) {
            INSTANCE = new MEOCloudService(token);
        }
        return INSTANCE;
    }

    private void removeToken() {
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
                if (httpE.getErrorCode() == HttpStatus.UNAUTHORIZED) {
                    removeToken();
                }
                callback.onRequestError(httpE);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }).execute();
    }

    @Override
    public void getMetadata(String path, final FilesRepository.BaseCallback callback) {
        new MEOGetMetadata(new MEOCallback<MEOMetadata>() {
            @Override
            public void onComplete(MEOMetadata result) {
                callback.onComplete(result);
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                Log.e(TAG, httpE.getMessage(), httpE);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(path);
    }

    @Override
    public void downloadTempFile(String path, String to, final FilesRepository.Callback<File> callback) {
        new MEODownloadFile(new MEOCallback<FileResponse>() {
            @Override
            public void onComplete(FileResponse result) {
                callback.onComplete(result);
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                callback.onError(httpE);
                Log.e(TAG, httpE.getMessage(), httpE);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(path, null, to);
    }

    @Override
    public void downloadFile(final String path, final FilesRepository.Callback<File> callback) {
        new MEODownloadFile(new MEOCallback<FileResponse>() {
            @Override
            public void onComplete(FileResponse result) {
                callback.onComplete(result);
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                callback.onError(httpE);
                Log.e(TAG, httpE.getMessage(), httpE);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(path);
    }

    @Override
    public void uploadFile(final Uri uri, final String path, final String filename) {
        new MEOCreateFolderTree(new MEOCallback<MEOMetadata>() {
            @Override
            public void onComplete(MEOMetadata result) {
                new MEOUploadFile(new MEOCallback<MEOMetadata>() {
                    @Override
                    public void onComplete(MEOMetadata result) {
                        Log.d(TAG, "MEO - uploaded " + result.getPath());
                    }

                    @Override
                    public void onRequestError(HttpErrorException httpE) {
                        Log.e(TAG, httpE.getMessage(), httpE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }).execute(uri.toString(), path + "/" + filename);
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                Log.e(TAG, httpE.getMessage(), httpE);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(path.split("/"));
    }

    @Override
    public void moveFile(final String from, final String to) {
        new MEOCreateFolderTree(new MEOCallback<MEOMetadata>() {
            @Override
            public void onComplete(MEOMetadata result) {
                new MEOMoveFile(new MEOCallback<MEOMetadata>() {
                    @Override
                    public void onComplete(MEOMetadata result) {
                        Log.d(TAG, "MEO - moved file " + result.getPath());
                    }

                    @Override
                    public void onRequestError(HttpErrorException httpE) {
                        Log.e(TAG, httpE.getMessage(), httpE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }).execute(from, to);
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                Log.e(TAG, httpE.getMessage(), httpE);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(to.substring(1, to.lastIndexOf("/")).split("/"));
    }

    @Override
    public void deleteFile(String path) {
        new MEODeleteFile(new MEOCallback<MEOMetadata>() {
            @Override
            public void onComplete(MEOMetadata result) {
                Log.d(TAG, "MEO - deleted " + result.getPath());
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                Log.e(TAG, httpE.getMessage(), httpE);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(path);
    }

    @Override
    public void downloadThumbnail(final String path, final FilesRepository.Callback<File> callback) {
        new MEOGetThumbnail(new MEOCallback<File>() {
            @Override
            public void onComplete(File result) {
                callback.onComplete(result);
                Log.d(TAG, "MEO - downloaded thumbnail " + result.getName());
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                Log.e(TAG, httpE.getMessage(), httpE);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(path, /*format*/ null, MEOCloudAPI.THUMBNAIL_SIZE_M);
    }
}
