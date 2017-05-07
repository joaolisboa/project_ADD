package ipleiria.project.add.view.items;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;
import static ipleiria.project.add.view.items.ItemsActivity.LIST_DELETED_KEY;

/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemsFragment extends Fragment implements ItemsContract.View{

    private ItemsContract.Presenter itemsPresenter;

    private ItemAdapter listAdapter;

    private View noItemsView;
    private TextView noItemsMainView;
    private TextView noItemsAddView;
    private LinearLayout itemsView;

    public ItemsFragment() {
        // Requires empty public constructor
    }

    public static ItemsFragment newInstance() {
        return new ItemsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean listDeleted = getActivity().getIntent().getBooleanExtra(LIST_DELETED_KEY, false);
        listAdapter = new ItemAdapter(new LinkedList<Item>(), mItemListener, listDeleted, itemsPresenter.getIntentAction());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.items_frag, container, false);

        // Set up tasks view
        ListView listView = (ListView) root.findViewById(R.id.items_list);
        listView.setAdapter(listAdapter);
        itemsView = (LinearLayout) root.findViewById(R.id.itemsLL);

        // Set up  no tasks view
        noItemsView = root.findViewById(R.id.noItems);
        noItemsMainView = (TextView) root.findViewById(R.id.noItemsMain);
        noItemsAddView = (TextView) root.findViewById(R.id.noItemsAdd);
        noItemsAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItem();
            }
        });

        // Set up floating action button
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_add);
        fab.setImageResource(R.drawable.add_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItem();
            }
        });

        // Set up progress indicator
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itemsPresenter.showFilteredItems();
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        itemsPresenter.subscribe();
    }

    @Override
    public void onStop() {
        super.onStop();
        itemsPresenter.unsubscribe();
    }

    @Override
    public void setPresenter(@NonNull ItemsContract.Presenter presenter) {
        this.itemsPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        itemsPresenter.result(requestCode, resultCode);
    }

    @Override
    public void setLoadingIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl = (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);
        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showItems(List<Item> items) {
        listAdapter.replaceData(items);

        itemsView.setVisibility(View.VISIBLE);
        noItemsView.setVisibility(View.GONE);
    }

    @Override
    public void showAddItem() {
        // TODO: 06-May-17 create new item - without file
    }

    @Override
    public void showAddedItem(@NonNull Item item) {
        listAdapter.onItemAdded(item);

        itemsView.setVisibility(View.VISIBLE);
        noItemsView.setVisibility(View.GONE);
    }

    @Override
    public void removeDeletedItem(@NonNull Item deletedItem) {
        listAdapter.onItemRemoved(deletedItem);
        itemsPresenter.checkForEmptyList();
    }

    @Override
    public void showNoItems() {
        showNoItemsViews(getString(R.string.no_items));
    }

    @Override
    public void showNoDeletedItems() {
        showNoItemsViews(getString(R.string.no_deleted_items));
        noItemsAddView.setVisibility(View.GONE);
    }

    private void showNoItemsViews(String mainText) {
        itemsView.setVisibility(View.GONE);
        noItemsView.setVisibility(View.VISIBLE);

        noItemsMainView.setText(mainText);
    }

    // TODO: 06-May-17 refactor file share
    private void addFilesToItem(Item itemAtPosition) {
        /*receivedFiles = new LinkedList<>();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handleSingleFile(intent);
        } else if (SENDING_PHOTO.equals(action)){
            handleFile(Uri.parse(intent.getStringExtra( "photo_uri")));

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            handleMultipleFiles(intent);
        }
        List<ItemFile> itemFiles = new LinkedList<>();
        for (Uri uri : receivedFiles) {
            itemFiles.add(new ItemFile(UriHelper.getFileName(ItemsActivity.this, uri)));
        }
        itemAtPosition.addFiles(itemFiles);
        if (NetworkState.isOnline(this)) {
            for(int i = 0; i < receivedFiles.size(); i++){
                Log.d("FILE_UPLOAD", "uploading file: " + UriHelper.getFileName(ItemsActivity.this, receivedFiles.get(i)));
                CloudHandler.uploadFileToCloud(this, receivedFiles.get(i),
                        itemFiles.get(i), itemAtPosition.getCriteria());
            }
        }else{
            for(int i = 0; i < receivedFiles.size(); i++){
                FileUtils.copyFileToLocalDir(this, receivedFiles.get(i), itemAtPosition.getCriteria());
            }
        }
        if (ApplicationData.getInstance().getUserUID() != null) {
            FirebaseHandler.getInstance().writeItem(itemAtPosition);
        }
        Toast.makeText(this, "File added to item", Toast.LENGTH_SHORT).show();*/
    }

    private void handleFile(Uri uri) {
        //receivedFiles.add(uri);
    }

    private void handleSingleFile(Intent intent) {
        //handleFile((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
    }

    private void handleMultipleFiles(Intent intent) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris != null) {
            for (Uri uri : uris) {
                handleFile(uri);
            }
        }
    }

    /**
     * Listener for clicks on item and swipeLayout in the ListView.
     */
    ItemActionListener mItemListener = new ItemActionListener() {

        @Override
        public void onItemClick(Item clickedIem) {
            // TODO: 06-May-17 open item details act

        }

        @Override
        public void onDeleteItem(Item deletedItem) {
            itemsPresenter.deleteItem(deletedItem);
        }

        @Override
        public void onPermanentDeleteItem(Item deletedItem) {
            itemsPresenter.permanentlyDeleteItem(deletedItem);
        }

        @Override
        public void onEditItem(Item itemToEdit) {
            // TODO: 06-May-17 open addEditAct to edit item
        }

        @Override
        public void onRestoreItem(Item restoredItem) {
            itemsPresenter.restoreItem(restoredItem);
        }
    };


    interface ItemActionListener {

        void onItemClick(Item clickedIem);

        void onDeleteItem(Item deletedItem);

        void onPermanentDeleteItem(Item deletedItem);

        void onEditItem(Item itemToEdit);

        void onRestoreItem(Item restoredItem);
    }


}
