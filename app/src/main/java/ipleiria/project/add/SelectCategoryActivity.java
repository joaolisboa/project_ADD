package ipleiria.project.add;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;

public class SelectCategoryActivity extends AppCompatActivity {

    private ViewGroup containerView;

    private TreeNode treeRoot;
    private AndroidTreeView tView;
    private LinkedList<Criteria> searchResults;
    private ListView searchListView;
    ArrayAdapter<Criteria> adapter;
    private List<Criteria> criterias;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);
        containerView = (ViewGroup) findViewById(R.id.container);

        SearchView search = (SearchView) findViewById(R.id.searchText);
        final SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchListView = (ListView) findViewById(R.id.listviewSeach);
        searchResults = new LinkedList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchResults);
        searchListView.setAdapter(adapter);

        searchListView.setOnItemClickListener(searchItemClickListener);
        search.setOnQueryTextListener(searchQueryListener);

        criterias = new LinkedList<>();
        criterias = ApplicationData.getInstance().getCriterias();
        treeRoot = TreeNode.root();
        createTreeView();

        tView = new AndroidTreeView(SelectCategoryActivity.this, treeRoot);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setDefaultNodeLongClickListener(nodeLongClickListener);
        tView.setUse2dScroll(false);
        tView.setDefaultAnimation(true);
        containerView.addView(tView.getView());

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                tView.restoreState(state);
            }
        }

    }


    public void createTreeView() {
        for (Dimension dimension : ApplicationData.getInstance().getDimensions()) {
            TreeNode parent = new TreeNode(dimension);
            for (Area area : dimension.getAreas()) {
                TreeNode level1 = new TreeNode(area);
                for (Criteria criteria : area.getCriterias()) {
                    TreeNode level2 = new TreeNode(criteria);
                    level2.setViewHolder(new CategoryNodeHolder(SelectCategoryActivity.this, R.layout.node_holder_criteria));
                    level1.addChild(level2);
                }
                level1.setViewHolder(new CategoryNodeHolder(SelectCategoryActivity.this, R.layout.node_holder_area));
                parent.addChild(level1);
            }
            parent.setViewHolder(new CategoryNodeHolder(SelectCategoryActivity.this, R.layout.node_holder_dimension));
            treeRoot.addChild(parent);
        }
    }

    private SearchView.OnQueryTextListener searchQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            searchResults.clear();
            if (TextUtils.isEmpty(newText)) {
                searchListView.setVisibility(View.GONE);
                containerView.setVisibility(View.VISIBLE);
            } else {
                String query = newText.toLowerCase();
                for (Criteria criterio : criterias) {
                    if (criterio.contains(query)) {
                        searchResults.add(criterio);
                    }
                }
                searchListView.setVisibility(View.VISIBLE);
                containerView.setVisibility(View.GONE);
            }
            searchListView.setAdapter(adapter);
            return false;
        }
    };

    private ListView.OnItemClickListener searchItemClickListener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Category item = (Category) searchListView.getItemAtPosition(position);
            if (item instanceof Criteria) {
                Toast.makeText(SelectCategoryActivity.this, ((Criteria) item).getRealReference(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            Category item = (Category) value;
            if (item instanceof Criteria) {
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
