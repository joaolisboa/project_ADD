package ipleiria.project.add.data.source;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.dropbox.core.v2.files.Metadata;

import java.io.File;

/**
 * Created by Lisboa on 06-May-17.
 */

interface RemoteFileService<I> {

    void init(@NonNull String token);

    boolean isAvailable();

    void revokeToken(I callback);

    void getMetadata(String path, FilesRepository.BaseCallback callback);

    void downloadTempFile(String path, String to, FilesRepository.Callback<File> callback);

    void downloadFile(String path, FilesRepository.Callback<File> callback);

    void uploadFile(Uri uri, String path, String filename);

    void moveFile(String from, String to);

    void deleteFile(String path);

    void downloadThumbnail(String path, FilesRepository.Callback<File> callback);

}