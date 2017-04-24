package ipleiria.project.add;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Dimension;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;

public class ListItemActivity extends AppCompatActivity {

    private boolean listDeleted;

    private ListView listView;
    private ListItemAdapter listViewAdapter;

    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        spinner = (Spinner) t.findViewById(R.id.spinner_nav);

        listDeleted = getIntent().getBooleanExtra("list_deleted", false);

        List<String> filters = new LinkedList<>();
        filters.add("All");
        for(Dimension d: ApplicationData.getInstance().getDimensions()){
            filters.add(d.getName());
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        if(!listDeleted){
            FirebaseHandler.getInstance().getItemsReference().addChildEventListener(itemsEventListener);
        }else{
            FirebaseHandler.getInstance().getDeletedItemsReference().addChildEventListener(itemsEventListener);
        }

        updateListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //((SwipeLayout)(listView.getChildAt(position - listView.getFirstVisiblePosition()))).open(true);
                Toast.makeText(getApplicationContext(), "clicked item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.list_item_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private void updateListView() {
        listView = (ListView) findViewById(R.id.listview);
        if(!listDeleted){
            listViewAdapter = new ListItemAdapter(this, ApplicationData.getInstance().getItems(), listDeleted);
        }else{
            listViewAdapter = new ListItemAdapter(this, ApplicationData.getInstance().getDeletedItems(), listDeleted);
        }
        listViewAdapter.setMode(com.daimajia.swipe.util.Attributes.Mode.Single);
        listView.setAdapter(listViewAdapter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(!listDeleted){
            FirebaseHandler.getInstance().getItemsReference().removeEventListener(itemsEventListener);
        }else{
            FirebaseHandler.getInstance().getDeletedItemsReference().removeEventListener(itemsEventListener);
        }
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
            if(!listDeleted){
                ApplicationData.getInstance().addItem(newItem);
            }else{
                ApplicationData.getInstance().addDeletedItem(newItem);
            }
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
            if(!listDeleted){
                ApplicationData.getInstance().addItem(newItem);
            }else{
                ApplicationData.getInstance().addDeletedItem(newItem);
            }
            updateListView();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if(!listDeleted){
                ApplicationData.getInstance().deleteItem(dataSnapshot.getKey());
            }else{
                ApplicationData.getInstance().deleteDeletedItem(dataSnapshot.getKey());
            }
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
