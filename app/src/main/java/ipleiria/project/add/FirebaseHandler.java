package ipleiria.project.add;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.Model.Email;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.utils.FileUtils;

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

    public void readCategories(final Context context){
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
                            int readX = criteriaSnap.child("readX").getValue(Integer.class);
                            int readY = criteriaSnap.child("readY").getValue(Integer.class);
                            int writeX = criteriaSnap.child("writeX").getValue(Integer.class);
                            int writeY = criteriaSnap.child("writeY").getValue(Integer.class);
                            criteria.setReadCell(new Criteria.Coordinate(readX, readY));
                            criteria.setWriteCell(new Criteria.Coordinate(writeX, writeY));
                            criteria.setDbKey(criteriaSnap.getKey());
                            area.addCriteria(criteria);
                        }
                        dimension.addArea(area);
                    }
                    dimensions.add(dimension);
                }
                ApplicationData.getInstance().addDimensions(dimensions);
                //readExcel(context);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    private void readExcel(Context context) {
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        try {
            File file = FileUtils.getExcelFile(context);
            InputStream inputStream = new FileInputStream(file);
            Workbook wb = new XSSFWorkbook(inputStream);
            Sheet sheet = wb.getSheetAt(0);

            int y = 12;
            int writeX = 5;
            int readX = 14;
            for(int i = 0; i < 4; i++){
                Criteria criteria = ApplicationData.getInstance().getCriterias().get(i);
                criteria.setReadCell(new Criteria.Coordinate(readX, y));
                criteria.setWriteCell(new Criteria.Coordinate(writeX, y));
            }
            y+=4;
            int lastDimension = 1;
            for(int i = 4; i < ApplicationData.getInstance().getCriterias().size(); i++){
                Criteria criteria = ApplicationData.getInstance().getCriterias().get(i);
                if(criteria.getDimension().getReference() != lastDimension){
                    lastDimension = criteria.getDimension().getReference();
                    if(lastDimension==3){
                        y++;
                    }
                    y++;
                }
                criteria.setReadCell(new Criteria.Coordinate(readX, y));
                criteria.setWriteCell(new Criteria.Coordinate(writeX, y));
                y++;
            }
            sheet.getRow(12).getCell(5).setCellValue(10);
            try (FileOutputStream stream = new FileOutputStream(file)) {
                wb.write(stream);
            }
            wb.close();
            inputStream.close();
            //writeCategories();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void writeCategories(){
        DatabaseReference categoryRef = categoryReference;
        for (Dimension dimension : ApplicationData.getInstance().getDimensions()) {
            DatabaseReference dimRef = categoryRef.push();
            dimRef.child("name").setValue(dimension.getName());
            dimRef.child("reference").setValue(dimension.getReference());

            for (Area area : dimension.getAreas()) {
                DatabaseReference areaRef = dimRef.child("areas").push();
                areaRef.child("name").setValue(area.getName());
                areaRef.child("reference").setValue(area.getReference());

                for (Criteria criteria : area.getCriterias()) {
                    DatabaseReference criteriaRef = areaRef.child("criterias").push();
                    criteriaRef.child("name").setValue(criteria.getName());
                    criteriaRef.child("reference").setValue(criteria.getReference());
                    criteriaRef.child("readX").setValue(criteria.getReadCell().x);
                    criteriaRef.child("readY").setValue(criteria.getReadCell().y);
                    criteriaRef.child("writeX").setValue(criteria.getWriteCell().x);
                    criteriaRef.child("writeY").setValue(criteria.getWriteCell().y);
                }
            }
        }
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

    public void readUserData(final Context context){
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ApplicationData.getInstance().setDisplayName((String)dataSnapshot.child("name").getValue());
                for (DataSnapshot emailsSnapshot : dataSnapshot.child("emails").getChildren()) {
                    Email newEmail = emailsSnapshot.getValue(Email.class);
                    newEmail.setDbKey(emailsSnapshot.getKey());
                    ApplicationData.getInstance().addEmail(newEmail);
                }
                if(context instanceof MainActivity){
                    ((MainActivity)context).updateUserInfo();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
}
