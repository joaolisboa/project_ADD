package ipleiria.project.add.data.source;

import java.io.File;

import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 06-May-17.
 */

public interface FilesDataSource {

    void addFile(ItemFile newFile);

    File getFileThumbnail(ItemFile file);

    File downloadThumbnail(ItemFile file);

    File getLocalFile(ItemFile file);

    File downloadFile(ItemFile file);

    void deleteFile(ItemFile deletedFile);

    void permanentlyDeleteFile(ItemFile file);

    void restoreFile(ItemFile restoredFile);

    void renameFile(ItemFile file, String oldFilename, String newFilename);

}
