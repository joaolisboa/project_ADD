package ipleiria.project.add.data.source.database;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.Application;
import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.utils.UriHelper;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;

import static ipleiria.project.add.data.model.PendingFile.DROPBOX;
import static ipleiria.project.add.data.model.PendingFile.EMAIL;
import static ipleiria.project.add.data.model.PendingFile.MEO_CLOUD;

/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemsRepository implements ItemsDataSource {

    private static final String TAG = "ITEMS_REPO";
    private static final String DELETED_ITEMS = "deleted-items";
    private static final String ITEMS = "items";

    private static ItemsRepository INSTANCE = null;

    private final FilesRepository filesRepository;
    private final UserService userService;

    private DatabaseReference itemsReference;
    private DatabaseReference deletedItemsReference;

    private Map<String, List<Item>> localItems;
    //private List<Item> localItems;
    private Map<String, List<Item>> localDeletedItems;
    // this list will contain all tags from all items to provide autocomplete suggestions
    private List<String> tags;

    // value of the currently selected period by the user
    private EvaluationPeriod currentPeriod;

    // TODO: 12-Jun-17 force reading remotely, otherwise will get local items
    private boolean cacheIsDirty;

    // Prevent direct instantiation.
    private ItemsRepository() {
        //this.localItems = new LinkedList<>();
        this.localItems = new LinkedHashMap<>();
        this.localDeletedItems = new LinkedHashMap<>();
        this.tags = new LinkedList<>();
        this.cacheIsDirty = false;

        // add default tags from strings.xml
        tags.addAll(Arrays.asList(Application.getAppContext().getResources().getStringArray(R.array.default_tags)));

        this.userService = UserService.getInstance();
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

    public void initCurrentPeriod(User user){
        EvaluationPeriod mostRecentStart = null;
        for (EvaluationPeriod evaluationPeriod : user.getEvaluationPeriods()) {
            if (mostRecentStart == null || evaluationPeriod.getStartDate()
                    .compareTo(mostRecentStart.getStartDate()) > 0) {
                mostRecentStart = evaluationPeriod;
            }
        }

        if(currentPeriod == null && mostRecentStart != null){
            setCurrentPeriod(mostRecentStart);
        }
    }

    public void setCurrentPeriod(EvaluationPeriod evaluationPeriod) {
        currentPeriod = evaluationPeriod;
        filesRepository.setCurrentPeriod(evaluationPeriod);

        // ensure list creation when period is selected
        // will happen when the user has no items saved in the period
        if(localItems.get(currentPeriod.getDbKey()) == null){
            localItems.put(currentPeriod.getDbKey(), new LinkedList<Item>());
        }
        if(localDeletedItems.get(currentPeriod.getDbKey()) == null){
            localDeletedItems.put(currentPeriod.getDbKey(), new LinkedList<Item>());
        }
    }

    public EvaluationPeriod getCurrentPeriod() {
        return currentPeriod;
    }

    public void mergePeriodItems(EvaluationPeriod newPeriod, EvaluationPeriod currentPeriod) {
        localItems.put(newPeriod.getDbKey(), localItems.get(currentPeriod.getDbKey()));
        localDeletedItems.put(newPeriod.getDbKey(), localDeletedItems.get(currentPeriod.getDbKey()));

        deleteEvaluationPeriod(currentPeriod);

        for(Item item: localItems.get(newPeriod.getDbKey())){
            saveItemToDatabase(newPeriod.getDbKey(), item);
        }

        for(Item item: localDeletedItems.get(newPeriod.getDbKey())){
            saveDeletedItemToDatabase(newPeriod.getDbKey(), item);
        }

        localItems.remove(currentPeriod.getDbKey());
        localDeletedItems.remove(currentPeriod.getDbKey());

        itemsReference.child(currentPeriod.getDbKey()).removeValue();
        deletedItemsReference.child(currentPeriod.getDbKey()).removeValue();
    }

    public void deleteEvaluationPeriod(EvaluationPeriod period) {
        // delete items for the period
        itemsReference.child(period.getDbKey()).removeValue();
        // in case the current period has been deleted rerun init to select most recent period
        if(currentPeriod.equals(period)){
            currentPeriod = null;
            initCurrentPeriod(userService.getUser());
        }
    }

    @Override
    public DatabaseReference getDeletedItemsReference() {
        return deletedItemsReference.child(currentPeriod.getDbKey());
    }

    @Override
    public DatabaseReference getItemsReference() {
        return itemsReference.child(currentPeriod.getDbKey());
    }

    // simply read all items and store
    public void readAllItems(){
        itemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot periodSnapshot: dataSnapshot.getChildren()){
                    localItems.put(periodSnapshot.getKey(), new LinkedList<Item>());

                    for (DataSnapshot itemSnapshot: periodSnapshot.getChildren()) {
                        localItems.get(periodSnapshot.getKey()).add(transformItem(periodSnapshot.getKey(), itemSnapshot, false));
                    }
                }
                deletedItemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot periodSnapshot: dataSnapshot.getChildren()){
                            localDeletedItems.put(periodSnapshot.getKey(), new LinkedList<Item>());

                            for (DataSnapshot itemSnapshot: periodSnapshot.getChildren()) {
                                localDeletedItems.get(periodSnapshot.getKey()).add(transformItem(periodSnapshot.getKey(), itemSnapshot, true));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // read all items, store and send back
    @Override
    public void getItems(final boolean deleted, final FilesRepository.Callback<List<Item>> callback) {
        if(currentPeriod == null){
            // TODO: 09-Jul-17 add protection in case current period isn't initiliazed or no periods exist
            callback.onError(new Exception("No period"));
            return;
        }
        itemsReference.child(currentPeriod.getDbKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                localItems.put(currentPeriod.getDbKey(), new LinkedList<Item>());
                for (DataSnapshot itemSnapshot: dataSnapshot.getChildren()) {
                    localItems.get(currentPeriod.getDbKey()).add(transformItem(currentPeriod.getDbKey(), itemSnapshot, false));
                }

                deletedItemsReference.child(currentPeriod.getDbKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        localDeletedItems.put(currentPeriod.getDbKey(), new LinkedList<Item>());
                        for (DataSnapshot itemSnapshot: dataSnapshot.getChildren()) {
                            localDeletedItems.get(currentPeriod.getDbKey()).add(transformItem(currentPeriod.getDbKey(), itemSnapshot, true));
                        }

                        if (deleted) {
                            callback.onComplete(localDeletedItems.get(currentPeriod.getDbKey()));
                        } else {
                            callback.onComplete(localItems.get(currentPeriod.getDbKey()));
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

            }
        });
    }

    // local items will be moved to new user
    // ie. when he upgrades from anon to Google account
    @Override
    public void moveItemsToNewUser() {
        Log.d(TAG, "Moving items to new user: " +  userService.getUser().getUid());
        initUser(userService.getUser().getUid());
        for(Map.Entry<String, List<Item>> entry: localItems.entrySet()){
            for (Item item : entry.getValue()) {
                saveItemToDatabase(entry.getKey(), item);
            }
        }
        for(Map.Entry<String, List<Item>> entry: localDeletedItems.entrySet()){
            for (Item item : entry.getValue()) {
                saveDeletedItemToDatabase(entry.getKey(), item);
            }
        }
    }

    @Override
    public List<Item> getItems() {
        /*if(localItems.isEmpty() || cacheIsDirty){
            return getItems();
        }*/
        return localItems.get(currentPeriod.getDbKey());
    }

    @Override
    public List<Item> getDeletedItems() {
        return localDeletedItems.get(currentPeriod.getDbKey());
    }

    @Override
    public Item getItem(@NonNull String dbKey) {
        for (Item item : localItems.get(currentPeriod.getDbKey())) {
            if (item.getDbKey().equals(dbKey)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public Item getDeletedItem(@NonNull String dbKey) {
        for (Item item : localDeletedItems.get(currentPeriod.getDbKey())) {
            if (item.getDbKey().equals(dbKey)) {
                return item;
            }
        }
        return null;
    }

    private Item transformItem(String periodKey, DataSnapshot itemSnapshot, boolean deleted){
        Item newItem = new Item((String) itemSnapshot.child("description").getValue());
        newItem.setDbKey(itemSnapshot.getKey());

        newItem.setWeight((Long) itemSnapshot.child("weight").getValue());

        String reference = (String) itemSnapshot.child("reference").getValue();
        Criteria criteria = CategoryRepository.getInstance().getCriteriaFromReference(reference);
        newItem.setCriteria(criteria/*, deleted*/);
        if(periodKey.equals(currentPeriod.getDbKey())) {
            if (deleted) {
                criteria.addDeletedItem(newItem);
            } else {
                criteria.addItem(newItem);
            }
        }

        for (DataSnapshot tagSnapshot : itemSnapshot.child("tags").getChildren()) {
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
        return newItem;
    }

    @Override
    public void addNewItem(@NonNull DataSnapshot itemSnapshot, String periodDbKey, boolean deleted) {
        Item newItem = new Item((String) itemSnapshot.child("description").getValue());
        newItem.setDbKey(itemSnapshot.getKey());

        newItem.setWeight((Long) itemSnapshot.child("weight").getValue());

        String reference = (String) itemSnapshot.child("reference").getValue();
        Criteria criteria = CategoryRepository.getInstance().getCriteriaFromReference(reference);
        newItem.setCriteria(criteria, deleted);

        for (DataSnapshot tagSnapshot : itemSnapshot.child("tags").getChildren()) {
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
        addItem(newItem, periodDbKey, deleted);
    }

    // if no evaluation period(periodDbKey == null) is specified currentPeriod will be used
    @Override
    public void addItem(Item item, String periodDbKey, boolean flag) {
        String period = periodDbKey;
        if(periodDbKey == null){
            period = currentPeriod.getDbKey();
        }
        List<Item> itemDestination = (!flag ? localItems.get(period) :
                                                localDeletedItems.get(period));
        if(itemDestination == null){
            if(!flag){
                itemDestination = localItems.put(period, new LinkedList<Item>());
            }else{
                itemDestination = localDeletedItems.put(period, new LinkedList<Item>());
            }
        }
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
        // save items only occur in the current context of the app
        // so the period key can be null
        addItem(item, null, flag);
        saveItemToDatabase(item);
    }

    @Override
    public void editItem(Item item, String newDescription, Criteria newCriteria, long weight) {
        item.setDescription(newDescription);
        item.setWeight(weight);
        if (!item.getCriteria().equals(newCriteria)) {
            for (ItemFile file : item.getFiles()) {
                filesRepository.moveFile(file, newCriteria);
            }
            item.setCriteria(newCriteria, false);
        }
        // we can only edit items in the non-deleted list so it should always be false
        saveItem(item, false);
        // if the item has a deleted version(one or more files were deleted) we also need to update it
        if (localDeletedItems.get(currentPeriod.getDbKey()).contains(item)) {
            int pos = localDeletedItems.get(currentPeriod.getDbKey()).indexOf(item);
            Item deletedVersion = localDeletedItems.get(currentPeriod.getDbKey()).get(pos);
            deletedVersion.setDescription(item.getDescription());
            if (!deletedVersion.getCriteria().equals(newCriteria)) {
                for (ItemFile file : deletedVersion.getDeletedFiles()) {
                    filesRepository.moveFile(file, newCriteria);
                }
                //edit deleted version
                deletedVersion.setCriteria(newCriteria, true);
            }
            saveDeletedItemToDatabase(deletedVersion);
        }
    }

    @Override
    public void deleteLocalItem(@NonNull Item item, boolean listingDeleted) {
        if (!listingDeleted) {
            localItems.get(currentPeriod.getDbKey()).remove(item);
        } else {
            localDeletedItems.get(currentPeriod.getDbKey()).remove(item);
        }
    }

    @Override
    public void deleteItem(@NonNull Item item) {
        // move item to deleted-items and delete original
        localItems.remove(item);

        item.getCriteria().deleteItem(item);

        // if item is being deleted and already has a copy in deletedFiles then copy
        if (localDeletedItems.get(currentPeriod.getDbKey()).contains(item)) {
            int pos = localDeletedItems.get(currentPeriod.getDbKey()).indexOf(item);
            Item originalItem = localDeletedItems.get(currentPeriod.getDbKey()).get(pos);
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
            localDeletedItems.get(currentPeriod.getDbKey()).add(item);
        }

        item.clearFiles();
        saveDeletedItemToDatabase(item);
        getItemsReference().child(item.getDbKey()).removeValue();
    }

    @Override
    public void permanentlyDeleteItem(@NonNull Item item) {
        localDeletedItems.get(currentPeriod.getDbKey()).remove(item);
        localItems.get(currentPeriod.getDbKey()).remove(item);
        item.getCriteria().permanentlyDeleteItem(item);

        if (getItemsReference().child(item.getDbKey()) != null) {
            getItemsReference().child(item.getDbKey()).removeValue();
        }
        getDeletedItemsReference().child(item.getDbKey()).removeValue();

        for (ItemFile file : item.getDeletedFiles()) {
            filesRepository.permanentlyDeleteFile(file);
        }
    }

    @Override
    public void restoreItem(@NonNull Item item) {
        localDeletedItems.remove(item);
        item.getCriteria().restoreItem(item);

        // if item has deleted files but itself isn't deleted then it'll be in this list
        if (localItems.get(currentPeriod.getDbKey()).contains(item)) {
            int pos = localItems.get(currentPeriod.getDbKey()).indexOf(item);
            Item originalItem = localItems.get(currentPeriod.getDbKey()).get(pos);
            // move deleted files to original item
            for (ItemFile fileToRestore : item.getDeletedFiles()) {
                filesRepository.restoreFile(fileToRestore);
                fileToRestore.setDeleted(false);
                originalItem.addFile(fileToRestore);
            }
            item = originalItem;
        } else {
            for (ItemFile file : item.getDeletedFiles()) {
                filesRepository.restoreFile(file);
                file.setDeleted(false);
                item.addFile(file);
            }
            localItems.get(currentPeriod.getDbKey()).add(item);
        }

        item.clearDeletedFiles();

        saveItemToDatabase(item);
        getDeletedItemsReference().child(item.getDbKey()).removeValue();
    }

    @Override
    public void addFilesToItem(Item item, List<Uri> receivedFiles) {
        for (Uri uri : receivedFiles) {
            String filename = UriHelper.getFileName(Application.getAppContext(), uri);
            ItemFile file = new ItemFile(filename);

            // file doesn't have dbkey so it will compare by filename(see equals in ItemFile)
            if(item.getFiles().contains(file)){
                Log.d(TAG, "filename alread exists: " + filename);
                file.setFilename(getRepeatedFilename(item, filename));
                Log.d(TAG, "altered filename: " + file.getFilename());
            }
            item.addFile(file);
            filesRepository.saveFile(file, uri);
        }
        saveItem(item, false);
    }

    // this will make files with the same filename use the parenthesis method like Windows
    // where it'll add ([int]) to the end of the filename or increment if it exists
    private String getRepeatedFilename(Item item, String filename){
        String ext = filename.substring(filename.lastIndexOf(".")); // no need to cut '.'
        String nameNoExt = filename.substring(0, filename.lastIndexOf("."));
        if(nameNoExt.lastIndexOf("(") != -1 && nameNoExt.lastIndexOf(")") != -1){
            // char '(' and ')' exists in the name
            String valueInParenthesis =
                    nameNoExt.substring(nameNoExt.lastIndexOf("(")+1, nameNoExt.lastIndexOf(")"));

            // check if number in parenthesis is an integer to determine
            if(isInteger(valueInParenthesis)){
                String nameNoParenthesis = nameNoExt.substring(0, nameNoExt.lastIndexOf("("));
                int copyNum = Integer.parseInt(valueInParenthesis) + 1;
                nameNoExt = nameNoParenthesis + "(" + copyNum + ")";
            }else {
                // last block of parenthesis isn't a number
                // - invalid filename to increment copy number so add the default
                nameNoExt = nameNoExt.concat("(1)");
            }
        }else{
            nameNoExt = nameNoExt.concat("(1)");
        }

        String newEditedFilename = nameNoExt + ext;
        // confirm if new filename doesn't exist(in case it's being added again
        // ie. _name_(1) exists, return _name_(2)
        if(item.getFiles().contains(new ItemFile(newEditedFilename))){
            newEditedFilename = getRepeatedFilename(item, newEditedFilename);
        }

        return newEditedFilename;
    }

    // check if string is integer - faster than Integer.parseInt, regex and numberformatexception
    private boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public void addPendingFilesToItem(Item item, List<PendingFile> receivedPendingFiles) {
        for(PendingFile file: receivedPendingFiles){
            item.addFile(file.getItemFile());

            switch(file.getProvider()){
                case MEO_CLOUD:
                case DROPBOX:
                    filesRepository.movePendingFile(file, item, item.getCriteria());
                    break;
                case EMAIL:
                    File email = new File(Application.getAppContext().getFilesDir(), file.getFilename());
                    filesRepository.saveFile(file.getItemFile(), Uri.fromFile(email));
                    break;
            }
        }
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

    void saveDeletedItemToDatabase(Item item) {
        DatabaseReference deletedItemRef = getDeletedItemsReference();
        Map<String, Object> values = new HashMap<>();
        if (item.getDbKey() == null || item.getDbKey().isEmpty()) {
            deletedItemRef = deletedItemRef.push();
            item.setDbKey(deletedItemRef.getKey());
        } else {
            deletedItemRef = deletedItemRef.child(item.getDbKey());
        }

        if (!item.getDeletedFiles().isEmpty()) {
            Map<String, Object> deletedFileList = getFileList(deletedItemRef.child("files"), item.getDeletedFiles());
            values.put("files", deletedFileList);
        }
        values.put("reference", item.getCategoryReference());
        values.put("description", item.getDescription());
        values.put("weight", item.getWeight());
        values.put("tags", item.getTags());
        deletedItemRef.setValue(values);
    }

    void saveItemToDatabase(Item item) {
        DatabaseReference itemRef = getItemsReference();

        // when updating the item in db make sure it wasn't deleted otherwise
        // it'll create the item again after changes are made
        if (localItems.get(currentPeriod.getDbKey()).contains(item)) {
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

    private void saveDeletedItemToDatabase(String periodDbKey, Item item) {
        DatabaseReference deletedItemRef = deletedItemsReference.child(periodDbKey);

        Map<String, Object> values = new HashMap<>();
        if (item.getDbKey() == null || item.getDbKey().isEmpty()) {
            deletedItemRef = deletedItemRef.push();
            item.setDbKey(deletedItemRef.getKey());
        } else {
            deletedItemRef = deletedItemRef.child(item.getDbKey());
        }

        if (!item.getDeletedFiles().isEmpty()) {
            Map<String, Object> deletedFileList = getFileList(deletedItemRef.child("files"), item.getDeletedFiles());
            values.put("files", deletedFileList);
        }
        values.put("reference", item.getCategoryReference());
        values.put("description", item.getDescription());
        values.put("weight", item.getWeight());
        values.put("tags", item.getTags());
        deletedItemRef.setValue(values);
    }

    private void saveItemToDatabase(String periodDbKey, Item item){
        DatabaseReference itemRef = itemsReference.child(periodDbKey);

        // when updating the item in db make sure it wasn't deleted otherwise
        // it'll create the item again after changes are made
        if (localItems.get(periodDbKey).contains(item)) {
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
