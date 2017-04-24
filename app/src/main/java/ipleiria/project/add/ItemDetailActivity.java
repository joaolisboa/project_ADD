package ipleiria.project.add;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.daimajia.swipe.SwipeLayout;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;

public class ItemDetailActivity extends AppCompatActivity {

    private Item item;

    private ListView listView;
    private ItemFileAdapter listFileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        listView = (ListView) findViewById(R.id.listview);
        Intent intent = getIntent();
        item = ApplicationData.getInstance().getItem(intent.getStringExtra("item_key"));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(item.getDescription());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


        setFileListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((SwipeLayout)(listView.getChildAt(position - listView.getFirstVisiblePosition()))).open(true);
            }
        });

    }

    private void setFileListView(){
        List<ItemFile> files = new LinkedList<>();
        /*if (!listDeleted) {
            items = ApplicationData.getInstance().getItems();
        } else {
            items = ApplicationData.getInstance().getDeletedItems();
        }*/
        files = item.getFiles();
        listFileAdapter = new ItemFileAdapter(this, item, false);
        listFileAdapter.setMode(com.daimajia.swipe.util.Attributes.Mode.Single);
        listView.setAdapter(listFileAdapter);
    }

}
