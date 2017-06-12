package ipleiria.project.add.view.categories;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.utils.StringUtils;
import ipleiria.project.add.utils.UriHelper;

import static android.app.Activity.RESULT_OK;
import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PHOTO;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM_CHANGE;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ITEM_EDIT;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesPresenter implements CategoriesContract.Presenter {

    private static final String TAG = "CATEGORIES_PRESENTER";
    public static final String LIST_DELETED_KEY = "list_deleted";

    // only showing dimensions
    private static final int ROOT_FOCUS = 0;
    // showing selected dimension on the top with areas below
    private static final int DIMENSION_FOCUS = 1;
    // showing selected dimension/area on the top with criterias below
    private static final int AREA_FOCUS = 2;
    // showing selected dimension/area/criteria with the list of items of the selectedCriteria
    private static final int CRITERIA_FOCUS = 3;

    private int currentFocus;

    private Dimension selectedDimension;
    private Area selectedArea;
    private Criteria selectedCriteria;

    private boolean listingDeleted;
    private boolean forceRefresh;
    private String action;
    private List<Uri> receivedFiles;

    private final CategoryRepository categoryRepository;
    private final ItemsRepository itemsRepository;
    private final CategoriesContract.View categoriesView;

    public CategoriesPresenter(CategoriesContract.View categoriesView, CategoryRepository categoryRepository,
                               ItemsRepository itemsRepository, boolean listingDeleted) {
        this.categoryRepository = categoryRepository;
        this.itemsRepository = itemsRepository;
        this.categoriesView = categoriesView;
        this.categoriesView.setPresenter(this);

        this.listingDeleted = listingDeleted;
        this.receivedFiles = new ArrayList<>();

        this.currentFocus = ROOT_FOCUS;
        this.forceRefresh = true;
    }

    @Override
    public void subscribe() {
        categoriesView.setTitle(UserService.getInstance().getUser().getEvaluationPeriods().get(0).toString());
        refreshData();
    }

    @Override
    public void forceRefreshData() {
        // force data refresh since there may be new data synced
        // which will require a full evaluation of the points
        forceRefresh = true;
        refreshData();
    }

    public void refreshData() {
        categoriesView.showProgressDialog();
        new RefreshTask().execute();
    }

    private void refreshItems() {
        itemsRepository.getItems(listingDeleted, new FilesRepository.Callback<List<Item>>() {
            @Override
            public void onComplete(List<Item> result) {
                if (result.isEmpty()) {
                    // don't bother showing dimensions or anything else if there are no items
                    if (!listingDeleted) {
                        categoriesView.showNoItems();
                    } else {
                        categoriesView.showNoDeletedItems();
                    }
                } else {
                    evaluatePoints();
                    processList();
                }
                categoriesView.hideProgressDialog();
            }

            @Override
            public void onError(Exception e) {
                categoriesView.hideProgressDialog();
            }
        });
    }

    private void evaluatePoints() {
        if (!forceRefresh) {
            switch (currentFocus) {
                case CRITERIA_FOCUS:
                    // an item can be edited when items are shown(from details or list swipe action)
                    // in these scenarios only evaluate the criteria in the excel file
                    // to improve performance(~2x)
                    System.out.println("updating single criteria");
                    FileUtils.readExcel(selectedCriteria);
                    return;

                default:
                    break;
            }
        }
        forceRefresh = false;
        Log.d(TAG, "performing full refresh");
        FileUtils.readExcel();
    }

    private void processList() {
        switch (currentFocus) {
            case ROOT_FOCUS:
                returnToDimensionView();
                break;

            case DIMENSION_FOCUS:
                categoriesView.showSelectedDimension(selectedDimension);
                returnToAreaView();
                break;

            case AREA_FOCUS:
                categoriesView.showSelectedDimension(selectedDimension);
                categoriesView.showSelectedArea(selectedArea);
                returnToCriteriaView();
                break;

            case CRITERIA_FOCUS:
                categoriesView.showSelectedDimension(selectedDimension);
                categoriesView.showSelectedArea(selectedArea);
                categoriesView.showSelectedCriteria(selectedCriteria);
                processItemsList(selectedCriteria);
        }
    }

    private void processItemsList(Criteria criteria) {
        List<Item> items = (!listingDeleted ? criteria.getItems() : criteria.getDeletedItems());
        if (items.isEmpty()) {
            categoriesView.showNoItems();
            currentFocus = ROOT_FOCUS;
        } else {
            categoriesView.showItemsList(items);
        }
    }

    @Override
    public void unsubscribe() {
        categoriesView.hideProgressDialog();
    }

    @Override
    public void onResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_NEW_ITEM) {
                categoriesView.showItemAddedMessage();
            }
            if (requestCode == REQUEST_ADD_NEW_ITEM_CHANGE) {
                categoriesView.showItemAddedMessage();
                action = null;
                categoriesView.enableListSwipe(true);
            }
            if (requestCode == REQUEST_ITEM_EDIT) {
                categoriesView.showItemEditedMessage();
            }
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
            List<Item> searchingList = (!listingDeleted ? itemsRepository.getItems() : itemsRepository.getDeletedItems());
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

        for (String tag : item.getTags()) {
            if (tag.contains(query)) {
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
        processItemsList(criteria);
        categoriesView.hideCategoryList();
        currentFocus = CRITERIA_FOCUS;
    }

    @Override
    public boolean onBackPressed() {
        switch (currentFocus) {
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
        refreshData();
    }

    @Override
    public void permanentlyDeleteItem(@NonNull Item item) {
        itemsRepository.permanentlyDeleteItem(item);
        categoriesView.removeDeletedItem(item);
        // deleted items don't affect points so there's no point...
        // in refreshing data
    }

    @Override
    public void restoreItem(@NonNull Item item) {
        itemsRepository.restoreItem(item);
        categoriesView.removeDeletedItem(item);
        refreshData();
    }

    @Override
    public void onItemClicked(Item clickedItem) {
        if (action == null) {
            categoriesView.openItemDetails(clickedItem, listingDeleted);
        } else {
            itemsRepository.addFilesToItem(clickedItem, receivedFiles);
            categoriesView.showFilesAddedMessage();
            action = null;
            categoriesView.enableListSwipe(true);
            processItemsList(selectedCriteria);
        }
    }

    @Override
    public void setPeriodSelection() {
        /*itemsRepository.setCurrentPeriod(evaluationPeriod);
        categoriesView.setTitle(evaluationPeriod.toString());
        forceRefreshData();*/
        CharSequence periods[] = new CharSequence[UserService.getInstance().getUser().getEvaluationPeriods().size()];
        int i = 0;
        for(EvaluationPeriod evaluationPeriod: UserService.getInstance().getUser().getEvaluationPeriods()){
            periods[i] = evaluationPeriod.toString();
            i++;
        }
        categoriesView.openPeriodSelection(periods);
    }

    @Override
    public void switchPeriod(int position){
        EvaluationPeriod evaluationPeriod = UserService.getInstance().getUser().getEvaluationPeriods().get(position);
        itemsRepository.setCurrentPeriod(evaluationPeriod);
        categoriesView.setTitle(evaluationPeriod.toString());
        forceRefreshData();
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
    public Criteria getSelectedCriteria() {
        return selectedCriteria;
    }

    @Override
    public Bundle saveInstanceState() {
        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putInt("focus", currentFocus);
        if(selectedDimension != null) {
            savedInstanceState.putInt("dimension_ref", selectedDimension.getReference());
            if(selectedArea != null){
                savedInstanceState.putInt("area_ref", selectedArea.getReference());
                if(selectedCriteria != null){
                    savedInstanceState.putInt("criteria_ref", selectedCriteria.getReference());
                }
            }
        }

        return savedInstanceState;
    }

    @Override
    public void restoreInstanceState(Bundle savedInstanceState) {
        int dimensionRef = savedInstanceState.getInt("dimension_ref");
        int areaRef = savedInstanceState.getInt("area_ref");
        int criteriaRef = savedInstanceState.getInt("criteria_ref");

        currentFocus = savedInstanceState.getInt("focus");

        selectedDimension = categoryRepository.getDimensions().get(dimensionRef - 1);
        selectedArea = selectedDimension.getArea(areaRef - 1);
        selectedCriteria = selectedArea.getCriteria(criteriaRef - 1);

        processList();
    }

    private class RefreshTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            categoryRepository.readData(new FilesRepository.Callback<List<Dimension>>() {
                @Override
                public void onComplete(List<Dimension> result) {
                    refreshItems();
                }

                @Override
                public void onError(Exception e) {
                    categoriesView.hideProgressDialog();
                }
            });
            return null;
        }
    }
}
