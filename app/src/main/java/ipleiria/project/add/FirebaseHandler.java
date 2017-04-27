package ipleiria.project.add;

import android.content.Context;
import android.util.Log;

import com.dropbox.core.v2.users.FullAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxGetAccount;
import ipleiria.project.add.MEOCloud.Data.Account;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOGetAccount;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Area;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Dimension;
import ipleiria.project.add.Model.Email;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;
import ipleiria.project.add.Utils.CloudHandler;
import ipleiria.project.add.Utils.FileUtils;
import ipleiria.project.add.Utils.NetworkState;

import static ipleiria.project.add.SettingsActivity.DROPBOX_PREFS_KEY;
import static ipleiria.project.add.SettingsActivity.MEO_PREFS_KEY;

/**
 * Created by Lisboa on 15-Apr-17.
 */

public class FirebaseHandler {

    public static final String FIREBASE_UID_KEY = "firebase_user_uid";
    private static final String TAG = "FirebaseHandler";
    private static FirebaseHandler instance = null;

    private FirebaseDatabase database;
    private DatabaseReference userReference;
    private DatabaseReference categoryReference;
    private DatabaseReference itemsReference;
    private DatabaseReference deletedItemsReference;

    private FirebaseHandler() {
        database = FirebaseDatabase.getInstance();
        categoryReference = database.getReference().child("categories");
        categoryReference.keepSynced(true);

        String userUID = ApplicationData.getInstance().getSharedPreferences().getString(FIREBASE_UID_KEY, null);
        if(userUID != null){
            ApplicationData.getInstance().setUserUID(userUID);
        }else{
            Log.e(TAG, "User is offline and has no UID stored in prefs - never opened app offline?");
        }
        initReferences();
    }

    public void initReferences(){
        if(ApplicationData.getInstance().getUserUID()!= null){
            userReference = database.getReference().child("users").child(ApplicationData.getInstance().getUserUID());
            userReference.keepSynced(true);
            itemsReference = database.getReference().child("items").child(ApplicationData.getInstance().getUserUID());
            itemsReference.keepSynced(true);
            deletedItemsReference = database.getReference().child("deleted-items").child(ApplicationData.getInstance().getUserUID());
            deletedItemsReference.keepSynced(true);
        }
    }

    public static FirebaseHandler getInstance() {
        if (instance == null) {
            instance = new FirebaseHandler();
        }
        return instance;
    }

    public static FirebaseHandler newInstance() {
        instance = new FirebaseHandler();
        return instance;
    }

