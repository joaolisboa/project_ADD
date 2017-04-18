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

import java.util.List;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Item;

public class ListItemActivity extends AppCompatActivity {

    private ListView listView;
    private ListItemAdapter listViewAdapter;
    private List<Item> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        FirebaseHandler.getInstance().getItemsReference().addChildEventListener(itemsEventListener);

        listView = (ListView) findViewById(R.id.listview);
        list = ApplicationData.getInstance().getItems();
        listViewAdapter = new ListItemAdapter(this, list);
        listViewAdapter.setMode(com.daimajia.swipe.util.Attributes.Mode.Single);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((SwipeLayout)(listView.getChildAt(position - listView.getFirstVisiblePosition()))).open(true);
            }
        });
    }

    private void updateListView() {
        listView = (ListView) findViewById(R.id.listview);
        list = ApplicationData.getInstance().getItems();
        listViewAdapter = new ListItemAdapter(this, list);
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
            //recyclerView.getAdapter().notifyDataSetChanged();
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
