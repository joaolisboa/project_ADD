package ipleiria.project.add.data.source.database;

import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 10-May-17.
 */

// itemfilesRepository responsibility will be dealing with Firebase while FilesRepository with local/remote files
public class ItemFilesRepository implements ItemFilesDataSource {

    private static final String TAG = "ITEM_FILES_REPOSITORY";
    private static ItemFilesRepository INSTANCE = null;

    private ItemsRepository itemsRepository;
    private DatabaseReference itemFilesReference;
    private DatabaseReference itemDeletedFilesReference;

    private Item item;

    public ItemFilesRepository(Item item, ItemsRepository itemsRepository) {
        this.item = item;
        this.itemsRepository = itemsRepository;

        this.itemFilesReference = itemsRepository.getItemsReference().child(item.getDbKey()).child("files");
        this.itemDeletedFilesReference = itemsRepository.getDeletedItemsReference().child(item.getDbKey()).child("files");
    }

    public static ItemFilesRepository getInstance(Item item, ItemsRepository itemsRepository) {
        if (INSTANCE == null) {
            INSTANCE = new ItemFilesRepository(item, itemsRepository);
        }
        return INSTANCE;
    }

    public static ItemFilesRepository newInstance(Item item, ItemsRepository itemsRepository) {
        INSTANCE = new ItemFilesRepository(item, itemsRepository);
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public void renameItemFile(ItemFile file) {
        itemsRepository.saveItemToDatabase(item);
    }

    @Override
    public void deleteItemFile(ItemFile file) {
        file.setDeleted(true);

        if (itemsRepository.getItems().contains(file.getParent())) {
            int pos = itemsRepository.getItems().indexOf(file.getParent());
            Item originalItem = itemsRepository.getItems().get(pos);
            // delete file from deleted list
            if (!originalItem.getFiles().remove(file)) {
                Log.d(TAG, "file isn't in list");
            }
            itemsRepository.saveItemToDatabase(originalItem);
        } else {
            Log.wtf(TAG, "Really shouldn't happen, an item with a deleted file should always be in the Items list");
        }

        if (itemsRepository.getDeletedItems().contains(file.getParent())) {
            int pos = itemsRepository.getDeletedItems().indexOf(file.getParent());
            Item originalItem = itemsRepository.getDeletedItems().get(pos);
            // restore file in original list
            originalItem.addDeletedFile(file);
            item = originalItem;
        } else {
            // if item in the 'opposite' list is missing create it
            item.addDeletedFile(file);
            itemsRepository.addItem(file.getParent(), null, true);
        }

        itemsRepository.saveDeletedItemToDatabase(file.getParent());

        itemFilesReference.child(file.getDbKey()).removeValue();
    }

    @Override
    public void permanentlyDeleteItemFile(ItemFile file) {

        if (itemsRepository.getDeletedItems().contains(file.getParent())) {
            int pos = itemsRepository.getDeletedItems().indexOf(file.getParent());
            Item originalItem = itemsRepository.getDeletedItems().get(pos);
            originalItem.getDeletedFiles().remove(file);
        } else {
            Log.wtf(TAG, "Really shouldn't happen, an item with a deleted file should always be in the deleted Items list");
        }

        itemDeletedFilesReference.child(file.getDbKey()).removeValue();

        // if the delete item has no more files it'll be deleted
        if (file.getParent().getDeletedFiles().isEmpty()) {
            itemsRepository.deleteLocalItem(file.getParent(), true);
            itemsRepository.getDeletedItemsReference().child(file.getParent().getDbKey()).removeValue();
        }
    }

    @Override
    public void restoreItemFile(ItemFile file) {
        file.setDeleted(false);

        if (itemsRepository.getDeletedItems().contains(file.getParent())) {
            int pos = itemsRepository.getDeletedItems().indexOf(file.getParent());
            Item originalItem = itemsRepository.getDeletedItems().get(pos);
            // delete file from deleted list
            if (!originalItem.getDeletedFiles().remove(file)) {
                Log.d(TAG, "file isn't in list");
            }
            if (originalItem.getDeletedFiles().isEmpty()) {
                itemsRepository.deleteLocalItem(file.getParent(), true);
            }
            itemsRepository.saveDeletedItemToDatabase(originalItem);
        } else {
            System.out.println(itemsRepository.getDeletedItem(file.getParent().getDbKey()));
            System.out.println(Arrays.toString(itemsRepository.getDeletedItems().toArray()));
            Log.wtf(TAG, "Probably shouldn't happen, an item with a deleted file should be in the deleted Items list");
        }

        if (itemsRepository.getItems().contains(file.getParent())) {
            int pos = itemsRepository.getItems().indexOf(file.getParent());
            Item originalItem = itemsRepository.getItems().get(pos);
            // restore file in original list
            originalItem.addFile(file);
            item = originalItem;
        } else {
            // if item in the 'opposite' list is missing create it
            file.getParent().addFile(file);
            itemsRepository.addItem(file.getParent(), null, false);
        }

        itemsRepository.saveItemToDatabase(file.getParent());

        itemDeletedFilesReference.child(file.getDbKey()).removeValue();
    }

    @Override
    public List<String> getTagSuggestions() {
        return itemsRepository.getTags();
    }

    @Override
    public void addTag(String tag) {
        if(!item.getTags().contains(tag)){
            item.addTag(tag);
            if(itemsRepository.getDeletedItem(item.getDbKey()) != null){
                itemsRepository.saveDeletedItemToDatabase(item);
            }
            itemsRepository.saveItemToDatabase(item);
        }
        if(!itemsRepository.getTags().contains(tag)){
            itemsRepository.addTag(tag);
        }
    }

    @Override
    public void removeTag(String tag) {
        if(item.getTags().contains(tag)){
            item.removeTag(tag);
            if(itemsRepository.getDeletedItem(item.getDbKey()) != null){
                itemsRepository.saveDeletedItemToDatabase(item);
            }
            itemsRepository.saveItemToDatabase(item);
        }
    }
}
