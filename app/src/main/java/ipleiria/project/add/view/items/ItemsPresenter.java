package ipleiria.project.add.view.items;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

import static android.app.Activity.RESULT_OK;
import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PHOTO;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM_CHANGE;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ITEM_EDIT;


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
    private ChildEventListener deletedItemsListener;

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
            itemsRepository.getItemsReference().removeEventListener(itemsListener);
        }
        if (deletedItemsListener != null) {
            itemsRepository.getDeletedItemsReference().removeEventListener(deletedItemsListener);
        }
    }

    private void readCategories() {
        categoryRepository.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoryRepository.addDimensions(dataSnapshot);
                setItemsFilters();
                setItemsListener();
                itemsView.setLoadingIndicator(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    private void setItemsListener() {
        itemsListener = itemsRepository.getItemsReference().addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String key = dataSnapshot.getKey();
                        itemsRepository.addNewItem(dataSnapshot, false);
                        Item item = itemsRepository.getItem(key);
                        if(!listingDeleted) {
                            itemsView.showAddedItem(item);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        String key = dataSnapshot.getKey();
                        itemsRepository.addNewItem(dataSnapshot, false);
                        Item item = itemsRepository.getItem(key);
                        if(!listingDeleted) {
                            itemsView.showAddedItem(item);
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        String key = dataSnapshot.getKey();
                        Item deletedItem = itemsRepository.getItem(key);
                        itemsRepository.deleteLocalItem(deletedItem, false);
                        if(!listingDeleted) {
                            itemsView.removeDeletedItem(deletedItem);
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
        deletedItemsListener = itemsRepository.getDeletedItemsReference().addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String key = dataSnapshot.getKey();
                        itemsRepository.addNewItem(dataSnapshot, true);
                        Item item = itemsRepository.getDeletedItem(key);
                        if(listingDeleted) {
                            itemsView.showAddedItem(item);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        String key = dataSnapshot.getKey();
                        itemsRepository.addNewItem(dataSnapshot, true);
                        Item item = itemsRepository.getDeletedItem(key);
                        if(listingDeleted) {
                            itemsView.showAddedItem(item);
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        String key = dataSnapshot.getKey();
                        Item deletedItem = itemsRepository.getDeletedItem(key);
                        itemsRepository.deleteLocalItem(deletedItem, true);
                        if(listingDeleted) {
                            itemsView.removeDeletedItem(deletedItem);
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public void onResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_ADD_NEW_ITEM){
                itemsView.showItemAddedMessage();
            }
            if(requestCode == REQUEST_ADD_NEW_ITEM_CHANGE){
                itemsView.showItemAddedMessage();
                action = null;
                itemsView.enableListSwipe(true);
            }
            if(requestCode == REQUEST_ITEM_EDIT){
                itemsView.showItemEditedMessage();
            }
        }
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
            if (itemsRepository.getItems().isEmpty()) {
                itemsView.showNoItems();
            }
        } else {
            if (itemsRepository.getDeletedItems().isEmpty()) {
                itemsView.showNoDeletedItems();
            }
        }
    }

    @Override
    public void setIntentInfo(Intent intent) {
        this.action = intent.getAction();

        if (action != null) {
            switch (action) {
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
        if (action == null) {
            itemsView.openItemDetails(item, listingDeleted);
        } else {
            itemsRepository.addFilesToItem(item, receivedFiles);
            itemsView.showFilesAddedMessage();
            action = null;
            itemsView.enableListSwipe(true);
        }
    }

    private void processItems(List<Item> items) {
        if (items.isEmpty()) {
            if (!listingDeleted) {
                itemsView.showNoItems();
            } else {
                itemsView.showNoDeletedItems();
            }
        } else {
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
