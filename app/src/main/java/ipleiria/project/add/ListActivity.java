package ipleiria.project.add;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    ArrayAdapter<String> adapter;

    private LinkedList<String> list;
    private ListView l ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);
        l =(ListView) findViewById(R.id.listView);
        list = new LinkedList<>();
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
        l.setAdapter(adapter);

        File dir = getFilesDir();

        list.add("qweqeq");

        for(File f: dir.listFiles()){
            //TextView tview = new TextView(this);
            //tview.setText(f.getName());
            // listView.addHeaderView(tview);
            list.add(f.getName());

        }

    }


}
