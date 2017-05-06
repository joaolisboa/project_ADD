package ipleiria.project.add.view.items;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Utils.StringUtils;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.CategoryRepository;
import ipleiria.project.add.data.source.ItemsRepository;


/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemsPresenter implements ItemsContract.Presenter {

    private static final String TAG = "ITEMS_PRESENTER";

    private final CategoryRepository categoryRepository;
    private final ItemsRepository itemsRepository;
    private final ItemsContract.View itemsView;
    private final ItemsContract.ItemsActivityView activityView;

    private int currentFiltering;
    private List<Item> currentFilteredItems;

    private boolean listingDeleted;

    private DatabaseReference databaseRef;
    private ChildEventListener itemsListener;

    public ItemsPresenter(@NonNull ItemsRepository itemsRepository, @NonNull ItemsContract.View itemsView,
                          @NonNull ItemsContract.ItemsActivityView activityView, boolean listingDeleted) {
        this.itemsRepository = itemsRepository;
        this.categoryRepository = CategoryRepository.getInstance();
        this.activityView = activityView;
        this.itemsView = itemsView;
        this.itemsView.setPresenter(this);

        this.listingDeleted = listingDeleted;

        this.currentFiltering = 0;
        this.currentFilteredItems = new LinkedList<>();

        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void subscribe() {
        if (!categoryRepository.getDimensions().isEmpty()) {
            setItemsFilters();
            setItemsListener();
        } else {
            itemsView.setLoadingIndicator(true);
            readCategories();
        }
    }

    @Override
    public void unsubscribe() {
        if (itemsListener != null) {
            databaseRef.removeEventListener(itemsListener);
        }
    }

    private void readCategories() {
        categoryRepository.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dimensionSnap : dataSnapshot.getChildren()) {
                    categoryRepository.addDimension(dimensionSnap);
                }
                itemsView.setLoadingIndicator(false);
                setItemsFilters();
                setItemsListener();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    private void setItemsListener() {
        DatabaseReference reference = (!listingDeleted ? itemsRepository.getItemsReference() :
                itemsRepository.getDeletedItemsReference());
        itemsListener = reference.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String key = dataSnapshot.getKey();
                        itemsRepository.addNewItem(dataSnapshot, listingDeleted);
                        Item item = (!listingDeleted ? itemsRepository.getItem(key) : itemsRepository.getDeletedItem(key));
                        itemsView.showAddedItem(item);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        String key = dataSnapshot.getKey();
                        itemsRepository.addNewItem(dataSnapshot, listingDeleted);
                        Item item = (!listingDeleted ? itemsRepository.getItem(key) : itemsRepository.getDeletedItem(key));
                        itemsView.showAddedItem(item);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        itemsRepository.deleteItem(dataSnapshot.getKey());
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    @Override
    public void result(int requestCode, int resultCode) {

    }

    @Override
    public void showFilteredItems() {
        currentFilteredItems = new LinkedList<>();
        if (currentFiltering == 0) {
            if (!listingDeleted) {
                processItems(itemsRepository.getItems());
            } else {
                processItems(itemsRepository.getDeletedItems());
            }
        } else {
            for (Item i : itemsRepository.getItems()) {
                if (i.getDimension().getReference() == currentFiltering) {
                    currentFilteredItems.add(i);
                }
            }
            processItems(currentFilteredItems);
        }
    }

    @Override
    public void searchItems(String query) {
        List<Item> matchingItems = new LinkedList<>();

        query = StringUtils.replaceDiacriticalMarks(query).toLowerCase();

        if (TextUtils.isEmpty(query)) {
            showFilteredItems();
        } else {
            List<Item> searchingList = (currentFilteredItems.isEmpty() ? itemsRepository.getItems() : currentFilteredItems);
            for (Item item : searchingList) {
                if (itemMatchesQuery(item, query)) {
                    matchingItems.add(item);
                }
            }
            processItems(matchingItems);
        }
    }

    private boolean itemMatchesQuery(Item item, String query) {
        String criteriaName = StringUtils.replaceDiacriticalMarks(item.getCriteria().getName().toLowerCase());
        String itemDescription = StringUtils.replaceDiacriticalMarks(item.getDescription().toLowerCase());

        return criteriaName.contains(query) || itemDescription.contains(query);
    }

    @Override
    public void addNewItem() {

    }

    @Override
    public void openItemDetails(@NonNull Item item) {

    }

    @Override
    public void deleteItem(@NonNull Item item) {
        itemsRepository.deleteItem(item);
        itemsView.removeDeletedItem(item);
    }

    @Override
    public void permanentlyDeleteItem(@NonNull Item item) {
        itemsRepository.permanenetlyDeleteItem(item);
        itemsView.removeDeletedItem(item);
    }

    @Override
    public void restoreItem(@NonNull Item item) {
        itemsRepository.restoreItem(item);
        itemsView.removeDeletedItem(item);
    }

    @Override
    public void setFiltering(int requestType) {
        currentFiltering = requestType;
        showFilteredItems();
    }

    @Override
    public int getFiltering() {
        return currentFiltering;
    }

    @Override
    public void checkForEmptyList() {
        if (!listingDeleted) {
            if(itemsRepository.getItems().isEmpty()) {
                itemsView.showNoItems();
            }
        } else {
            if(itemsRepository.getDeletedItems().isEmpty()) {
                itemsView.showNoDeletedItems();
            }
        }
    }

    private void processItems(List<Item> items){
        if(items.isEmpty()){
            checkForEmptyList();
        }else{
            itemsView.showItems(items);
        }
    }

    private void setItemsFilters() {
        List<String> filters = new LinkedList<>();
        filters.add("All");
        for (Dimension d : categoryRepository.getDimensions()) {
            filters.add(d.getName());
        }
        activityView.setFilters(filters);
    }
}
