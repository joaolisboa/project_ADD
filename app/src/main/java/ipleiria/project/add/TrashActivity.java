package ipleiria.project.add;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;

public class TrashActivity extends AppCompatActivity {

    private ListView listView;
    private ListItemAdapter listViewAdapter;
    private List<Item> deletedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        deletedItems = ApplicationData.getInstance().getDeletedItems();

        // these deleted items below are items that include delete files
        // but not all are deleted or the item isn't deleted
        // if the item is fully deleted then it's part of the above list
        for(Item i: ApplicationData.getInstance().getItems()){
            for(ItemFile file: i.getFilenames()){
                if(file.isDeleted()){
                    deletedItems.add(i);
                }
            }
        }
    }

    private void updateListView() {
        listView = (ListView) findViewById(R.id.listview);
        deletedItems = ApplicationData.getInstance().getItems();
        listViewAdapter = new ListItemAdapter(this, deletedItems);
        listViewAdapter.setMode(com.daimajia.swipe.util.Attributes.Mode.Single);
        listView.setAdapter(listViewAdapter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        FirebaseHandler.getInstance().getItemsReference().removeEventListener(itemsEventListener);
        FirebaseHandler.getInstance().readItems();
    }


    private ChildEventListener itemsEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Item newItem = dataSnapshot.getValue(Item.class);
            newItem.setReference((String)dataSnapshot.child("reference").getValue());
            newItem.setDbKey(dataSnapshot.getKey());
            ApplicationData.getInstance().addItem(newItem);
            updateListView();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Item newItem = dataSnapshot.getValue(Item.class);
            newItem.setReference((String)dataSnapshot.child("reference").getValue());
            newItem.setDbKey(dataSnapshot.getKey());
            ApplicationData.getInstance().addItem(newItem);
            updateListView();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            ApplicationData.getInstance().deleteItem(dataSnapshot.getKey());
            updateListView();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
