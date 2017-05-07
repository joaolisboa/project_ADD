package ipleiria.project.add.data.source;

import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 06-May-17.
 */

public interface FilesDataSource {

    void deleteFile(ItemFile file);

    void permanenetlyDeleteFile(ItemFile file);

    void restoreFile(ItemFile file);

    void saveFile(ItemFile file);

}
