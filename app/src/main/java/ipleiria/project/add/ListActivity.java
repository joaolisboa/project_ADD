package ipleiria.project.add;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        File dir = getFilesDir();
        ListView listView = (ListView)findViewById(R.id.listView);

        for(File f: dir.listFiles()){
            TextView tview = new TextView(this);
            tview.setText(f.getName());
            listView.addHeaderView(tview);
        }

    }
}
