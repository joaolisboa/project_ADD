package ipleiria.project.add.view.items;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.ItemClickListener;
import ipleiria.project.add.view.itemdetail.ItemDetailActivity;
import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.view.add_edit_item.AddEditActivity;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;
import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.EDITING_ITEM;
import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.EDITING_ITEM_KEY;
import static ipleiria.project.add.view.itemdetail.ItemDetailPresenter.ITEM_KEY;
import static ipleiria.project.add.view.items.ItemsPresenter.LIST_DELETED_KEY;

/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemsFragment extends Fragment implements ItemsContract.View{

    // on resume will show an item added message and enable swipe
    // it differs because CHANGE comes externally(from another app)
    // which disables the swiping until the file/item was added/created
    public static final int REQUEST_ADD_NEW_ITEM_CHANGE = 2090;
    // on resume will show an item added message
    public static final int REQUEST_ADD_NEW_ITEM = 2091;
    // on resume will show an item edited message
    public static  final int REQUEST_ITEM_EDIT = 2092;

    private ItemsContract.Presenter itemsPresenter;

    private ItemAdapter listAdapter;

    private View noItemsView;
    private TextView noItemsMainView;
    private TextView noItemsAddView;
    private LinearLayout itemsView;

    private Spinner spinnerFilter;

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
        listAdapter = new ItemAdapter(new LinkedList<Item>(), itemActionListener,
                listDeleted, itemsPresenter.getIntentAction() == null);

        if(listDeleted){
            getActivity().findViewById(R.id.fab_add).setVisibility(View.GONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.items_frag, container, false);

        spinnerFilter = (Spinner) getActivity().findViewById(R.id.spinner_nav);

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
                addItem();
            }
        });

        // Set up floating action button
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_add);
        fab.setImageResource(R.drawable.add_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
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
                setLoadingIndicator(false);
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
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
    public void setFilters(List<String> filters) {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, filters);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerArrayAdapter);
        spinnerFilter.setSelection(0);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemsPresenter.setFiltering(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
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
    public void openItemDetails(Item item, boolean listingDeleted) {
        Intent intent = new Intent(getContext(), ItemDetailActivity.class);
        intent.putExtra(LIST_DELETED_KEY, listingDeleted);
        intent.putExtra(ITEM_KEY, item.getDbKey());
        startActivity(intent);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        itemsPresenter.onResult(requestCode, resultCode, data);
    }

    @Override
    public void showItemEditedMessage() {
        showMessage("Item saved");
    }

    @Override
    public void showItemAddedMessage(){
        showMessage("New item saved");
    }

    @Override
    public void showFilesAddedMessage() {
        showMessage("Files saved to item");
    }

    @Override
    public void enableListSwipe(boolean enable){
        listAdapter.enableSwipe(enable);
    }

    @Override
    public void setItemPoints(Item item, double finalPoints) {
        listAdapter.setItemPoints(item, finalPoints);
    }

    private void showMessage(String message){
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    private void showNoItemsViews(String mainText) {
        itemsView.setVisibility(View.GONE);
        noItemsView.setVisibility(View.VISIBLE);

        noItemsMainView.setText(mainText);
    }

    private void addItem(){
        if(itemsPresenter.getIntentAction() != null) {
            Intent intent = getActivity().getIntent();
            // change intent to use a different activity, keeping extras and action
            intent.setComponent(new ComponentName(getContext(), AddEditActivity.class));
            startActivityForResult(intent, REQUEST_ADD_NEW_ITEM_CHANGE);
        }else{
            startActivityForResult(new Intent(getContext(), AddEditActivity.class), REQUEST_ADD_NEW_ITEM);
        }
    }

    /**
     * Listener for clicks on item and swipeLayout in the ListView.
     */
    ItemClickListener itemActionListener = new ItemClickListener() {

        @Override
        public void onItemClick(Item clickedIem) {
            itemsPresenter.onItemClicked(clickedIem);
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
        public void onEditItem(Item editedItem) {
            Intent intent = new Intent(getContext(), AddEditActivity.class);
            intent.setAction(EDITING_ITEM);
            intent.putExtra(EDITING_ITEM_KEY, editedItem.getDbKey());
            startActivityForResult(intent, REQUEST_ITEM_EDIT);
        }

        @Override
        public void onRestoreItem(Item restoredItem) {
            itemsPresenter.restoreItem(restoredItem);
        }
    };

}
