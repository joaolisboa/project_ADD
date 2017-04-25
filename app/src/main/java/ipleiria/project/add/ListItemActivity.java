package ipleiria.project.add;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Dimension;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;
import ipleiria.project.add.Utils.CloudHandler;
import ipleiria.project.add.Utils.NetworkState;
import ipleiria.project.add.Utils.StringUtils;
import ipleiria.project.add.Utils.UriHelper;

import static ipleiria.project.add.AddItemActivity.SENDING_PHOTO;

public class ListItemActivity extends AppCompatActivity {

    private boolean listDeleted;

    private ListView listView;
    private ListItemAdapter listViewAdapter;
    private List<Item> items;

    private List<Uri> receivedFiles;

    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Spinner spinner = (Spinner) t.findViewById(R.id.spinner_nav);
        listView = (ListView) findViewById(R.id.listview);
        listDeleted = getIntent().getBooleanExtra("list_deleted", false);

        List<String> filters = new LinkedList<>();
        filters.add("All");
        for(Dimension d: ApplicationData.getInstance().getDimensions()){
            filters.add(d.getName());
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, filters);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                items = new LinkedList<>();
                if (position == 0) {
                    updateListView();
                } else {
                    for (Item i : ApplicationData.getInstance().getItems()) {
                        if (i.getDimension().getReference() == position) {
                            items.add(i);
                        }
                    }
                    listViewAdapter = new ListItemAdapter(ListItemActivity.this, items, listDeleted, action);
                    listViewAdapter.setMode(com.daimajia.swipe.util.Attributes.Mode.Single);
                    listView.setAdapter(listViewAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updateListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(action != null){
                    addFilesToItem((Item)parent.getItemAtPosition(position), intent);
                }else{
                    ((SwipeLayout)(listView.getChildAt(position - listView.getFirstVisiblePosition()))).open(true);
                }
            }
        });

        if(action != null){
            this.action = action;
            Button addNew = (Button) findViewById(R.id.add_new_button);
            addNew.setVisibility(View.VISIBLE);
            addNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent.setComponent(new ComponentName(ListItemActivity.this, AddItemActivity.class));
                    startActivity(intent);
                }
            });
        }
    }

    private void addFilesToItem(Item itemAtPosition, Intent intent) {
        receivedFiles = new LinkedList<>();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handleSingleFile(intent);
        } else if (SENDING_PHOTO.equals(action)){
            handleFile(Uri.parse(intent.getStringExtra( "photo_uri")));

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            handleMultipleFiles(intent);
        }
        List<ItemFile> itemFiles = new LinkedList<>();
        for (Uri uri : receivedFiles) {
            itemFiles.add(new ItemFile(UriHelper.getFileName(ListItemActivity.this, uri)));
        }
        itemAtPosition.addFiles(itemFiles);
        if (NetworkState.isOnline(this)) {
            for(int i = 0; i < receivedFiles.size(); i++){
                Log.d("FILE_UPLOAD", "uploading file: " + UriHelper.getFileName(ListItemActivity.this, receivedFiles.get(i)));
                CloudHandler.uploadFileToCloud(this, receivedFiles.get(i),
                        itemFiles.get(i), itemAtPosition.getCriteria());
            }
        }
        if (ApplicationData.getInstance().getUserUID() != null) {
            FirebaseHandler.getInstance().writeItem(itemAtPosition);
        }
        Toast.makeText(this, "File added to item", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleFile(Uri uri) {
        receivedFiles.add(uri);
    }

    private void handleSingleFile(Intent intent) {
        handleFile((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
    }

    private void handleMultipleFiles(Intent intent) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris != null) {
            for (Uri uri : uris) {
                handleFile(uri);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                List<Item> pesquisa = new LinkedList<>();

                newText = StringUtils.removeDiacriticalMarks(newText);

                if (TextUtils.isEmpty(newText)) {
                    pesquisa = items;
                } else {
                    String query = newText.toLowerCase();
                    for (Item i : items) {
                        String iString = StringUtils.removeDiacriticalMarks(i.getCriteria().getName().toLowerCase());
                        String iDesc = StringUtils.removeDiacriticalMarks(i.getDescription().toLowerCase());
                        if (iString.contains(query) || iDesc.contains(query)) {
                            pesquisa.add(i);
                        }
                    }

                }
                listViewAdapter = new ListItemAdapter(ListItemActivity.this, pesquisa, listDeleted, action);
                listViewAdapter.setMode(com.daimajia.swipe.util.Attributes.Mode.Single);
                listView.setAdapter(listViewAdapter);
                return false;
            }
        });

        return true;
    }
    private void updateListView() {
        items = new LinkedList<>();
        if (!listDeleted) {
            items = ApplicationData.getInstance().getItems();
        } else {
            items = ApplicationData.getInstance().getDeletedItems();
        }
        listViewAdapter = new ListItemAdapter(this, items, listDeleted, action);
        listViewAdapter.setMode(com.daimajia.swipe.util.Attributes.Mode.Single);
        listView.setAdapter(listViewAdapter);
    }
}
