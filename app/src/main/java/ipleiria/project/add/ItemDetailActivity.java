package ipleiria.project.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.data.model.Item;

public class ItemDetailActivity extends AppCompatActivity {

    private Item item;

    private ListView listView;
    private ItemFileAdapter listFileAdapter;

    private boolean listDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        listView = (ListView) findViewById(R.id.listview);
        Intent intent = getIntent();
        listDeleted = getIntent().getBooleanExtra("list_deleted", false);
        item = ApplicationData.getInstance().getItemNonDescript(intent.getStringExtra("item_key"));

        if(listDeleted){
            ((TextView)findViewById(R.id.file_label_subheader)).setText("Deleted Files");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(item.getDescription());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setIntentInfo("Action", null).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        setResult(Activity.RESULT_OK);
        finish();
    }

    public void setFileListView(){
        listFileAdapter = new ItemFileAdapter(this, item, listDeleted);
        listFileAdapter.setMode(com.daimajia.swipe.util.Attributes.Mode.Single);
        listView.setAdapter(listFileAdapter);
    }

}
