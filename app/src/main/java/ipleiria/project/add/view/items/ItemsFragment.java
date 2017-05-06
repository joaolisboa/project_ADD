package ipleiria.project.add.view.items;

import android.content.Intent;
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
import android.widget.Spinner;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemsFragment extends Fragment implements ItemsContract.View{

    private ItemsContract.Presenter presenter;

    private ItemAdapter listAdapter;

    private View noItemsView;
    private TextView noItemsMainView;
    private TextView noItemsAddView;
    private LinearLayout itemsView;
    private TextView mFilteringLabelView;
    private Spinner spinner;

    public ItemsFragment() {
        // Requires empty public constructor
    }

    public static ItemsFragment newInstance() {
        return new ItemsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listAdapter = new ItemAdapter(new LinkedList<Item>(), mItemListener, false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.items_frag, container, false);

        // Set up tasks view
        ListView listView = (ListView) root.findViewById(R.id.items_list);
        listView.setAdapter(listAdapter);
        mFilteringLabelView = (TextView) root.findViewById(R.id.filteringLabel);
        itemsView = (LinearLayout) root.findViewById(R.id.itemsLL);

        // set up spinner for filtering items
        // get criteria
        /*spinner = (Spinner) root.findViewById(R.id.spinner_nav);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, filters);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setSelection(0);*/

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
                presenter.getFilteredItems();
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.subscribe();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.unsubscribe();
    }

    @Override
    public void setPresenter(@NonNull ItemsContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.result(requestCode, resultCode);
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
    public void showNoItemsViews() {

    }

    @Override
    public void filterItems(){
        presenter.setFiltering(spinner.getSelectedItemPosition());
    }

    /**
     * Listener for clicks on items in the ListView.
     */
    ItemActionListener mItemListener = new ItemActionListener() {

        @Override
        public void onItemClick(Item clickedIem) {
            // TODO: 06-May-17 open item details act
        }

        @Override
        public void onDeleteItem(Item deletedItem) {

        }

        @Override
        public void onPermanentDeleteItem(Item deletedItem) {

        }

        @Override
        public void onEditItem(Item itemToEdit) {

        }

        @Override
        public void onRestoreItem(Item restoredItem) {

        }
    };


    public interface ItemActionListener {

        void onItemClick(Item clickedIem);

        void onDeleteItem(Item deletedItem);

        void onPermanentDeleteItem(Item deletedItem);

        void onEditItem(Item itemToEdit);

        void onRestoreItem(Item restoredItem);
    }


}
