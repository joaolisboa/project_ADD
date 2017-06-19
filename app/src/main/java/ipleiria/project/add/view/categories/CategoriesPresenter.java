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

import ipleiria.project.add.DrawerView;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.utils.StringUtils;
import ipleiria.project.add.utils.UriHelper;

import static android.app.Activity.RESULT_OK;
import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PENDING_FILES;
import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PHOTO;
import static ipleiria.project.add.view.categories.CategoriesFragment.REQUEST_ADD_NEW_ITEM;
import static ipleiria.project.add.view.categories.CategoriesFragment.REQUEST_ADD_NEW_ITEM_CHANGE;
import static ipleiria.project.add.view.categories.CategoriesFragment.REQUEST_ITEM_EDIT;
import static ipleiria.project.add.view.main.MainPresenter.REQUEST_TAKE_PHOTO;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesPresenter implements CategoriesContract.Presenter {

    private static final String TAG = "CATEGORIES_PRESENTER";
    public static final String LIST_DELETED_KEY = "list_deleted";
    public static final String OPEN_ITEM_ADDED = "open_item_added";

    // only showing dimensions
    private static final int ROOT_FOCUS = 0;
    // showing selected dimension on the top with areas below
    private static final int DIMENSION_FOCUS = 1;
    // showing selected dimension/area on the top with criterias below
    private static final int AREA_FOCUS = 2;
    // showing selected dimension/area/criteria with the list of items of the selectedCriteria
    private static final int CRITERIA_FOCUS = 3;

    private int currentFocus;

    private String selectedDimension;
    private String selectedArea;
    private String selectedCriteria;

    private boolean listingDeleted;
    private String action;
    private List<Uri> receivedFiles;
    private Uri photoUri;

    private List<PendingFile> receivedPendingFiles;

    private final CategoryRepository categoryRepository;
    private final ItemsRepository itemsRepository;
    private final UserService userService;

    private final CategoriesContract.View categoriesView;
    private final DrawerView drawerView;

    public CategoriesPresenter(CategoriesContract.View categoriesView, DrawerView drawerView,
                               CategoryRepository categoryRepository,
                               ItemsRepository itemsRepository,
                               UserService userService,
                               boolean listingDeleted) {
        this.categoryRepository = categoryRepository;
        this.itemsRepository = itemsRepository;
        this.userService = userService;
        this.categoriesView = categoriesView;
        this.categoriesView.setPresenter(this);
        this.drawerView = drawerView;

        this.listingDeleted = listingDeleted;
        this.receivedFiles = new ArrayList<>();
        this.receivedPendingFiles = new ArrayList<>();

        this.currentFocus = ROOT_FOCUS;
    }

    @Override
    public void subscribe() {
        if (listingDeleted) {
            categoriesView.setTitle("Trash " + itemsRepository.getCurrentPeriod().toString());
        } else {
            categoriesView.setTitle(itemsRepository.getCurrentPeriod().toString());
        }
        refreshData();
        drawerView.setUserInfo(userService.getUser());
        categoriesView.selectNavigationItem(listingDeleted);
    }

    @Override
    public void unsubscribe() {
        categoriesView.hideProgressDialog();
    }

    public void refreshData() {
        categoriesView.showProgressDialog();
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
                    categoriesView.hideProgressDialog();
                } else {
                    final Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        public void run() {
                            FileUtils.readExcel();
                            handler.post(new Runnable() {
                                public void run() {
                                    processList();
                                    categoriesView.hideProgressDialog();
                                }
                            });

                        }
                    };
                    new Thread(runnable).start();
                }
            }

            @Override
            public void onError(Exception e) {
                categoriesView.hideProgressDialog();
            }
        });
    }

    private void processList() {
        switch (currentFocus) {
            case ROOT_FOCUS:
                returnToDimensionView();
                break;

            case DIMENSION_FOCUS:
                categoriesView.showSelectedDimension(categoryRepository.getDimension(selectedDimension));
                returnToAreaView();
                break;

            case AREA_FOCUS:
                Dimension dimension = categoryRepository.getDimension(selectedDimension);
                categoriesView.showSelectedDimension(dimension);
                categoriesView.showSelectedArea(dimension.getArea(selectedArea));
                returnToCriteriaView();
                break;

            case CRITERIA_FOCUS:
                Dimension dimension_ = categoryRepository.getDimension(selectedDimension);
                categoriesView.showSelectedDimension(dimension_);
                Area area = dimension_.getArea(selectedArea);
                categoriesView.showSelectedArea(area);
                Criteria criteria = area.getCriteria(selectedCriteria);
                categoriesView.showSelectedCriteria(criteria);
                processItemsList(criteria);
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
    public void onResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_NEW_ITEM) {
                String itemKey = data.getStringExtra("item_added_key");
                Item item = itemsRepository.getItem(itemKey);
                selectedCriteria = item.getCriteria().getDbKey();
                selectedArea = item.getArea().getDbKey();
                selectedDimension = item.getDimension().getDbKey();
                currentFocus = CRITERIA_FOCUS;
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
            if (requestCode == REQUEST_TAKE_PHOTO) {
                categoriesView.enableListSwipe(false);
                action = SENDING_PHOTO;
                receivedFiles.add(photoUri);
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

                case SENDING_PENDING_FILES:
                    receivedPendingFiles = intent.getParcelableArrayListExtra("pending_files");
                    break;

                case OPEN_ITEM_ADDED:
                    String itemKey = intent.getStringExtra("item_added_key");
                    Item item = itemsRepository.getItem(itemKey);
                    selectedCriteria = item.getCriteria().getDbKey();
                    selectedArea = item.getArea().getDbKey();
                    selectedDimension = item.getDimension().getDbKey();
                    currentFocus = CRITERIA_FOCUS;
                    break;
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
        selectedDimension = dimension.getDbKey();
        categoriesView.showSelectedDimension(dimension);
        categoriesView.showAreas(dimension.getAreas());
        currentFocus = DIMENSION_FOCUS;

    }

    private void areaClicked(Area area) {
        selectedArea = area.getDbKey();
        categoriesView.showSelectedArea(area);
        categoriesView.showCriterias(area.getCriterias());
        currentFocus = AREA_FOCUS;
    }

    private void criteriaClicked(Criteria criteria) {
        selectedCriteria = criteria.getDbKey();
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

        Dimension dimension = categoryRepository.getDimension(selectedDimension);
        categoriesView.showSelectedDimension(dimension);
        categoriesView.showAreas(dimension.getAreas());
        currentFocus = DIMENSION_FOCUS;
    }

    @Override
    public void returnToCriteriaView() {
        categoriesView.hideSelectedCriteria();

        Dimension dimension = categoryRepository.getDimension(selectedDimension);
        categoriesView.showSelectedDimension(dimension);
        Area area = dimension.getArea(selectedArea);
        categoriesView.showSelectedArea(area);
        categoriesView.showCriterias(area.getCriterias());
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
            if (action.equals(SENDING_PHOTO)) {
                itemsRepository.addFilesToItem(clickedItem, receivedFiles);
            } else if (action.equals(SENDING_PENDING_FILES)) {
                itemsRepository.addPendingFilesToItem(clickedItem, receivedPendingFiles);
            }
            categoriesView.showFilesAddedMessage();
            action = null;
            categoriesView.enableListSwipe(true);
            processItemsList(categoryRepository.getCriteria(selectedCriteria));
        }
    }

    @Override
    public void setPeriodSelection() {
        CharSequence periods[] = new CharSequence[userService.getUser().getEvaluationPeriods().size()];
        int i = 0;
        for (EvaluationPeriod evaluationPeriod : userService.getUser().getEvaluationPeriods()) {
            periods[i] = evaluationPeriod.toString();
            i++;
        }
        categoriesView.openPeriodSelection(periods);
    }

    @Override
    public void switchPeriod(int position) {
        EvaluationPeriod evaluationPeriod = userService.getUser().getEvaluationPeriods().get(position);
        itemsRepository.setCurrentPeriod(evaluationPeriod);
        if (listingDeleted) {
            categoriesView.setTitle("Trash " + evaluationPeriod.toString());
        } else {
            categoriesView.setTitle(evaluationPeriod.toString());
        }
        refreshData();
    }

    @Override
    public String getIntentAction() {
        return action;
    }

    @Override
    public Criteria getSelectedCriteria() {
        return categoryRepository.getCriteria(selectedCriteria);
    }

    @Override
    public Bundle saveInstanceState() {
        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putInt("focus", currentFocus);
        if (selectedDimension != null) {
            Dimension dimension = categoryRepository.getDimension(selectedDimension);
            savedInstanceState.putInt("dimension_ref", dimension.getReference());
            if (selectedArea != null) {
                Area area = dimension.getArea(selectedArea);
                savedInstanceState.putInt("area_ref", area.getReference());
                if (selectedCriteria != null) {
                    savedInstanceState.putInt("criteria_ref", area.getCriteria(selectedCriteria).getReference());
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

        Dimension dimension = categoryRepository.getDimensions().get(dimensionRef - 1);
        selectedDimension = dimension.getDbKey();
        Area area = dimension.getArea(areaRef - 1);
        selectedArea = area.getDbKey();
        selectedCriteria = area.getCriteria(criteriaRef - 1).getDbKey();

        processList();
    }

    @Override
    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public boolean isListingDeleted(){
        return listingDeleted;
    }

}
