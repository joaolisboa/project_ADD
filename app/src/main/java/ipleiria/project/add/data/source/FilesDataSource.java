package ipleiria.project.add.data.source;

import java.io.File;

import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 06-May-17.
 */

public interface FilesDataSource {

    void saveFile(ItemFile newFile);

    File getCachedThumbnail(ItemFile file);

    void downloadThumbnail(ItemFile file, FilesRepository.Callback<File> callback);

    File getLocalFile(ItemFile file);

    void downloadFile(ItemFile file, FilesRepository.Callback<File> callback);

    void deleteFile(ItemFile deletedFile);

    void permanentlyDeleteFile(ItemFile file);

    void restoreFile(ItemFile restoredFile);

    void renameFile(ItemFile file, String oldFilename, String newFilename);

}
