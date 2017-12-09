package ipleiria.project.add.data.source;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;

import ipleiria.project.add.data.source.file.FilesRepository;

/**
 * Created by Lisboa on 06-May-17.
 */

interface RemoteFileService<I> {

    void init(@NonNull String token);

    boolean isAvailable();

    void revokeToken(I callback);

    void getMetadata(String path, FilesRepository.Callback callback);

    void downloadTempFile(String path, String to, FilesRepository.Callback<File> callback);

    void downloadFile(String path, FilesRepository.Callback<File> callback);

    void uploadFile(Uri uri, String path, String filename);

    void moveFile(String from, String to);

    void deleteFile(String path);

    // base callback can't be used because one of the services needs to act
    // as a fallback(dropbox) in case it fails
    void downloadThumbnail(String path, FilesRepository.Callback<File> callback);

}
