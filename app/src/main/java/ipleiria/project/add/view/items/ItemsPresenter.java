package ipleiria.project.add.view.items;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.CategoryRepository;
import ipleiria.project.add.data.source.ItemsRepository;


/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemsPresenter implements ItemsContract.Presenter{

    private static final String TAG = "ITEMS_PRESENTER";

    private final CategoryRepository categoryRepository;
    private final ItemsRepository itemsRepository;
    private final ItemsContract.View itemsView;
    private final ItemsContract.ItemsActivityView activityView;

    private int currentFiltering;

    private DatabaseReference databaseRef;
    private ChildEventListener itemsListener;

    public ItemsPresenter(@NonNull ItemsRepository itemsRepository, @NonNull ItemsContract.View itemsView, @NonNull ItemsContract.ItemsActivityView activityView) {
        this.itemsRepository = itemsRepository;
        this.categoryRepository = CategoryRepository.getInstance();
        this.activityView = activityView;
        this.itemsView = itemsView;
        this.itemsView.setPresenter(this);

        this.currentFiltering = 0;

        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void subscribe() {
        if(!categoryRepository.getDimensions().isEmpty()) {
            setItemsFilters();
            setItemsListener();
        }else{
            itemsView.setLoadingIndicator(true);
            readCategories();
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

    @Override
    public void unsubscribe() {
        if(itemsListener != null) {
            databaseRef.removeEventListener(itemsListener);
        }
    }

    private void setItemsListener() {
        itemsListener = itemsRepository.getItemsReference().addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String key = dataSnapshot.getKey();
                        itemsRepository.addNewItem(dataSnapshot);
                        itemsView.showAddedItem(itemsRepository.getItem(key));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        String key = dataSnapshot.getKey();
                        itemsRepository.addNewItem(dataSnapshot);
                        itemsView.showAddedItem(itemsRepository.getItem(key));
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

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
    public void getFilteredItems() {
        List<Item> items = new LinkedList<>();
        if (currentFiltering == 0) {
            itemsView.showItems(itemsRepository.getItems());
        } else {
            for (Item i : itemsRepository.getItems()) {
                if (i.getDimension().getReference() == currentFiltering) {
                    items.add(i);
                }
            }
            itemsView.showItems(items);
        }
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
        getFilteredItems();
    }

    @Override
    public int getFiltering() {
        return currentFiltering;
    }

    private void setItemsFilters() {
        List<String> filters = new LinkedList<>();
        filters.add("All");
        for(Dimension d: categoryRepository.getDimensions()){
            filters.add(d.getName());
        }
        activityView.setFilters(filters);
    }
}
