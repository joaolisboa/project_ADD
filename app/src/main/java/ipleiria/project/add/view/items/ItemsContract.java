package ipleiria.project.add.view.items;

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
    interface ItemsActivityView{

        void setFilters(List<String> filters);

    }

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showItems(List<Item> items);

        void showAddItem();

        void showAddedItem(@NonNull Item item);

        void removeDeletedItem(@NonNull Item deletedItem);

        void showNoItems();

        void showNoDeletedItems();

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

        int getFiltering();

        void checkForEmptyList();
    }
}
