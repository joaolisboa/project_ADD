package ipleiria.project.add.view.items;

import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;

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

        void openItemDetails(Item item);

        void showNoItems();

        void showNoDeletedItems();

        void finish();
    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode);

        void showFilteredItems();

        void searchItems(String query);

        void addNewItem();

        void openItemDetails(@NonNull Item item);

        void deleteItem(@NonNull Item completedTask);

        void permanentlyDeleteItem(@NonNull Item item);

        void restoreItem(@NonNull Item item);

        void setFiltering(int requestType);

        void checkForEmptyList();

        void setIntentInfo(Intent intent);

        String getIntentAction();

        void onItemClicked(Item item);
    }
}
