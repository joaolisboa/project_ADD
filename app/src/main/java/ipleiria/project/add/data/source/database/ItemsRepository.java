package ipleiria.project.add.data.source.database;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.utils.UriHelper;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemsRepository implements ItemsDataSource {

    private static final String TAG = "ITEMS_REPO";
    private static final String DELETED_ITEMS = "deleted-items";
    private static final String ITEMS = "items";

    private static ItemsRepository INSTANCE = null;

    private final FilesRepository filesRepository;

    private DatabaseReference itemsReference;
    private DatabaseReference deletedItemsReference;
    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    private List<Item> localItems;
    private List<Item> localDeletedItems;
    // this list will contain all tags from all tags to provide autocomplete suggestions
    private List<String> tags;

    // force reading remotely if required, otherwise will get local items
    private boolean cacheIsDirty;

    // Prevent direct instantiation.
    private ItemsRepository() {
        this.localItems = new LinkedList<>();
        this.localDeletedItems = new LinkedList<>();
        this.tags = new LinkedList<>();
        this.cacheIsDirty = false;

        // test tags
        tags.add("projeto");
        tags.add("exemplo");
        tags.add("visual");
        tags.add("relatorio");
        tags.add("report");
        tags.add("anexos");
        tags.add("programaçao");
        tags.add("design");

        this.filesRepository = FilesRepository.getInstance();
    }

    public void initUser(String userUid) {
        this.itemsReference = FirebaseDatabase.getInstance().getReference().child(ITEMS).child(userUid);
        this.itemsReference.keepSynced(true);
        this.deletedItemsReference = FirebaseDatabase.getInstance().getReference().child(DELETED_ITEMS).child(userUid);
        this.deletedItemsReference.keepSynced(true);
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
    public DatabaseReference getDeletedItemsReference() {
        return deletedItemsReference;
    }

    @Override
    public DatabaseReference getItemsReference() {
        return itemsReference;
    }

    @Override
    public void getItems(final boolean deleted, final FilesRepository.Callback<List<Item>> callback){
        itemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapshot: dataSnapshot.getChildren()) {
                    addNewItem(itemSnapshot, false);
                }
                deletedItemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot itemSnapshot: dataSnapshot.getChildren()) {
                            addNewItem(itemSnapshot, true);
                        }
                        if(deleted) {
                            callback.onComplete(localDeletedItems);
                        }else{
                            callback.onComplete(localItems);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    private void getItems(final FilesRepository.Callback<List<Item>> callback){
        itemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapshot: dataSnapshot.getChildren()) {
                    addNewItem(itemSnapshot, false);
                }
                callback.onComplete(localItems);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    private void getDeletedItems(final FilesRepository.Callback<List<Item>> callback){
        deletedItemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot itemSnapshot: dataSnapshot.getChildren()) {
                    addNewItem(itemSnapshot, true);
                }
                callback.onComplete(localDeletedItems);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }

    // local items will be moved to new user
    // ie. when he upgrades from anon to Google account
    @Override
    public void moveItemsToNewUser() {
        Log.d(TAG, "Moving files to new user: " + UserService.getInstance().getUser().getUid());
        for (Item item : localItems) {
            saveItemToDatabase(item);
        }
        for (Item deletedItem : localDeletedItems) {
            saveItemToDatabase(deletedItem);
        }
    }

    @Override
    public List<Item> getItems() {
        /*if(localItems.isEmpty() || cacheIsDirty){
            return getItems();
        }*/
        return localItems;
    }

    @Override
    public List<Item> getDeletedItems() {
        return localDeletedItems;
    }

    @Override
    public Item getItem(@NonNull String dbKey) {
        for (Item item : localItems) {
            if (item.getDbKey().equals(dbKey)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public Item getDeletedItem(@NonNull String dbKey) {
        for (Item item : localDeletedItems) {
            if (item.getDbKey().equals(dbKey)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void addNewItem(@NonNull DataSnapshot itemSnapshot, boolean deleted) {
        Item newItem = new Item((String) itemSnapshot.child("description").getValue());
        newItem.setDbKey(itemSnapshot.getKey());

        newItem.setWeight((Long) itemSnapshot.child("weight").getValue());

        String reference = (String) itemSnapshot.child("reference").getValue();
        Criteria criteriaO = CategoryRepository.getInstance().getCriteria(reference);
        newItem.setCriteria(criteriaO);
        if(deleted){
            criteriaO.deleteItem(newItem);
        }

        for(DataSnapshot tagSnapshot: itemSnapshot.child("tags").getChildren()){
            newItem.addTag(tagSnapshot.getValue(String.class));
        }

        for (DataSnapshot fileSnapshot : itemSnapshot.child("files").getChildren()) {
            ItemFile file = fileSnapshot.getValue(ItemFile.class);
            file.setDbKey(fileSnapshot.getKey());
            if (file.isDeleted()) {
                newItem.addDeletedFile(file);
            } else {
                newItem.addFile(file);
            }
        }
        addItem(newItem, deleted);
    }

    @Override
    public void addItem(Item item, boolean flag) {
        List<Item> itemDestination = (!flag ? localItems : localDeletedItems);
        int pos = itemDestination.indexOf(item);
        if (pos < 0) {
            itemDestination.add(item);
        } else {
            itemDestination.remove(pos);
            itemDestination.add(pos, item);
        }
    }

    @Override
    public void saveItem(@NonNull Item item, boolean flag) {
        addItem(item, flag);
        saveItemToDatabase(item);
    }

    @Override
    public void editItem(Item item, String newDescription, Criteria newCriteria, long weight) {
        item.setDescription(newDescription);
        System.out.println("new weight: " + weight);
        item.setWeight(weight);
        if(!item.getCriteria().equals(newCriteria)){
            for(ItemFile file: item.getFiles()){
                filesRepository.moveFile(file, newCriteria);
            }
            item.setCriteria(newCriteria);
        }
        // we can only edit items in the non-deleted list so it should always be false
        saveItem(item, false);
        // if the item has a deleted version(one or more files were deleted) we also need to update it
        if(localDeletedItems.contains(item)){
            int pos = localDeletedItems.indexOf(item);
            Item deletedVersion = localDeletedItems.get(pos);
            deletedVersion.setDescription(item.getDescription());
            if(!deletedVersion.getCriteria().equals(newCriteria)){
                for(ItemFile file: deletedVersion.getDeletedFiles()){
                    filesRepository.moveFile(file, newCriteria);
                }
                deletedVersion.setCriteria(newCriteria);
            }
            saveDeletedItemToDatabase(deletedVersion);
        }
    }

    @Override
    public void deleteLocalItem(@NonNull Item item, boolean listingDeleted) {
        if (!listingDeleted) {
            localItems.remove(item);
        } else {
            localDeletedItems.remove(item);
        }
    }

    @Override
    public void deleteItem(@NonNull Item item) {
        // move item to deleted-items and delete original
        localItems.remove(item);

        item.getCriteria().deleteItem(item);

        // if item is being deleted and already has a copy in deletedFiles then copy
        if (localDeletedItems.contains(item)) {
            int pos = localDeletedItems.indexOf(item);
            Item originalItem = localDeletedItems.get(pos);
            // move deleted files to original item
            for (ItemFile fileToDelete : item.getFiles()) {
                filesRepository.deleteFile(fileToDelete);
                fileToDelete.setDeleted(true);
                originalItem.addDeletedFile(fileToDelete);
            }
            item = originalItem;
        } else {
            for (ItemFile file : item.getFiles()) {
                filesRepository.deleteFile(file);
                file.setDeleted(true);
                item.addDeletedFile(file);
            }
            localDeletedItems.add(item);
        }

        item.clearFiles();
        saveDeletedItemToDatabase(item);
        itemsReference.child(item.getDbKey()).removeValue();
    }

    @Override
    public void permanentlyDeleteItem(@NonNull Item item) {
        localDeletedItems.remove(item);
        localItems.remove(item);
        item.getCriteria().permanentlyDeleteItem(item);

        if(itemsReference.child(item.getDbKey()) != null) {
            itemsReference.child(item.getDbKey()).removeValue();
        }
        deletedItemsReference.child(item.getDbKey()).removeValue();

        for (ItemFile file : item.getDeletedFiles()) {
            filesRepository.permanentlyDeleteFile(file);
        }
    }

    @Override
    public void restoreItem(@NonNull Item item) {
        localDeletedItems.remove(item);
        item.getCriteria().restoreItem(item);

        // if item has deleted files but itself isn't deleted then it'll be in this list
        if (localItems.contains(item)) {
            int pos = localItems.indexOf(item);
            Item originalItem = localItems.get(pos);
            // move deleted files to original item
            for (ItemFile fileToRestore : item.getDeletedFiles()) {
                filesRepository.restoreFile(fileToRestore);
                fileToRestore.setDeleted(false);
                originalItem.addFile(fileToRestore);
            }
            item = originalItem;
        } else{
            for (ItemFile file : item.getDeletedFiles()) {
                filesRepository.restoreFile(file);
                file.setDeleted(false);
                item.addFile(file);
            }
            localItems.add(item);
        }

        item.clearDeletedFiles();

        saveItemToDatabase(item);
        deletedItemsReference.child(item.getDbKey()).removeValue();
    }

    @Override
    public void addFilesToItem(Item item, List<Uri> receivedFiles) {
        for (Uri uri : receivedFiles) {
            ItemFile file = new ItemFile(UriHelper.getFileName(Application.getAppContext(), uri));
            item.addFile(file);
            filesRepository.saveFile(file, uri);
        }
        saveItem(item, false);
    }

    @Override
    public void addTag(String tag) {
        tags.add(tag);
    }

    @Override
    public void addTags(List<String> tags) {
        this.tags.addAll(tags);
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    void saveDeletedItemToDatabase(Item item){
        DatabaseReference deletedItemRef = getDeletedItemsReference();
        Map<String, Object> values = new HashMap<>();
        if (item.getDbKey() == null || item.getDbKey().isEmpty()) {
            deletedItemRef = deletedItemRef.push();
            item.setDbKey(deletedItemRef.getKey());
        } else {
            deletedItemRef = deletedItemRef.child(item.getDbKey());
        }
        if (!item.getDeletedFiles().isEmpty()) {
            System.out.println("writing deleted files: " + Arrays.toString(item.getDeletedFiles().toArray()));
            Map<String, Object> deletedFileList = getFileList(deletedItemRef.child("files"), item.getDeletedFiles());
            values.put("files", deletedFileList);
            values.put("reference", item.getCategoryReference());
            values.put("description", item.getDescription());
            values.put("weight", item.getWeight());
            values.put("tags", item.getTags());
            deletedItemRef.setValue(values);
        } else {
            // no deleted files means we can delete the whole item in the deleted-items
            deletedItemRef.removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Log.d(TAG, "Deleting item from deleted-items due to empty list");
                }
            });
        }
    }

    void saveItemToDatabase(Item item) {
        DatabaseReference itemRef = getItemsReference();

        // when updating the item in db make sure it wasn't deleted otherwise
        // it'll create the item again after changes are made
        if(localItems.contains(item)){
            Map<String, Object> values = new HashMap<>();
            if (item.getDbKey() == null || item.getDbKey().isEmpty()) {
                itemRef = itemRef.push();
                item.setDbKey(itemRef.getKey());
            } else {
                itemRef = itemRef.child(item.getDbKey());
            }

            if (!item.getFiles().isEmpty()) {
                Map<String, Object> fileList = getFileList(itemRef.child("files"), item.getFiles());
                values.put("files", fileList);
            }
            values.put("reference", item.getCategoryReference());
            values.put("description", item.getDescription());
            values.put("weight", item.getWeight());
            values.put("tags", item.getTags());
            itemRef.setValue(values);
        }
    }

    private Map<String, Object> getFileList(DatabaseReference ref, List<ItemFile> files) {
        Map<String, Object> fileList = new HashMap<>();
        for (ItemFile file : files) {
            Map<String, Object> itemFile = new HashMap<>();
            if (file.getDbKey() == null || file.getDbKey().isEmpty()) {
                ref = ref.push();
                file.setDbKey(ref.getKey());
            } else {
                ref = ref.child(file.getDbKey());
            }
            itemFile.put("filename", file.getFilename());
            itemFile.put("deleted", file.isDeleted());
            fileList.put(file.getDbKey(), itemFile);
        }
        return fileList;
    }

}