    public void readCategories(){
        categoryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Dimension> dimensions = new LinkedList<>();
                for (DataSnapshot dimensionSnap : dataSnapshot.getChildren()) {
                    Dimension dimension = new Dimension(dimensionSnap.child("name").getValue(String.class),
                            dimensionSnap.child("reference").getValue(Integer.class));
                    dimension.setDbKey(dimensionSnap.getKey());
                    for (DataSnapshot areaSnap : dimensionSnap.child("areas").getChildren()) {
                        Area area = new Area(areaSnap.child("name").getValue(String.class),
                                areaSnap.child("reference").getValue(Integer.class));
                        area.setDbKey(areaSnap.getKey());
                        for (DataSnapshot criteriaSnap : areaSnap.child("criterias").getChildren()) {
                            Criteria criteria = new Criteria(criteriaSnap.child("name").getValue(String.class),
                                    criteriaSnap.child("reference").getValue(Integer.class));
                            criteria.setDbKey(criteriaSnap.getKey());
                            area.addCriteria(criteria);
                        }
                        dimension.addArea(area);
                    }
                    dimensions.add(dimension);
                }
                ApplicationData.getInstance().addDimensions(dimensions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    private void setUserReferenceListener(){
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "Value is: " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                System.out.println(error.getDetails());
                System.out.println(error.getMessage());
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void readEmails(Context context){
        DatabaseReference emailRef = userReference.child("emails");
        emailRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Email newEmail = dataSnapshot.getValue(Email.class);
                newEmail.setDbKey(dataSnapshot.getKey());
                ApplicationData.getInstance().addEmail(newEmail);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Email newEmail = dataSnapshot.getValue(Email.class);
                newEmail.setDbKey(dataSnapshot.getKey());
                ApplicationData.getInstance().addEmail(newEmail);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage(), databaseError.toException());
            }
        });
        if(NetworkState.isOnline(context)) {
            if (MEOCloudClient.isClientInitialized()) {
                new MEOGetAccount(new MEOCallback<Account>() {
                    @Override
                    public void onComplete(Account result) {
                        ApplicationData.getInstance().addEmail(new Email(result.getEmail(), true));
                    }

                    @Override
                    public void onRequestError(HttpErrorException httpE) {
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                }).execute();
            }
            if (DropboxClientFactory.isClientInitialized()) {
                new DropboxGetAccount(DropboxClientFactory.getClient(), new DropboxGetAccount.Callback() {
                    @Override
                    public void onComplete(FullAccount result) {
                        ApplicationData.getInstance().addEmail(new Email(result.getEmail(), true));
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                }).execute();
            }
        }
    }

    public void writeUserInfo(){
        if(userReference == null){
            initReferences();
        }
        if(userReference != null){
            userReference.child("name").setValue(ApplicationData.getInstance().getDisplayName());
        }
    }

    public void writeEmails(){
        List<Email> emails = ApplicationData.getInstance().getEmails();
        System.out.println("writing emails " + emails);
        Map<String, Object> values = new HashMap<>();
        for(Email email: emails) {
            DatabaseReference emailRef = userReference.child("emails");
            if(email.getDbKey() == null || email.getDbKey().isEmpty()){
                emailRef = emailRef.push();
            }else{
                emailRef = emailRef.child(email.getDbKey());
            }
            values.put("email", email.getEmail());
            values.put("verified", email.isVerified());
            emailRef.setValue(values);
        }
    }

    public void writeItems(){
        for(Item item: ApplicationData.getInstance().getItems()) {
            writeItem(item);
        }
    }

    public void writeItem(Item item){
        System.out.println(item);
        DatabaseReference itemRef = itemsReference.getRef();
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

    public void readUserData(){
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ApplicationData.getInstance().setDisplayName((String)dataSnapshot.child("name").getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeItemsListener(){
        itemsReference.removeEventListener(itemsEventListener);
    }

    public void readItems(){
        itemsReference.addChildEventListener(itemsEventListener);
    }

    public DatabaseReference getUserReference() {
        return userReference;
    }

    public DatabaseReference getCategoryReference() {
        return categoryReference;
    }

    public DatabaseReference getItemsReference() {
        return itemsReference;
    }

    public DatabaseReference getDeletedItemsReference(){
        return deletedItemsReference;
    }

    private ChildEventListener itemsEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Item newItem = dataSnapshot.getValue(Item.class);
            newItem.setReference((String) dataSnapshot.child("reference").getValue());
            newItem.setDbKey(dataSnapshot.getKey());
            for (DataSnapshot fileSnapshot : dataSnapshot.child("files").getChildren()) {
                ItemFile file = fileSnapshot.getValue(ItemFile.class);
                file.setDbKey(fileSnapshot.getKey());
                newItem.addFile(file);
            }
            ApplicationData.getInstance().addItem(newItem);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Item newItem = dataSnapshot.getValue(Item.class);
            newItem.setReference((String)dataSnapshot.child("reference").getValue());
            newItem.setDbKey(dataSnapshot.getKey());
            for(DataSnapshot fileSnapshot: dataSnapshot.child("files").getChildren()){
                ItemFile file = fileSnapshot.getValue(ItemFile.class);
                file.setDbKey(fileSnapshot.getKey());
                newItem.addFile(file);
            }
            ApplicationData.getInstance().addItem(newItem);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            ApplicationData.getInstance().deleteItem(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener deletedItemsEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Item newItem = dataSnapshot.getValue(Item.class);
            newItem.setReference((String) dataSnapshot.child("reference").getValue());
            newItem.setDbKey(dataSnapshot.getKey());
            for (DataSnapshot fileSnapshot : dataSnapshot.child("files").getChildren()) {
                ItemFile file = fileSnapshot.getValue(ItemFile.class);
                file.setDbKey(fileSnapshot.getKey());
                newItem.addFile(file);
            }
            ApplicationData.getInstance().addDeletedItem(newItem);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Item newItem = dataSnapshot.getValue(Item.class);
            newItem.setReference((String)dataSnapshot.child("reference").getValue());
            newItem.setDbKey(dataSnapshot.getKey());
            for(DataSnapshot fileSnapshot: dataSnapshot.child("files").getChildren()){
                ItemFile file = fileSnapshot.getValue(ItemFile.class);
                file.setDbKey(fileSnapshot.getKey());
                newItem.addFile(file);
            }
            ApplicationData.getInstance().addDeletedItem(newItem);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            ApplicationData.getInstance().deleteDeletedItem(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void writeDeletedItem(Item item){
        System.out.println(item);
        DatabaseReference itemRef = deletedItemsReference.getRef();
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

    public void deleteItem(final Item item){
        writeDeletedItem(item);
        itemsReference.child(item.getDbKey()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                System.out.println("Item deleted: " + item.getDbKey());
            }
        });
    }

    public void permanentlyDeleteItem(final Item item){
        deletedItemsReference.child(item.getDbKey()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                System.out.println("Item permanently deleted: " + item.getDbKey());
            }
        });
    }

    public void restoreItem(Item item){
        writeItem(item);
        permanentlyDeleteItem(item);
    }

    public void readDeletedItems() {
        deletedItemsReference.addChildEventListener(deletedItemsEventListener);
    }
}
