package ipleiria.project.add.view.add_edit_item;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.view.CategoryNodeHolder;
import ipleiria.project.add.R;
import ipleiria.project.add.utils.UriHelper;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.view.categories.CategoryAdapter;

import static android.content.Context.SEARCH_SERVICE;
import static ipleiria.project.add.view.categories.CategoriesPresenter.OPEN_ITEM_ADDED;

/**
 * Created by Lisboa on 07-May-17.
 */

public class AddEditFragment extends Fragment implements AddEditContract.View{

    private static final String TAG = "AddEditFragment";
    public static final String SENDING_PHOTO = "SendingPhotoAction";
    public static final String SENDING_PENDING_FILES = "SendingPendingFilesAction";

    private ViewGroup containerView;
    private EditText descriptionEditText;
    private TextView categoryTitle;
    private TextView filenameView;
    private TextView scoreView;
    private ListView searchListView;
    private SearchView searchView;
    private AndroidTreeView tView;

    private ProgressDialog progressDialog;
    private FloatingActionButton fab;

    private AddEditContract.Presenter addEditPresenter;

    public AddEditFragment() {}

    public static AddEditFragment newInstance() {
        return new AddEditFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.add_edit_frag, container, false);

        containerView = (ViewGroup) root.findViewById(R.id.container);
        descriptionEditText = (EditText) root.findViewById(R.id.item_description);
        categoryTitle = (TextView) root.findViewById(R.id.category_title);
        filenameView = (TextView) root.findViewById(R.id.filename);
        searchListView = (ListView) root.findViewById(R.id.listviewSeach);
        searchView = (SearchView) root.findViewById(R.id.searchText);
        scoreView = (EditText) root.findViewById(R.id.score_id);

        SearchManager searchManager = (SearchManager) getContext().getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchListView.setOnItemClickListener(searchItemClickListener);
        searchView.setOnQueryTextListener(searchQueryListener);

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                tView.restoreState(state);
            }
        }

        scoreView.setText("1");
        scoreView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addEditPresenter.setWeight(scoreView.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addEditPresenter.setDescription(descriptionEditText.getText().toString());
            }
        });

        // Set up floating action button
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_complete);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEditPresenter.finishAction();
            }
        });
        fab.hide();

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(){
        super.onStart();
        addEditPresenter.subscribe(getActivity().getIntent());
    }

    @Override
    public void setPresenter(AddEditContract.Presenter presenter) {
        this.addEditPresenter = presenter;
    }

    @Override
    public void showSearchItems(List<Criteria> criterias){
        CategoryAdapter adapter = new CategoryAdapter(new LinkedList<Category>(criterias), false);
        searchListView.setAdapter(adapter);

        searchListView.setVisibility(View.VISIBLE);
        containerView.setVisibility(View.GONE);
        categoryTitle.setVisibility(View.GONE);
    }

    @Override
    public void setSelectedCriteria(Criteria criteria) {
        categoryTitle.setText("Selected " + criteria.getRealReference());
    }

    @Override
    public void hideSearch() {
        searchListView.setVisibility(View.GONE);
        containerView.setVisibility(View.VISIBLE);
        categoryTitle.setVisibility(View.VISIBLE);
    }

    @Override
    public void setItemInfo(Item item) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Edit Item");
        fab.setImageResource(R.drawable.ic_check_white);
        descriptionEditText.setText(item.getDescription());
        scoreView.setText(String.valueOf(item.getWeight()));
        setSelectedCriteria(item.getCriteria());
    }

    @Override
    public void setFilesInfo(List<Uri> receivedFiles) {
        filenameView.setText(UriHelper.getFileName(getContext(), receivedFiles.get(0)));
    }

    @Override
    public String getDescriptionText() {
        return descriptionEditText.getText().toString();
    }

    // returns item with result.OK to open item in Categories when added
    @Override
    public void finishAction(Item item) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(descriptionEditText.getWindowToken(), 0);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("item_added_key", item.getDbKey());
        resultIntent.setAction(OPEN_ITEM_ADDED);
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

    @Override
    public void finishAction() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(descriptionEditText.getWindowToken(), 0);

        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void showEmptyDescriptionError() {
        Toast.makeText(getContext(), "Description is empty", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNoSelectedCriteriaError() {
        Toast.makeText(getContext(), "No criteria selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showFloatingActionButton() {
        fab.show();
    }

    @Override
    public void hideFloatingActionButton() {
        fab.hide();
    }

    @Override
    public void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Loading");
        }

        progressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void createTreeView(List<Dimension> dimensions) {
        TreeNode treeRoot = TreeNode.root();
        for (Dimension dimension : dimensions) {
            TreeNode parent = new TreeNode(dimension);
            for (Area area : dimension.getAreas()) {
                TreeNode level1 = new TreeNode(area);
                for (Criteria criteria : area.getCriterias()) {
                    TreeNode level2 = new TreeNode(criteria);
                    level2.setViewHolder(new CategoryNodeHolder(getContext(), R.layout.node_holder_criteria));
                    level1.addChild(level2);
                }
                level1.setViewHolder(new CategoryNodeHolder(getContext(), R.layout.node_holder_area));
                parent.addChild(level1);
            }
            parent.setViewHolder(new CategoryNodeHolder(getContext(), R.layout.node_holder_dimension));
            treeRoot.addChild(parent);
        }
        tView = new AndroidTreeView(getContext(), treeRoot);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setUse2dScroll(false);
        tView.setSelectionModeEnabled(true);
        tView.setDefaultAnimation(true);
        containerView.addView(tView.getView());
    }

    private SearchView.OnQueryTextListener searchQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            addEditPresenter.searchForCriteria(newText);
            return false;
        }
    };

    private ListView.OnItemClickListener searchItemClickListener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Category item = (Category) searchListView.getItemAtPosition(position);
            if (item instanceof Criteria) {
                addEditPresenter.selectedCriteria((Criteria) item);
            }
        }
    };

    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            Category item = (Category) value;
            if (item instanceof Criteria) {
                if(!tView.getSelected().isEmpty()){
                    tView.getSelected().get(0).setSelected(false);
                }
                node.setSelectable(true);
                node.setSelected(true);
                tView.selectNode(node, true);
                addEditPresenter.selectedCriteria((Criteria) item);
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tState", tView.getSaveState());
    }

}
