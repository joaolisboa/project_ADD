package ipleiria.project.add.view.items;

import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 04-May-17.
 */

public class ItemsContract {

    interface View extends BaseView<Presenter> {

        void setFilters(List<String> filters);

        void setLoadingIndicator(boolean active);

        void showItems(List<Item> items);

        void showAddedItem(@NonNull Item item);

        void removeDeletedItem(@NonNull Item deletedItem);

        void openItemDetails(Item item, boolean listingDeleted);

        void showNoItems();

        void showNoDeletedItems();

        void showFilesAddedMessage();

        void showItemAddedMessage();

        void showItemEditedMessage();

        void enableListSwipe(boolean enable);
    }

    interface Presenter extends BasePresenter {

        void onResult(int requestCode, int resultCode, Intent data);

        void showFilteredItems();

        void searchItems(String query);

        void deleteItem(@NonNull Item item);

        void permanentlyDeleteItem(@NonNull Item item);

        void restoreItem(@NonNull Item item);

        void setFiltering(int requestType);

        void checkForEmptyList();

        void setIntentInfo(Intent intent);

        String getIntentAction();

        void onItemClicked(Item item);
    }
}
