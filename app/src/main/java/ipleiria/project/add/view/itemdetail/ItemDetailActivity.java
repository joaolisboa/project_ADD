package ipleiria.project.add.view.itemdetail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.database.ItemFilesRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.ActivityUtils;

import static ipleiria.project.add.view.categories.CategoriesPresenter.LIST_DELETED_KEY;
import static ipleiria.project.add.view.itemdetail.ItemDetailPresenter.ITEM_KEY;

/**
 * Created by Lisboa on 10-May-17.
 */

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.item_detail_activity);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ItemDetailFragment itemDetailFragment =
                (ItemDetailFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (itemDetailFragment == null) {
            itemDetailFragment = ItemDetailFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), itemDetailFragment, R.id.contentFrame);
        }

        boolean listDeleted = getIntent().getBooleanExtra(LIST_DELETED_KEY, false);

        String itemKey = getIntent().getStringExtra(ITEM_KEY);
        Item item;
        ItemsRepository itemsRepository = ItemsRepository.getInstance();
        if(!listDeleted){
            item = itemsRepository.getItem(itemKey);
        }else{
            item = itemsRepository.getDeletedItem(itemKey);
        }
        new ItemDetailPresenter(itemDetailFragment, FilesRepository.getInstance(),
                ItemFilesRepository.newInstance(item, itemsRepository), item, listDeleted);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if(!getIntent().getBooleanExtra(LIST_DELETED_KEY, false)){
            getMenuInflater().inflate(R.menu.item_detail_menu, menu);
        }
        return true;
    }

}
