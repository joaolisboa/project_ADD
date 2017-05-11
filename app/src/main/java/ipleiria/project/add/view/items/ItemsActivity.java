package ipleiria.project.add.view.items;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import ipleiria.project.add.R;
import ipleiria.project.add.utils.ActivityUtils;
import ipleiria.project.add.data.source.database.ItemsRepository;

import static ipleiria.project.add.view.items.ItemsPresenter.LIST_DELETED_KEY;

public class ItemsActivity extends AppCompatActivity{

    private ItemsPresenter itemsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_activity);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ItemsFragment itemsFragment = (ItemsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (itemsFragment == null) {
            // Create the fragment
            itemsFragment = ItemsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), itemsFragment, R.id.contentFrame);
        }

        // Create the presenter
        boolean listDeleted = getIntent().getBooleanExtra(LIST_DELETED_KEY, false);
        itemsPresenter = new ItemsPresenter(ItemsRepository.getInstance(), itemsFragment, listDeleted);
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

}
