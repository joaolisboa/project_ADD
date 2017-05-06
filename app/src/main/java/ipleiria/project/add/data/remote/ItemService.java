package ipleiria.project.add.data.remote;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.User;

/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemService {

    private User user;
    private DatabaseReference databaseRef;

    public ItemService(User user) {
        this.user = user;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public DatabaseReference getItems() {
        DatabaseReference itemsReference = databaseRef.child("items").child(user.getUid());
        itemsReference.keepSynced(true);
        return itemsReference;
    }

    public void setItem(Item item) {
        // TODO: 04-May-17  write item to firebase
    }

}
