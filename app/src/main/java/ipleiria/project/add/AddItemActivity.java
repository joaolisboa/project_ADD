package ipleiria.project.add;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.v2.files.FileMetadata;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxUploadFile;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOUploadFile;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Area;
import ipleiria.project.add.Model.Category;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Dimension;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Utils.NetworkState;
import ipleiria.project.add.Utils.UriHelper;

public class AddItemActivity extends AppCompatActivity {

    private ViewGroup containerView;

    private TreeNode treeRoot;
    private AndroidTreeView tView;
    private LinkedList<Criteria> searchResults;
    private ListView searchListView;
    ArrayAdapter<Criteria> adapter;
    private List<Criteria> criterias;

    private Uri receivedFile;
    private String filename;
    private Criteria selectedCriteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_data);

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

        tView = new AndroidTreeView(AddItemActivity.this, treeRoot);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setUse2dScroll(false);
        tView.setDefaultAnimation(true);
        containerView.addView(tView.getView());

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                tView.restoreState(state);
            }
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handleFile(intent);
        } /*else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            handleMultipleFiles(intent);
        }*/ else {
            // Handle other intents, such as being started from the home screen
        }
    }

    private void handleFile(Intent intent) {
        receivedFile = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        filename = UriHelper.getFileName(AddItemActivity.this, receivedFile);
        ((TextView)findViewById(R.id.filename)).setText(filename);
    }

    /*private void handleMultipleFiles(Intent intent) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris != null) {
            for (Uri uri : uris) {
                handleFile(uri);
            }
        }
    }*/

    public void addItem(View view) {
        EditText descriptionEditText = (EditText) findViewById(R.id.item_description);
        String description = descriptionEditText.getText().toString();

        if(selectedCriteria == null){
            Toast.makeText(this, "No criteria selected", Toast.LENGTH_SHORT).show();
        }else if(description.isEmpty()) {
            Toast.makeText(this, "Description is empty", Toast.LENGTH_SHORT).show();
        }else{
            Item item = new Item(filename, description);
            item.setCriteria(selectedCriteria);
            ApplicationData.getInstance().addItem(item);

            if (NetworkState.isOnline(this)) {
                if (MEOCloudClient.isClientInitialized()) {
                    new MEOUploadFile(AddItemActivity.this, new MEOCallback<MEOMetadata>() {

                        @Override
                        public void onComplete(MEOCloudResponse<MEOMetadata> result) {
                            System.out.println("Upload successful");
                        }

                        @Override
                        public void onRequestError(HttpErrorException httpE) {

                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("UploadError", e.getMessage(), e);
                        }
                    }).execute(receivedFile.toString(), UriHelper.getFileName(AddItemActivity.this, receivedFile));
                }
                if (DropboxClientFactory.isClientInitialized()) {
                    new DropboxUploadFile(AddItemActivity.this, DropboxClientFactory.getClient(), new DropboxUploadFile.Callback() {

                        @Override
                        public void onUploadComplete(FileMetadata result) {
                            System.out.println(result.getName());
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("UploadDropError", e.getMessage(), e);
                        }
                    }).execute(receivedFile.toString());
                }
            }
            if (ApplicationData.getInstance().getUserUID() != null) {
                FirebaseHandler.getInstance().writeItem(item);
            }
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    public void createTreeView() {
        for (Dimension dimension : ApplicationData.getInstance().getDimensions()) {
            TreeNode parent = new TreeNode(dimension);
            for (Area area : dimension.getAreas()) {
                TreeNode level1 = new TreeNode(area);
                for (Criteria criteria : area.getCriterias()) {
                    TreeNode level2 = new TreeNode(criteria);
                    level2.setViewHolder(new CategoryNodeHolder(AddItemActivity.this, R.layout.node_holder_criteria));
                    level1.addChild(level2);
                }
                level1.setViewHolder(new CategoryNodeHolder(AddItemActivity.this, R.layout.node_holder_area));
                parent.addChild(level1);
            }
            parent.setViewHolder(new CategoryNodeHolder(AddItemActivity.this, R.layout.node_holder_dimension));
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
                findViewById(R.id.category_title).setVisibility(View.VISIBLE);
            } else {
                String query = newText.toLowerCase();
                for (Criteria criterio : criterias) {
                    if (criterio.contains(query)) {
                        searchResults.add(criterio);
                    }
                }
                searchListView.setVisibility(View.VISIBLE);
                containerView.setVisibility(View.GONE);
                findViewById(R.id.category_title).setVisibility(View.GONE);
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
                Toast.makeText(AddItemActivity.this, ((Criteria) item).getRealReference(), Toast.LENGTH_SHORT).show();
                selectedCriteria = (Criteria)item;
            }
        }
    };

    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            Category item = (Category) value;
            if (item instanceof Criteria) {
                Toast.makeText(AddItemActivity.this, ((Criteria) item).getRealReference(), Toast.LENGTH_SHORT).show();
                selectedCriteria = (Criteria)item;
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tState", tView.getSaveState());
    }

}
