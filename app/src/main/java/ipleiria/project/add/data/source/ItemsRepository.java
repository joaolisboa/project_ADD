package ipleiria.project.add.data.source;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.remote.FirebaseUserService;

/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemsRepository implements ItemsDataSource {

    private static ItemsRepository INSTANCE = null;

    private User user;
    private DatabaseReference databaseRef;
    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    List<Item> localItems;
    List<Item> localDeletedItems;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean cacheIsDirty = false;

    // Prevent direct instantiation.
    private ItemsRepository() {
        this.user = UserService.getInstance().getUser();
        this.databaseRef = FirebaseDatabase.getInstance().getReference();

        this.localItems = new LinkedList<>();
        this.localDeletedItems = new LinkedList<>();
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @return the {@link ItemsRepository} instance
     */
    public static ItemsRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemsRepository();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public DatabaseReference getItems() {
        DatabaseReference itemsReference = databaseRef.child("items").child(user.getUid());
        itemsReference.keepSynced(true);
        return itemsReference;
    }

    @Override
    public List<Item> getLocalItems() {
        return localItems;
    }

    @Override
    public Item getItem(@NonNull String dbKey) {
        for(Item item: localItems){
            if(item.getDbKey().equals(dbKey)){
                return item;
            }
        }
        return null;
    }

    @Override
    public void addNewItem(@NonNull DataSnapshot itemSnapshot) {
        Item newItem = itemSnapshot.getValue(Item.class);
        String reference = (String) itemSnapshot.child("reference").getValue();
        String[] s = reference.split("\\.");
        int dimension = Integer.valueOf(s[0]) - 1;
        int area = Integer.valueOf(s[1]) - 1;
        int criteria = Integer.valueOf(s[2]) - 1;

        newItem.setCriteria(CategoryRepository.getInstance().getCriteria(dimension, area, criteria));
        newItem.setDbKey(itemSnapshot.getKey());
        for (DataSnapshot fileSnapshot : itemSnapshot.child("files").getChildren()) {
            ItemFile file = fileSnapshot.getValue(ItemFile.class);
            file.setDbKey(fileSnapshot.getKey());
            newItem.addFile(file);
        }
        addItem(newItem);
    }

    private void addItem(Item item){
        int pos = localItems.indexOf(item);
        if(pos < 0){
            localItems.add(item);
        }else{
            localItems.remove(pos);
            localItems.add(pos, item);
        }
    }

    @Override
    public void saveItem(@NonNull Item item) {
        // save locally to list(s) ?
        // save in firebase
    }

    @Override
    public void deleteItem(@NonNull Item item) {

    }

    @Override
    public void permanenetlyDeleteItem(@NonNull Item item) {

    }

    @Override
    public void restoreItem(@NonNull Item item) {

    }

    @Override
    public void refreshItems() {

    }
}
