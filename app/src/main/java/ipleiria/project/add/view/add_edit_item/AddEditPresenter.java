package ipleiria.project.add.view.add_edit_item;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.utils.StringUtils;
import ipleiria.project.add.utils.UriHelper;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;

import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PENDING_FILES;
import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PHOTO;

/**
 * Created by Lisboa on 07-May-17.
 */

public class AddEditPresenter implements AddEditContract.Presenter {

    private static final String TAG = "AddEditPresenter";

    public static final String EDITING_ITEM = "editing_item_action";
    public static final String EDITING_ITEM_KEY = "item_key";
    // to save the user one step, we can select the criteria depending on the context
    // ie. user opened a criteria with no items and clicked the option to add items
    public static final String CRITERIA_SELECTED = "criteria_selected";

    private final AddEditContract.View addEditView;

    private final ItemsRepository itemsRepository;
    private final CategoryRepository categoryRepository;

    private String intentAction;

    private Criteria selectedCriteria;
    private String description;
    private long weight = 1;
    private Item editingItem;

    private List<Uri> receivedFiles;
    private List<PendingFile> receivedPendingFiles;

    AddEditPresenter(@NonNull AddEditContract.View addEditView, @NonNull ItemsRepository itemsRepository) {
        this.itemsRepository = itemsRepository;
        this.categoryRepository = CategoryRepository.getInstance();

        this.addEditView = addEditView;
        this.addEditView.setPresenter(this);

        this.receivedFiles = new ArrayList<>();
        this.receivedPendingFiles = new ArrayList<>();
    }

    @Override
    public void subscribe(Intent intent) {
        addEditView.showProgressDialog();
        readCategories();
        setIntentInfo(intent);
    }

    private void readCategories() {
        categoryRepository.readData(new FilesRepository.Callback<List<Dimension>>() {
            @Override
            public void onComplete(List<Dimension> result) {
                refreshItems();
            }

            @Override
            public void onError(Exception e) {
                addEditView.hideProgressDialog();
            }
        });
    }

    private void refreshItems() {
        itemsRepository.getItems(false, new FilesRepository.Callback<List<Item>>() {
            @Override
            public void onComplete(List<Item> result) {
                final Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    public void run() {
                        FileUtils.readExcel();
                        handler.post(new Runnable() {
                            public void run() {
                                addEditView.hideProgressDialog();
                                addEditView.createTreeView(categoryRepository.getDimensions());
                            }
                        });

                    }
                };
                new Thread(runnable).start();
            }

            @Override
            public void onError(Exception e) {
                addEditView.hideProgressDialog();
            }
        });
    }

    private void setIntentInfo(Intent intent) {
        intentAction = intent.getAction();

        if (intentAction != null) {
            switch (intentAction) {
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
                    System.out.println("on set intent: " + receivedPendingFiles);
                    break;

                case EDITING_ITEM:
                    String itemDbKey = intent.getStringExtra(EDITING_ITEM_KEY);
                    editingItem = itemsRepository.getItem(itemDbKey);
                    selectedCriteria = editingItem.getCriteria();
                    addEditView.setItemInfo(editingItem);
                    // fab will show by default only if editing item
                    // because it already has data
                    addEditView.showFloatingActionButton();
                    break;

                case CRITERIA_SELECTED:
                    String reference = intent.getStringExtra(CRITERIA_SELECTED);
                    selectedCriteria = categoryRepository.getCriteriaFromReference(reference);
                    addEditView.setSelectedCriteria(selectedCriteria);
            }
        }
        // no action means the app is opening to add a new item without file
    }

    @Override
    public void searchForCriteria(String query) {
        List<Criteria> results = new LinkedList<>();

        query = StringUtils.replaceDiacriticalMarks(query).toLowerCase();

        if (TextUtils.isEmpty(query)) {
            addEditView.hideSearch();
        } else {
            for (Criteria criterio : categoryRepository.getCriterias()) {
                String stringcriterio = StringUtils.replaceDiacriticalMarks(criterio.getName().toLowerCase());
                if (stringcriterio.contains(query)) {
                    results.add(criterio);
                }
            }
            addEditView.showSearchItems(results);
        }
    }

    @Override
    public void selectedCriteria(Criteria criteria) {
        selectedCriteria = criteria;
        addEditView.setSelectedCriteria(criteria);
        verifyInput();
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
        verifyInput();
    }

    @Override
    public void verifyInput() {
        if (description == null || description.isEmpty() || weight <= 0 || selectedCriteria == null) {
            addEditView.hideFloatingActionButton();
        } else {
            addEditView.showFloatingActionButton();
        }
    }

    @Override
    public void setWeight(String weight) {
        if (weight == null || weight.isEmpty()) {
            this.weight = 1;
        } else {
            this.weight = Integer.valueOf(weight);
        }
        verifyInput();
    }

    @Override
    public void finishAction() {
        if (intentAction != null) {
            switch (intentAction) {
                case Intent.ACTION_SEND:
                case Intent.ACTION_SEND_MULTIPLE:
                case SENDING_PHOTO:
                case CRITERIA_SELECTED:
                case SENDING_PENDING_FILES:
                    Item item = new Item(description);
                    item.setCriteria(selectedCriteria, false);
                    item.setWeight(weight);
                    itemsRepository.addFilesToItem(item, receivedFiles);
                    System.out.println("on finish action: " + receivedPendingFiles);
                    if (!receivedPendingFiles.isEmpty()) {
                        itemsRepository.addPendingFilesToItem(item, receivedPendingFiles);
                    }
                    itemsRepository.saveItem(item, false);
                    addEditView.finishAction(item);
                    break;

                case EDITING_ITEM:
                    itemsRepository.editItem(editingItem, description, selectedCriteria, weight);
                    addEditView.finishAction();
                    break;
            }
        } else {
            // creating new item
            Item item = new Item(description);
            item.setCriteria(selectedCriteria, false);
            item.setWeight(weight);
            itemsRepository.saveItem(item, false);
            addEditView.finishAction(item);
        }
    }

    public String getIntentAction(){
        return intentAction;
    }

}
