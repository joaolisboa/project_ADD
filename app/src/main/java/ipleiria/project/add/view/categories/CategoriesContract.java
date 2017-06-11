package ipleiria.project.add.view.categories;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesContract {

    interface View extends BaseView<Presenter> {

        void showProgressDialog();

        void hideProgressDialog();

        void setTitle(String title);

        void setCategoryPoints(Category category);

        void showDimensions(List<Dimension> dimensions);

        void showAreas(List<Area> areas);

        void showCriterias(List<Criteria> criterias);

        void showItemsList(List<Item> items);

        void hideSelectedDimension();

        void hideSelectedArea();

        void hideSelectedCriteria();

        void hideCategoryList();

        void showSelectedDimension(Dimension dimension);

        void showSelectedArea(Area area);

        void showSelectedCriteria(Criteria criteria);

        void showNoItems();

        void removeDeletedItem(Item item);

        void openItemDetails(Item clickedItem, boolean listingDeleted);

        void showFilesAddedMessage();

        void showItemAddedMessage();

        void showItemEditedMessage();

        void enableListSwipe(boolean b);

        void showSearchItems(List<Item> matchingItems);

        void showNoDeletedItems();
    }

    interface Presenter extends BasePresenter {

        void categoryClicked(Category dimension);

        boolean onBackPressed();

        void returnToDimensionView();

        void returnToAreaView();

        void returnToCriteriaView();

        void refreshData();

        void forceRefreshData();

        void onItemClicked(Item clickedItem);

        void deleteItem(Item deletedIttem);

        void permanentlyDeleteItem(Item item);

        void restoreItem(Item item);

        void onResult(int requestCode, int resultCode, Intent data);

        void searchItems(String query);

        void setIntentInfo(Intent intent);

        String getIntentAction();

        Criteria getSelectedCriteria();

        Bundle saveInstanceState();

        void restoreInstanceState(Bundle savedInstanceState);
    }

}
