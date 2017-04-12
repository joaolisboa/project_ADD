package ipleiria.project.add;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Area;
import ipleiria.project.add.Model.Category;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Dimension;

public class SelectCategoryActivity extends AppCompatActivity {

    private TreeNode treeRoot;
    private AndroidTreeView tView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);
        ViewGroup containerView = (ViewGroup)findViewById(R.id.container);

        treeRoot = TreeNode.root();

        for(Dimension dimension: ApplicationData.getInstance().getDimensions()){
            TreeNode parent = new TreeNode(dimension);
            for(Area area: dimension.getAreas()){
                TreeNode level1 = new TreeNode(area);
                for(Criteria criteria: area.getCriterias()){
                    TreeNode level2 = new TreeNode(criteria);
                    level2.setViewHolder(new NodeHolder(SelectCategoryActivity.this, R.layout.node_holder_criteria));
                    level1.addChild(level2);
                }
                level1.setViewHolder(new NodeHolder(SelectCategoryActivity.this, R.layout.node_holder_area));
                parent.addChild(level1);
            }
            parent.setViewHolder(new NodeHolder(SelectCategoryActivity.this, R.layout.node_holder_dimension));
            treeRoot.addChild(parent);
        }

        tView = new AndroidTreeView(SelectCategoryActivity.this, treeRoot);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setDefaultNodeLongClickListener(nodeLongClickListener);
        tView.setUse2dScroll(true);
        tView.setDefaultAnimation(true);
        containerView.addView(tView.getView());

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                tView.restoreState(state);
            }
        }

    }

    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            Category item = (Category) value;
            if(item instanceof Criteria){
                Toast.makeText(SelectCategoryActivity.this, ((Criteria) item).getRealReference(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            //IconTreeItemHolder.IconTreeItem item = (IconTreeItemHolder.IconTreeItem) value;
            //Toast.makeText(getActivity(), "Long click: " + item.text, Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tState", tView.getSaveState());
    }


}
