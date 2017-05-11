package ipleiria.project.add.data.source.database;

import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 10-May-17.
 */

public interface ItemFilesDataSource {

    void renameItemFile(ItemFile file);

    void deleteItemFile(ItemFile file);

    void permanentlyDeleteItemFile(ItemFile file);

    void restoreItemFile(ItemFile file);

}
