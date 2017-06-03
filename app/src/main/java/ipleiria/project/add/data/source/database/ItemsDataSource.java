package ipleiria.project.add.data.source.database;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.FilesRepository;

/**
 * Created by Lisboa on 04-May-17.
 */

public interface ItemsDataSource {

    DatabaseReference getDeletedItemsReference();

    DatabaseReference getItemsReference();

    void moveItemsToNewUser();

    void getRemoteItems(final FilesRepository.Callback<List<Item>> callback);

    void getRemoteDeletedItems(final FilesRepository.Callback<List<Item>> callback);

    List<Item> getItems();

    List<Item> getDeletedItems();

    Item getItem(@NonNull String dbKey);

    Item getDeletedItem(@NonNull String dbKey);

    void addNewItem(@NonNull DataSnapshot itemSnapshot, boolean listDeleted);

    void addItem(Item item, boolean flag);

    void saveItem(@NonNull Item item, boolean flag);

    void editItem(Item item, String description, Criteria criteria, long weight);

    void deleteItem(@NonNull Item item);

    void deleteLocalItem(@NonNull Item item, boolean listingDeleted);

    void permanentlyDeleteItem(@NonNull Item item);

    void restoreItem(@NonNull Item item);

    void addFilesToItem(Item item, List<Uri> receivedFiles);

    void addTag(String tag);

    void addTags(List<String> tags);

    List<String> getTags();
}
