package ipleiria.project.add.view.add_edit_item;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.AddItemActivity;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.R;
import ipleiria.project.add.Utils.StringUtils;
import ipleiria.project.add.Utils.UriHelper;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.CategoryRepository;
import ipleiria.project.add.data.source.DropboxService;
import ipleiria.project.add.data.source.ItemsRepository;
import ipleiria.project.add.data.source.MEOCloudService;
import ipleiria.project.add.data.source.UserService;

import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PHOTO;

/**
 * Created by Lisboa on 07-May-17.
 */

public class AddEditPresenter implements AddEditContract.Presenter {

    private static final String TAG  ="AddEditPresenter";

    public static final String EDITING_ITEM = "editing_item_action";
    public static final String EDITING_ITEM_KEY = "item_key";

    private final AddEditContract.View addEditView;

    private final ItemsRepository itemsRepository;
    private final CategoryRepository categoryRepository;

    private String intentAction;

    private Criteria selectedCriteria;
    private Item editingItem;

    private List<Uri> receivedFiles;

    public AddEditPresenter(@NonNull AddEditContract.View addEditView, @NonNull ItemsRepository itemsRepository) {
        this.itemsRepository = itemsRepository;
        this.categoryRepository = CategoryRepository.getInstance();

        this.addEditView = addEditView;
        this.addEditView.setPresenter(this);

        this.receivedFiles = new ArrayList<>();
    }

    @Override
    public void subscribe(Intent intent) {
        if (!categoryRepository.getDimensions().isEmpty()) {
            addEditView.createTreeView(categoryRepository.getDimensions());
        } else {
            readCategories();
        }
        setIntentInfo(intent);
    }

    private void readCategories() {
        categoryRepository.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoryRepository.addDimensions(dataSnapshot);
                addEditView.createTreeView(categoryRepository.getDimensions());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    private void setIntentInfo(Intent intent) {
        intentAction = intent.getAction();

        if(intentAction != null) {
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

                case EDITING_ITEM:
                    String itemDbKey = intent.getStringExtra(EDITING_ITEM_KEY);
                    editingItem = itemsRepository.getItem(itemDbKey);
                    selectedCriteria = editingItem.getCriteria();
                    addEditView.setItemInfo(editingItem);
                    break;
            }
        }
        // no action means the app is opening to add a new item without file
    }

    @Override
    public void createTreeView() {

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
    }

    @Override
    public void finishAction() {
        String description = addEditView.getDescriptionText();
        if(intentAction != null) {
            switch (intentAction) {
                case Intent.ACTION_SEND:
                case Intent.ACTION_SEND_MULTIPLE:
                case SENDING_PHOTO:
                    Item item = new Item(description);
                    item.setCriteria(selectedCriteria);
                    itemsRepository.saveItem(item);
                    itemsRepository.addFilesToItem(item, receivedFiles);
                    break;

                case EDITING_ITEM:
                    editingItem.setDescription(description);
                    editingItem.setCriteria(selectedCriteria);
                    itemsRepository.saveItem(editingItem);
                    break;
            }
        }else{
            // creating new item
            Item item = new Item(description);
            item.setCriteria(selectedCriteria);
            itemsRepository.saveItem(item);
        }
        addEditView.finish();
    }
}
