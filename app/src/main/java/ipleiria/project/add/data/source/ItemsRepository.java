package ipleiria.project.add.data.source;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.Application;
import ipleiria.project.add.Utils.UriHelper;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.User;

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
    List<Item> localItems;
    List<Item> localDeletedItems;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean cacheIsDirty = false;

    // Prevent direct instantiation.
    private ItemsRepository() {
        initUser(UserService.getInstance().getUser().getUid());

        this.localItems = new LinkedList<>();
        this.localDeletedItems = new LinkedList<>();

        this.filesRepository = FilesRepository.getInstance();
    }

    public void initUser(String userUid){
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
    public List<Item> getItems() {
        return localItems;
    }

    @Override
    public List<Item> getDeletedItems() {
        return localDeletedItems;
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
    public Item getDeletedItem(@NonNull String dbKey) {
        for(Item item: localDeletedItems){
            if(item.getDbKey().equals(dbKey)){
                return item;
            }
        }
        return null;
    }

    @Override
    public void addNewItem(@NonNull DataSnapshot itemSnapshot, boolean listDeleted) {
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
        addItem(newItem, listDeleted);
    }

    private void addItem(Item item, boolean listDeleted){
        List<Item> itemDestination = (!listDeleted ? localItems : localDeletedItems);
        int pos = itemDestination.indexOf(item);
        if(pos < 0){
            itemDestination.add(item);
        }else{
            itemDestination.remove(pos);
            itemDestination.add(pos, item);
        }
    }

    @Override
    public void saveItem(@NonNull Item item) {
        writeItem(item);
        addItem(item, false);
    }

    @Override
    public void deleteItem(@NonNull String dbKey){
        localItems.remove(getItem(dbKey));
    }

    @Override
    public void deleteItem(@NonNull final Item item) {
        // move item to deleted-items and delete original
        localItems.remove(item);
        localDeletedItems.add(item);

        for(ItemFile file: item.getFiles()){
            filesRepository.deleteFile(file);
            file.setDeleted(true);
        }

        writeDeletedItem(item);
        getItemsReference().child(item.getDbKey()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d(TAG, "Deleted item: " + item);
            }
        });
    }

    @Override
    public void permanenetlyDeleteItem(@NonNull final Item item) {
        localDeletedItems.remove(item);
        for(ItemFile file: item.getFiles()){
            filesRepository.permanenetlyDeleteFile(file);
        }
        getDeletedItemsReference().child(item.getDbKey()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d(TAG, "Permanently deleted item: " + item);
            }
        });
    }

    @Override
    public void restoreItem(@NonNull final Item item) {
        localDeletedItems.remove(item);
        localItems.add(item);

        for(ItemFile file: item.getFiles()){
            if(file.isDeleted()){
                filesRepository.restoreFile(file);
                file.setDeleted(false);
            }
        }

        writeItem(item);
        getDeletedItemsReference().child(item.getDbKey()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d(TAG, "restore deleted item: " + item);
            }
        });
    }

    @Override
    public void refreshItems() {

    }

    @Override
    public void addFilesToItem(Item item, List<Uri> receivedFiles) {
        for (Uri uri : receivedFiles) {
            ItemFile file = new ItemFile(UriHelper.getFileName(Application.getAppContext(), uri));
            item.addFile(file);
            filesRepository.saveFile(file);
        }
        saveItem(item);
    }

    private void writeItem(Item item){
        writeItemToRef(item, getItemsReference());
    }

    private void writeDeletedItem(Item item){
        writeItemToRef(item, getDeletedItemsReference());
    }

    private void writeItemToRef(Item item, DatabaseReference itemRef){
        Map<String, Object> values = new HashMap<>();
        if(item.getDbKey() == null || item.getDbKey().isEmpty()){
            itemRef = itemRef.push();
            item.setDbKey(itemRef.getKey());
        }else{
            itemRef = itemRef.child(item.getDbKey());
        }
        DatabaseReference itemFileRef = itemRef.child("files");
        Map<String, Object> fileList = new HashMap<>();
        for(ItemFile file: item.getFiles()){
            Map<String, Object> itemFile = new HashMap<>();
            if(file.getDbKey() == null || file.getDbKey().isEmpty()){
                itemFileRef = itemFileRef.push();
                file.setDbKey(itemFileRef.getKey());
            }else{
                itemFileRef = itemFileRef.child(file.getDbKey());
            }
            itemFile.put("filename", file.getFilename());
            itemFile.put("deleted", file.isDeleted());
            fileList.put(file.getDbKey(), itemFile);
        }
        values.put("files", fileList);
        values.put("reference", item.getCategoryReference());
        values.put("description", item.getDescription());
        itemRef.setValue(values);
    }

}