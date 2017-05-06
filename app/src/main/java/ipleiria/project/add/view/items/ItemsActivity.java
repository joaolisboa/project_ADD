package ipleiria.project.add.view.items;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.FirebaseHandler;
import ipleiria.project.add.ListItemAdapter;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.R;
import ipleiria.project.add.Utils.ActivityUtils;
import ipleiria.project.add.Utils.CloudHandler;
import ipleiria.project.add.Utils.FileUtils;
import ipleiria.project.add.Utils.NetworkState;
import ipleiria.project.add.Utils.StringUtils;
import ipleiria.project.add.Utils.UriHelper;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.source.ItemsRepository;

import static ipleiria.project.add.AddItemActivity.SENDING_PHOTO;

public class ItemsActivity extends AppCompatActivity implements ItemsContract.ItemsActivityView{

    private static final String TAG = "LIST_ITEM_ACTIVITY";
    private static final String CURRENT_FILTERING_KEY = "ITEMS_FILTER";

    public static final String LIST_DELETED_KEY = "list_deleted";
    public static final int CHANGING_DATA_SET = 2001;

    private boolean listDeleted;

    private ItemsPresenter itemsPresenter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private ListItemAdapter listViewAdapter;
    private List<Item> items;

    private List<Uri> receivedFiles;

    private String action;

    private Spinner spinnerFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_activity);

        final Intent intent = getIntent();
        final String action = intent.getAction();
        listDeleted = intent.getBooleanExtra(LIST_DELETED_KEY, false);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        spinnerFilter = (Spinner) t.findViewById(R.id.spinner_nav);

        ItemsFragment itemsFragment = (ItemsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (itemsFragment == null) {
            // Create the fragment
            itemsFragment = ItemsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), itemsFragment, R.id.contentFrame);
        }

        // Create the presenter
        itemsPresenter = new ItemsPresenter(ItemsRepository.getInstance(), itemsFragment, this, listDeleted);
    }

    // TODO: 06-May-17 refactor file share
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
            itemFiles.add(new ItemFile(UriHelper.getFileName(ItemsActivity.this, uri)));
        }
        itemAtPosition.addFiles(itemFiles);
        if (NetworkState.isOnline(this)) {
            for(int i = 0; i < receivedFiles.size(); i++){
                Log.d("FILE_UPLOAD", "uploading file: " + UriHelper.getFileName(ItemsActivity.this, receivedFiles.get(i)));
                CloudHandler.uploadFileToCloud(this, receivedFiles.get(i),
                        itemFiles.get(i), itemAtPosition.getCriteria());
            }
        }else{
            for(int i = 0; i < receivedFiles.size(); i++){
                FileUtils.copyFileToLocalDir(this, receivedFiles.get(i), itemAtPosition.getCriteria());
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
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                itemsPresenter.searchItems(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        itemsPresenter.result(requestCode, resultCode);
    }

    @Override
    public void setFilters(List<String> filters) {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, filters);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerArrayAdapter);
        spinnerFilter.setSelection(0);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemsPresenter.setFiltering(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
    }
}
