package ipleiria.project.add.data.source.database;

import java.util.List;

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

    List<String> getTagSuggestions();

    void addTag(String tag);

    void removeTag(String tag);
}
