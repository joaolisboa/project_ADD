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
    private DeletedItemAdapter listViewAdapter;
    private List<Item> deletedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        updateListView();
    }

    private void updateListView() {
        listView = (ListView) findViewById(R.id.listview);
        listViewAdapter = new DeletedItemAdapter(this, ApplicationData.getInstance().getDeletedItems());
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
            ((DeletedItemAdapter)listView.getAdapter()).updateListItems(ApplicationData.getInstance().getDeletedItems());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Item newItem = dataSnapshot.getValue(Item.class);
            newItem.setReference((String)dataSnapshot.child("reference").getValue());
            newItem.setDbKey(dataSnapshot.getKey());
            ApplicationData.getInstance().addItem(newItem);
            ((DeletedItemAdapter)listView.getAdapter()).updateListItems(ApplicationData.getInstance().getDeletedItems());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            ApplicationData.getInstance().deleteItem(dataSnapshot.getKey());
            ((DeletedItemAdapter)listView.getAdapter()).updateListItems(ApplicationData.getInstance().getDeletedItems());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
