package ipleiria.project.add.data.remote;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Lisboa on 04-May-17.
 */

public class CategoryService {

    private DatabaseReference databaseRef;

    public CategoryService() {
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public DatabaseReference getCategories() {
        DatabaseReference categoriesReference = databaseRef.child("categories");
        categoriesReference.keepSynced(true);
        return categoriesReference;
    }

}
