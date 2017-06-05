package ipleiria.project.add.view.categories;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.utils.StringUtils;

import static android.app.Activity.RESULT_OK;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM_CHANGE;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ITEM_EDIT;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesPresenter implements CategoriesContract.Presenter {

    private static final String TAG = "CATEGORIES_PRESENTER";

    // only showing dimensions
    private static final int ROOT_FOCUS = 0;
    // showing selected dimension on the top with areas below
    private static final int DIMENSION_FOCUS = 1;
    // showing selected dimension/area on the top with criterias below
    private static final int AREA_FOCUS = 2;
    // showing selected dimension/area/criteria with the list of items of the selectedCriteria
    private static final int CRITERIA_FOCUS = 3;

    private int currentFocus = ROOT_FOCUS;

    private Dimension selectedDimension;
    private Area selectedArea;
    private Criteria selectedCriteria;

    private String action;
    private List<Uri> receivedFiles;

    private final CategoryRepository categoryRepository;
    private final ItemsRepository itemsRepository;
    private final CategoriesContract.View categoriesView;

    public CategoriesPresenter(CategoriesContract.View categoriesView, CategoryRepository categoryRepository, ItemsRepository itemsRepository) {
        this.categoryRepository = categoryRepository;
        this.itemsRepository = itemsRepository;
        this.categoriesView = categoriesView;
        this.categoriesView.setPresenter(this);

        this.receivedFiles = new ArrayList<>();
    }

    @Override
    public void subscribe() {
        refreshData();
    }


    public void refreshData(){
        categoriesView.showProgressDialog();

        categoryRepository.readData(new FilesRepository.Callback<List<Dimension>>() {
            @Override
            public void onComplete(List<Dimension> result) {
                //categoriesView.showDimensions(result);

                itemsRepository.getItems(new FilesRepository.Callback<List<Item>>() {
                    @Override
                    public void onComplete(List<Item> result) {
                        FileUtils.readExcel();
                        processList();
                        categoriesView.hideProgressDialog();
                    }

                    @Override
                    public void onError(Exception e) {
                        categoriesView.hideProgressDialog();
                    }
                });

            }

            @Override
            public void onError(Exception e) {
                categoriesView.hideProgressDialog();
            }
        });
    }

    private void processList(){
        switch (currentFocus) {
            case ROOT_FOCUS:
                categoriesView.setTitle("Dimensions");
                returnToDimensionView();
                break;

            case DIMENSION_FOCUS:
                categoriesView.setTitle("Areas");
                categoriesView.showSelectedDimension(selectedDimension);
                returnToAreaView();
                break;

            case AREA_FOCUS:
                categoriesView.setTitle("Criterias");
                categoriesView.showSelectedArea(selectedArea);
                returnToCriteriaView();
                break;

            case CRITERIA_FOCUS:
                categoriesView.setTitle("Items");
                categoriesView.showSelectedCriteria(selectedCriteria);
                processItemsList(selectedCriteria.getItems());
        }
    }

    private void processItemsList(List<Item> items){
        if(items.isEmpty()){
            categoriesView.showNoItems();
        }else{
            categoriesView.showItemsList(items);
        }
    }

    @Override
    public void unsubscribe() {
        categoriesView.hideProgressDialog();
    }

    @Override
    public void onResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_ADD_NEW_ITEM){
                categoriesView.showItemAddedMessage();
            }
            if(requestCode == REQUEST_ADD_NEW_ITEM_CHANGE){
                categoriesView.showItemAddedMessage();
                action = null;
                categoriesView.enableListSwipe(true);
            }
            if(requestCode == REQUEST_ITEM_EDIT){
                categoriesView.showItemEditedMessage();
            }
            refreshData();
        }
    }

    @Override
    public void searchItems(String query) {
        List<Item> matchingItems = new LinkedList<>();

        query = StringUtils.replaceDiacriticalMarks(query).toLowerCase();

        if (TextUtils.isEmpty(query)) {
            // go back to previous state
            processList();
        } else {
            List<Item> searchingList = itemsRepository.getItems();
            for (Item item : searchingList) {
                if (itemMatchesQuery(item, query)) {
                    matchingItems.add(item);
                }
            }
            categoriesView.showSearchItems(matchingItems);
        }
    }

    private boolean itemMatchesQuery(Item item, String query) {
        String criteriaName = StringUtils.replaceDiacriticalMarks(item.getCriteria().getName().toLowerCase());
        String itemDescription = StringUtils.replaceDiacriticalMarks(item.getDescription().toLowerCase());

        for(String tag: item.getTags()){
            if(tag.contains(query)){
                return true;
            }
        }

        return criteriaName.contains(query) || itemDescription.contains(query);
    }

    @Override
    public void categoryClicked(Category category) {
        if (category instanceof Dimension) {
            dimensionClicked((Dimension) category);
        } else if (category instanceof Area) {
            areaClicked((Area) category);
        } else if (category instanceof Criteria) {
            criteriaClicked((Criteria) category);
        } else {
            Log.e(TAG, "Invalid object - shouldn't happen");
        }

        switch (currentFocus) {
            case ROOT_FOCUS:
                categoriesView.setTitle("Dimensions");
                break;

            case DIMENSION_FOCUS:
                categoriesView.setTitle("Areas");
                break;

            case AREA_FOCUS:
                categoriesView.setTitle("Criterias");
                break;
        }
    }

    private void dimensionClicked(Dimension dimension) {
        selectedDimension = dimension;
        categoriesView.showSelectedDimension(dimension);
        categoriesView.showAreas(dimension.getAreas());
        currentFocus = DIMENSION_FOCUS;

    }

    private void areaClicked(Area area) {
        selectedArea = area;
        categoriesView.showSelectedArea(area);
        categoriesView.showCriterias(area.getCriterias());
        currentFocus = AREA_FOCUS;
    }

    private void criteriaClicked(Criteria criteria) {
        // only with AREA_FOCUS
        selectedCriteria = criteria;
        categoriesView.showSelectedCriteria(criteria);
        processItemsList(criteria.getItems());
        categoriesView.hideCategoryList();
        currentFocus = CRITERIA_FOCUS;
    }

    @Override
    public boolean onBackPressed() {
        switch(currentFocus){
            case DIMENSION_FOCUS:
                returnToDimensionView();
                return true;

            case AREA_FOCUS:
                returnToAreaView();
                return true;

            case CRITERIA_FOCUS:
                returnToCriteriaView();
                return true;
        }

        return false;
    }

    @Override
    public void returnToDimensionView() {
        categoriesView.hideSelectedDimension();
        categoriesView.hideSelectedArea();
        categoriesView.hideSelectedCriteria();
        categoriesView.showDimensions(categoryRepository.getDimensions());
        currentFocus = ROOT_FOCUS;
    }

    @Override
    public void returnToAreaView() {
        categoriesView.hideSelectedArea();
        categoriesView.hideSelectedCriteria();
        categoriesView.showSelectedDimension(selectedDimension);
        categoriesView.showAreas(selectedDimension.getAreas());
        currentFocus = DIMENSION_FOCUS;
    }

    @Override
    public void returnToCriteriaView() {
        categoriesView.hideSelectedCriteria();
        categoriesView.showSelectedDimension(selectedDimension);
        categoriesView.showSelectedArea(selectedArea);
        categoriesView.showCriterias(selectedArea.getCriterias());
        currentFocus = AREA_FOCUS;
    }

    @Override
    public void deleteItem(@NonNull Item item) {
        itemsRepository.deleteItem(item);
        categoriesView.removeDeletedItem(item);
    }

    @Override
    public void onItemClicked(Item clickedIem) {
        if (action == null) {
            categoriesView.openItemDetails(clickedIem);
        } else {
            itemsRepository.addFilesToItem(clickedIem, receivedFiles);
            categoriesView.showFilesAddedMessage();
            action = null;
            categoriesView.enableListSwipe(true);
        }
    }
}
