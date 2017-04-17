package ipleiria.project.add;

import android.util.Log;

import com.dropbox.core.v2.users.FullAccount;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxGetAccount;
import ipleiria.project.add.MEOCloud.Data.Account;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOGetAccount;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Area;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Dimension;
import ipleiria.project.add.Model.Email;

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

    private FirebaseHandler() {
        database = FirebaseDatabase.getInstance();
        String userUID = ApplicationData.getInstance().getSharedPreferences().getString(FIREBASE_UID_KEY, null);
        if(userUID != null){
            ApplicationData.getInstance().setUserUID(userUID);
        }else{
            Log.e(TAG, "User is offline and has no UID stored in prefs - never opened app offline?");
        }
        initReferences();
    }

    private void initReferences(){
        if(ApplicationData.getInstance().getUserUID()!= null){
            userReference = database.getReference().child("users").child(ApplicationData.getInstance().getUserUID());
            userReference.keepSynced(true);
        }
        categoryReference = database.getReference().child("categories");
        categoryReference.keepSynced(true);
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
                    for (DataSnapshot areaSnap : dimensionSnap.child("areas").getChildren()) {
                        Area area = new Area(areaSnap.child("name").getValue(String.class),
                                areaSnap.child("reference").getValue(Integer.class));
                        for (DataSnapshot criteriaSnap : areaSnap.child("criterias").getChildren()) {
                            Criteria criteria = new Criteria(criteriaSnap.child("name").getValue(String.class),
                                    criteriaSnap.child("reference").getValue(Integer.class));
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

    public void readEmailsOnce(){

    }

    public void readEmails(){
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
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage(), databaseError.toException());
            }
        });
        if(MEOCloudClient.isClientInitialized()){
            new MEOGetAccount(new MEOCallback<Account>() {
                @Override
                public void onComplete(MEOCloudResponse<Account> result) {
                    ApplicationData.getInstance().addEmail(new Email(result.getResponse().getEmail(), true));
                }

                @Override
                public void onRequestError(HttpErrorException httpE) {}

                @Override
                public void onError(Exception e) {}
            }).execute();
        }
        if(DropboxClientFactory.isClientInitialized()){
            new DropboxGetAccount(DropboxClientFactory.getClient(), new DropboxGetAccount.Callback() {
                @Override
                public void onComplete(FullAccount result) {
                    ApplicationData.getInstance().addEmail(new Email(result.getEmail(), true));
                }

                @Override
                public void onError(Exception e) {}
            }).execute();
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
        for(Email email: emails) {
            DatabaseReference emailRef = userReference.child("emails");
            if(email.getDbKey() == null || email.getDbKey().isEmpty()){
                emailRef = emailRef.push();
            }else{
                emailRef = emailRef.child(email.getDbKey());
            }
            emailRef.child("email").setValue(email.getEmail());
            emailRef.child("verified").setValue(email.isVerified());
        }
    }

}
