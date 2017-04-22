package ipleiria.project.add;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.daimajia.swipe.SwipeLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;

public class ListItemActivity extends AppCompatActivity {

    private ListView listView;
    private ListItemAdapter listViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        FirebaseHandler.getInstance().getItemsReference().addChildEventListener(itemsEventListener);
        updateListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((SwipeLayout)(listView.getChildAt(position - listView.getFirstVisiblePosition()))).open(true);
            }
        });
    }

    private void updateListView() {
        listView = (ListView) findViewById(R.id.listview);
        listViewAdapter = new ListItemAdapter(this, ApplicationData.getInstance().getItems());
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
            newItem.setReference((String) dataSnapshot.child("reference").getValue());
            newItem.setDbKey(dataSnapshot.getKey());
            for (DataSnapshot fileSnapshot : dataSnapshot.child("files").getChildren()) {
                ItemFile file = fileSnapshot.getValue(ItemFile.class);
                file.setDbKey(fileSnapshot.getKey());
                newItem.addFile(file);
            }
            ApplicationData.getInstance().addItem(newItem);
            updateListView();
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
