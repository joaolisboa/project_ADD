package ipleiria.project.add.data.source;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import ipleiria.project.add.data.model.Item;

/**
 * Created by Lisboa on 04-May-17.
 */

public interface ItemsDataSource {

    DatabaseReference getItems();

    List<Item> getLocalItems();

    Item getItem(@NonNull String itemID);

    void addNewItem(@NonNull DataSnapshot itemSnapshot);

    void saveItem(@NonNull Item item);

    void deleteItem(@NonNull Item item);

    void permanenetlyDeleteItem(@NonNull Item item);

    void restoreItem(@NonNull Item item);

    void refreshItems();
}
