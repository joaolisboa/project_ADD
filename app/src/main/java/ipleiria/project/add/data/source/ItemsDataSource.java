package ipleiria.project.add.data.source;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import ipleiria.project.add.data.model.Item;

/**
 * Created by Lisboa on 04-May-17.
 */

public interface ItemsDataSource {

    DatabaseReference getDeletedItemsReference();

    DatabaseReference getItemsReference();

    void moveItemsToNewUser();

    List<Item> getItems();

    List<Item> getDeletedItems();

    Item getItem(@NonNull String dbKey);

    Item getDeletedItem(@NonNull String dbKey);

    void addNewItem(@NonNull DataSnapshot itemSnapshot, boolean listDeleted);

    void saveItem(@NonNull Item item);

    void deleteItem(@NonNull Item item);

    void deleteItem(@NonNull String dbKey);

    void permanenetlyDeleteItem(@NonNull Item item);

    void restoreItem(@NonNull Item item);

    void refreshItems();

    void addFilesToItem(Item item, List<Uri> receivedFiles);
}
