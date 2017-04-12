package ipleiria.project.add;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.unnamed.b.atv.model.TreeNode;

public class SelectCategoryActivity extends AppCompatActivity {

    private TreeNode treeRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        treeRoot = TreeNode.root();




    }


}
