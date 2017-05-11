package ipleiria.project.add.view.items;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.utils.StringUtils;
import ipleiria.project.add.utils.UriHelper;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;

import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PHOTO;


/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemsPresenter implements ItemsContract.Presenter {

    private static final String TAG = "ITEMS_PRESENTER";
    public static final String LIST_DELETED_KEY = "list_deleted";

    private final CategoryRepository categoryRepository;
    private final ItemsRepository itemsRepository;
    private final ItemsContract.View itemsView;

    private int currentFiltering;
    private List<Item> currentFilteredItems;

    private boolean listingDeleted;
    private String action;
    private List<Uri> receivedFiles;

    private ChildEventListener itemsListener;

    public ItemsPresenter(@NonNull ItemsRepository itemsRepository, @NonNull ItemsContract.View itemsView, boolean listingDeleted) {
        this.itemsRepository = itemsRepository;
        this.categoryRepository = CategoryRepository.getInstance();
        this.itemsView = itemsView;
        this.itemsView.setPresenter(this);

        this.listingDeleted = listingDeleted;

        this.currentFiltering = 0;
        this.currentFilteredItems = new LinkedList<>();

        this.receivedFiles = new ArrayList<>();
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
            if(!listingDeleted){
                itemsRepository.getItemsReference().removeEventListener(itemsListener);
            }else{
                itemsRepository.getDeletedItemsReference().removeEventListener(itemsListener);
            }
        }
    }

    private void readCategories() {
        categoryRepository.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoryRepository.addDimensions(dataSnapshot);
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
                        /*String key = dataSnapshot.getKey();
                        Item deletedItem = (!listingDeleted ? itemsRepository.getItem(key) : itemsRepository.getDeletedItem(key));
                        itemsRepository.deleteLocalItem(deletedItem, listingDeleted);
                        itemsView.removeDeletedItem(deletedItem);*/
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
    public void showFilteredItems() {
        if (currentFiltering == 0) {
            if (!listingDeleted) {
                processItems(itemsRepository.getItems());
            } else {
                processItems(itemsRepository.getDeletedItems());
            }
        } else {
            currentFilteredItems = new LinkedList<>();
            List<Item> searchList = (!listingDeleted ? itemsRepository.getItems() : itemsRepository.getDeletedItems());
            for (Item i : searchList) {
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
    public void deleteItem(@NonNull Item item) {
        itemsRepository.deleteItem(item);
        itemsView.removeDeletedItem(item);
    }

    @Override
    public void permanentlyDeleteItem(@NonNull Item item) {
        itemsRepository.permanentlyDeleteItem(item);
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

    @Override
    public void setIntentInfo(Intent intent) {
        this.action = intent.getAction();

        if(action != null){
            switch(action){
                case Intent.ACTION_SEND:
                    receivedFiles.add(UriHelper.getUriFromExtra(intent));
                    break;

                case Intent.ACTION_SEND_MULTIPLE:
                    receivedFiles.addAll(UriHelper.getUriListFromExtra(intent));
                    break;

                case SENDING_PHOTO:
                    receivedFiles.add(Uri.parse(intent.getStringExtra("photo_uri")));
                    break;
            }
        }
    }

    @Override
    public String getIntentAction() {
        return action;
    }

    @Override
    public void onItemClicked(Item item) {
        if(action == null){
            itemsView.openItemDetails(item, listingDeleted);
        }else{
            itemsRepository.addFilesToItem(item, receivedFiles);
            itemsView.finish();
        }
    }

    private void processItems(List<Item> items){
        if(items.isEmpty()){
            if (!listingDeleted) {
                itemsView.showNoItems();
            } else {
                itemsView.showNoDeletedItems();
            }
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
        itemsView.setFilters(filters);
    }
}
