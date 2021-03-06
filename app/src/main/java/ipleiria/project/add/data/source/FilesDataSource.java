package ipleiria.project.add.data.source;

import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.PendingFile;

/**
 * Created by Lisboa on 06-May-17.
 */

public interface FilesDataSource {

    String getRelativePath(File file);

    void getRemotePendingFiles(final FilesRepository.ServiceCallback<List<PendingFile>> callback);

    void addPendingFiles(List<PendingFile> files);

    void saveFile(ItemFile newFile, Uri uri);

    void uploadFile(Uri uri, String path, String filename);

    File getCachedThumbnail(ItemFile file);

    void downloadThumbnail(ItemFile file, FilesRepository.BaseCallback<File> callback);

    void getThumbnail(ItemFile file, FilesRepository.BaseCallback<File> callback);

    void getFileToShare(ItemFile file, FilesRepository.Callback<File> callback);

    void getFile(ItemFile file, FilesRepository.Callback<File> callback);

    void moveFile(ItemFile file, Criteria newCriteria);

    void deleteFile(ItemFile deletedFile);

    void permanentlyDeleteFile(ItemFile file);

    void restoreFile(ItemFile restoredFile);

    void renameFile(ItemFile file, String oldFilename, String newFilename);

    void saveEmailAttachment(String filename, byte[] fileByteArray) throws IOException;
}
