package ipleiria.project.add;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Area;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Dimension;

public class SelectCategoryActivity extends AppCompatActivity {

    private TreeNode treeRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        treeRoot = TreeNode.root();

        for(Dimension dimension: ApplicationData.getInstance().getDimensions()){
            TreeNode parent = new TreeNode(dimension.getReference() + ". " + dimension.getName());
            for(Area area: dimension.getAreas()){
                TreeNode level1 = new TreeNode(area.getReference() + ". " + area.getName());
                for(Criteria criteria: area.getCriterias()){
                    TreeNode level2 = new TreeNode(criteria.getReference() + ". " + criteria.getName());
                    level1.addChild(level2);
                }
                parent.addChild(level1);
            }
            treeRoot.addChild(parent);
        }

        ViewGroup containerView = (ViewGroup)findViewById(R.id.container);
        AndroidTreeView tView = new AndroidTreeView(SelectCategoryActivity.this, treeRoot);
        containerView.addView(tView.getView());



    }


}
