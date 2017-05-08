package ipleiria.project.add.view.items;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.Utils.ActivityUtils;
import ipleiria.project.add.data.source.ItemsRepository;
import ipleiria.project.add.view.add_edit_item.AddEditActivity;

public class ItemsActivity extends AppCompatActivity implements ItemsContract.ItemsActivityView{

    private static final String TAG = "LIST_ITEM_ACTIVITY";
    private static final String CURRENT_FILTERING_KEY = "ITEMS_FILTER";

    public static final String LIST_DELETED_KEY = "list_deleted";
    public static final int CHANGING_DATA_SET = 2001;

    private ItemsPresenter itemsPresenter;

    private Spinner spinnerFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_activity);

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
        boolean listDeleted = getIntent().getBooleanExtra(LIST_DELETED_KEY, false);
        itemsPresenter = new ItemsPresenter(ItemsRepository.getInstance(), itemsFragment, this, listDeleted);
        itemsPresenter.setIntentInfo(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items_activity_menu, menu);

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
