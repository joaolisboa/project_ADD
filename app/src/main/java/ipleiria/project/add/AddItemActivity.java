package ipleiria.project.add;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Area;
import ipleiria.project.add.Model.Category;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Dimension;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;
import ipleiria.project.add.Utils.CloudHandler;
import ipleiria.project.add.Utils.FileUtils;
import ipleiria.project.add.Utils.NetworkState;
import ipleiria.project.add.Utils.PathUtils;
import ipleiria.project.add.Utils.StringUtils;
import ipleiria.project.add.Utils.UriHelper;

import static ipleiria.project.add.SettingsActivity.DROPBOX_PREFS_KEY;
import static ipleiria.project.add.SettingsActivity.MEO_PREFS_KEY;

public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "AddItemActivity";
    public static final String SENDING_PHOTO = "SendingPhotoAction";

    private ViewGroup containerView;
    private EditText descriptionEditText;
    private TextView categoryTitle;
    private TextView filenameView;

    private TreeNode treeRoot;
    private AndroidTreeView tView;
    private LinkedList<Criteria> searchResults;
    private ListView searchListView;
    ArrayAdapter<Criteria> adapter;
    private List<Criteria> criterias;

    //private Uri receivedFile;
    private List<Uri> receivedFiles;
    private Criteria selectedCriteria;

    private Item editingItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_data);

        containerView = (ViewGroup) findViewById(R.id.container);
        descriptionEditText = (EditText) findViewById(R.id.item_description);
        categoryTitle = (TextView) findViewById(R.id.category_title);
        filenameView = (TextView) findViewById(R.id.filename);

        receivedFiles = new LinkedList<>();

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
        if(ApplicationData.getInstance().getSharedPreferences() == null){
            ApplicationData.getInstance()
                    .setSharedPreferences(getSharedPreferences(getString(R.string.shared_prefs_user), MODE_PRIVATE));
        }

        if (NetworkState.isOnline(this)) {
            String dropToken = ApplicationData.getInstance().getSharedPreferences().getString(DROPBOX_PREFS_KEY, "");
            if (!dropToken.isEmpty()) {
                DropboxClientFactory.init(dropToken);
            }
            String meoToken = ApplicationData.getInstance().getSharedPreferences().getString(MEO_PREFS_KEY, "");
            if (!meoToken.isEmpty()) {
                MEOCloudClient.init(meoToken);
            }
        }

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }catch(DatabaseException e){
            // set persistence must only be run once and be run before any call to FirebaseDatabase
            Log.d(TAG, e.getMessage());
        }
        treeRoot = TreeNode.root();
        criterias = ApplicationData.getInstance().getCriterias();
        if(criterias.isEmpty()){
            readCategories();
        }else{
            createTreeView();
        }

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
            handleSingleFile(intent);
        } else if (SENDING_PHOTO.equals(action)){
            handleFile(Uri.parse(intent.getStringExtra( "photo_uri")));
            String filename = UriHelper.getFileName(AddItemActivity.this, receivedFiles.get(0));
            filenameView.setText(filename);

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            handleMultipleFiles(intent);
        } else {
            // Handle other intents, such as being started from the home screen
            String itemDbKey = intent.getStringExtra("itemKey");
            System.out.println("Editing item with key: " + itemDbKey);
            editingItem = ApplicationData.getInstance().getItem(itemDbKey);
            if (editingItem != null) {
                filenameView.setText(editingItem.getFiles().get(0).getFilename());
                descriptionEditText.setText(editingItem.getDescription());
                categoryTitle.setText("Selected " + editingItem.getCategoryReference());
                selectedCriteria = editingItem.getCriteria();
                ((FloatingActionButton)findViewById(R.id.fab)).setImageResource(R.drawable.ic_check_white);
            }
        }
    }

    private void readCategories() {
        FirebaseHandler.getInstance().getCategoryReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Dimension> dimensions = new LinkedList<>();
                for (DataSnapshot dimensionSnap : dataSnapshot.getChildren()) {
                    Dimension dimension = new Dimension(dimensionSnap.child("name").getValue(String.class),
                            dimensionSnap.child("reference").getValue(Integer.class));
                    dimension.setDbKey(dimensionSnap.getKey());
                    for (DataSnapshot areaSnap : dimensionSnap.child("areas").getChildren()) {
                        Area area = new Area(areaSnap.child("name").getValue(String.class),
                                areaSnap.child("reference").getValue(Integer.class));
                        area.setDbKey(areaSnap.getKey());
                        for (DataSnapshot criteriaSnap : areaSnap.child("criterias").getChildren()) {
                            Criteria criteria = new Criteria(criteriaSnap.child("name").getValue(String.class),
                                    criteriaSnap.child("reference").getValue(Integer.class));
                            criteria.setDbKey(criteriaSnap.getKey());
                            area.addCriteria(criteria);
                        }
                        dimension.addArea(area);
                    }
                    dimensions.add(dimension);
                }
                ApplicationData.getInstance().addDimensions(dimensions);
                createTreeView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    private void handleFile(Uri uri) {
        receivedFiles.add(uri);

    }

    private void handleSingleFile(Intent intent) {
        handleFile((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
        String filename = UriHelper.getFileName(AddItemActivity.this, receivedFiles.get(0));
        filenameView.setText(filename);
    }

    private void handleMultipleFiles(Intent intent) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris != null) {
            for (Uri uri : uris) {
                handleFile(uri);
            }
        }
        String filename = UriHelper.getFileName(AddItemActivity.this, receivedFiles.get(0));
        filenameView.setText(filename);
    }

    public void addItem(View view) {
        String description = descriptionEditText.getText().toString();

        if (selectedCriteria == null) {
            Toast.makeText(this, "No criteria selected", Toast.LENGTH_SHORT).show();
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Description is empty", Toast.LENGTH_SHORT).show();
        } else {
            List<ItemFile> itemFiles = new LinkedList<>();
            for (Uri uri : receivedFiles) {
                itemFiles.add(new ItemFile(UriHelper.getFileName(AddItemActivity.this, uri)));
            }
            Item item = new Item(itemFiles, description, selectedCriteria);
            if (editingItem != null) {
                editingItem.addFiles(itemFiles);
                // user changed items category so we need to move the files
                if(!editingItem.getCriteria().equals(selectedCriteria)){
                    String oldPath = PathUtils.getRemotePath(editingItem.getCriteria());
                    editingItem.setCriteria(selectedCriteria);
                    FileUtils.moveFilesToNewDir(this, editingItem.getFiles(), oldPath);
                }
                editingItem.setDescription(description);
                ApplicationData.getInstance().addItem(editingItem);
            } else {
                ApplicationData.getInstance().addItem(item);
                if (NetworkState.isOnline(this)) {
                    for(int i = 0; i < receivedFiles.size(); i++){
                        CloudHandler.uploadFileToCloud(this, receivedFiles.get(i),
                                item.getFiles().get(i), item.getCriteria());
                    }
                }else{
                    for(int i = 0; i < receivedFiles.size(); i++){
                        FileUtils.copyFileToLocalDir(this, receivedFiles.get(i), item.getCriteria());
                    }
                }
            }
            if (ApplicationData.getInstance().getUserUID() != null) {
                FirebaseHandler.getInstance().writeItem(editingItem == null ? item : editingItem);
            }
            if (editingItem == null) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                descriptionEditText.clearFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(descriptionEditText.getWindowToken(), 0);
                setResult(Activity.RESULT_OK);
                finish();
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
                    level2.setViewHolder(new CategoryNodeHolder(AddItemActivity.this, R.layout.node_holder_criteria));
                    level1.addChild(level2);
                }
                level1.setViewHolder(new CategoryNodeHolder(AddItemActivity.this, R.layout.node_holder_area));
                parent.addChild(level1);
            }
            parent.setViewHolder(new CategoryNodeHolder(AddItemActivity.this, R.layout.node_holder_dimension));
            treeRoot.addChild(parent);
        }
        tView = new AndroidTreeView(AddItemActivity.this, treeRoot);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setUse2dScroll(false);
        tView.setDefaultAnimation(true);
        containerView.addView(tView.getView());
    }

    private SearchView.OnQueryTextListener searchQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            searchResults.clear();


            newText = StringUtils.removeDiacriticalMarks(newText);

            if (TextUtils.isEmpty(newText)) {
                searchListView.setVisibility(View.GONE);
                containerView.setVisibility(View.VISIBLE);
                categoryTitle.setVisibility(View.VISIBLE);
            } else {
                String query = newText.toLowerCase();
                for (Criteria criterio : criterias) {
                    String stringcriterio = StringUtils.removeDiacriticalMarks(criterio.getName().toLowerCase());
                    if (stringcriterio.contains(query)) {
                        searchResults.add(criterio);
                    }
                }
                searchListView.setVisibility(View.VISIBLE);
                containerView.setVisibility(View.GONE);
                categoryTitle.setVisibility(View.GONE);
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
                selectedCriteria = (Criteria) item;
                categoryTitle.setText("Selected " + ((Criteria) item).getRealReference());
            }
        }
    };

    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            Category item = (Category) value;
            if (item instanceof Criteria) {
                Toast.makeText(AddItemActivity.this, ((Criteria) item).getRealReference(), Toast.LENGTH_SHORT).show();
                selectedCriteria = (Criteria) item;
                categoryTitle.setText("Selected " + ((Criteria) item).getRealReference());
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tState", tView.getSaveState());
    }

}
