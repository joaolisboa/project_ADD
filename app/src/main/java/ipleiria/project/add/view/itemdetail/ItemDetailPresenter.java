package ipleiria.project.add.view.itemdetail;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.database.ItemFilesRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;

import static android.app.Activity.RESULT_OK;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM_CHANGE;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ITEM_EDIT;

/**
 * Created by Lisboa on 10-May-17.
 */

public class ItemDetailPresenter implements ItemDetailContract.Presenter {

    private static final String TAG = "ITEM_DETAIL_PRESENTER";

    public static final String ITEM_KEY = "item_key";

    private ItemDetailContract.View itemDetailView;

    private ItemFilesRepository itemFilesRepository;
    private final FilesRepository filesRepository;

    private Item item;
    private boolean listingDeleted;

    private File sharedFile;

    ItemDetailPresenter(@NonNull ItemDetailContract.View itemDetailView, FilesRepository filesRepository,
                        Item item, boolean listingDeleted) {
        this.filesRepository = filesRepository;
        this.itemDetailView = itemDetailView;
        this.itemDetailView.setPresenter(this);

        this.item = item;
        this.listingDeleted = listingDeleted;
    }

    @Override
    public void subscribe() {
        this.itemFilesRepository = ItemFilesRepository.newInstance(item);

        List<ItemFile> files = (!listingDeleted ? item.getFiles() : item.getDeletedFiles());
        processFiles(files);
        processTags(item.getTags());
        itemDetailView.showItemInfo(item);

        // android doesn't seem to ever delete temp file or files with deleteOnExit()
        // so when activity resumes if a file was shared we delete it
        // ps: in case the user uses the app offline or already has a local file
        // we only delete the file if it start with tmp_ since that is the prefix added when
        // downloading the file
        // TODO: 08-Jun-17 Delete temp file only when leaving the activity(avoid repeated downloads(faster reopening) if user wants to review the file)
        if (sharedFile != null && sharedFile.exists() && sharedFile.getName().startsWith("tmp_")) {
            sharedFile.delete();
            sharedFile = null;
        }
    }

    private void processTags(List<String> tags) {
        if (listingDeleted) {
            itemDetailView.showNoTags();
        } else {
            itemDetailView.showTags(tags, itemFilesRepository.getTagSuggestions());
        }
    }

    private void processFiles(List<ItemFile> files) {
        if (files.isEmpty()) {
            itemDetailView.showNoFiles();
        } else {
            itemDetailView.showFiles(files);
        }
    }

    @Override
    public void unsubscribe() {
        ItemFilesRepository.destroyInstance();
    }

    @Override
    public void checkForEmptyList() {
        if (!listingDeleted) {
            if (item.getFiles().isEmpty() /* && item.getEmails().isEmpty()*/) {
                itemDetailView.showNoFiles();
            }
        } else {
            if (item.getDeletedFiles().isEmpty() /* && item.getDeletedEmails().isEmpty()*/) {
                itemDetailView.showNoFiles();
            }
        }
    }

    @Override
    public void deleteFile(@NonNull ItemFile file) {
        itemFilesRepository.deleteItemFile(file);
        filesRepository.deleteFile(file);
        itemDetailView.removeDeletedFile(file);
        checkForEmptyList();
    }

    @Override
    public void permanentlyDeleteFile(@NonNull ItemFile file) {
        itemFilesRepository.permanentlyDeleteItemFile(file);
        filesRepository.permanentlyDeleteFile(file);
        itemDetailView.removeDeletedFile(file);
    }

    @Override
    public void restoreFile(@NonNull ItemFile file) {
        itemFilesRepository.restoreItemFile(file);
        filesRepository.restoreFile(file);
        itemDetailView.removeDeletedFile(file);
    }

    @Override
    public void renameFile(@NonNull ItemFile file, @NonNull String newFilename) {
        String oldFilename = file.getFilename();
        if (!oldFilename.equals(newFilename)) {
            file.setFilename(newFilename);
            itemFilesRepository.renameItemFile(file);
            filesRepository.renameFile(file, oldFilename, newFilename);
            itemDetailView.showAddedFile(file);
        }
    }

    @Override
    public void createThumbnail(final ItemFile file) {
        filesRepository.getThumbnail(file, new FilesRepository.BaseCallback<File>() {
            @Override
            public void onComplete(File result) {
                itemDetailView.setFileThumbnail(file, result);
            }
        });

    }

    @Override
    public void addTag(String tag) {
        itemFilesRepository.addTag(tag);
    }

    @Override
    public void removeTag(String tag) {
        itemFilesRepository.removeTag(tag);
    }

    @Override
    public void onEditItemClicked() {
        itemDetailView.openEditItemView(item);
    }

    @Override
    public void onResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ITEM_EDIT) {
                itemDetailView.showItemInfo(item);
                processTags(item.getTags());
            }
        }
    }

    @Override
    public void onItemClicked(final ItemFile clickedFile) {
        if (clickedFile.getFilename().substring(clickedFile.getFilename().lastIndexOf(".") + 1).equals("eml")) {
            File email = new File(Application.getAppContext().getFilesDir(), clickedFile.getFilename());
            sharedFile = email;
            itemDetailView.openFileShare(filesRepository.getRelativePath(email));
        } else {
            itemDetailView.showLoadingIndicator();
            filesRepository.getFileToShare(clickedFile, new FilesRepository.Callback<File>() {
                @Override
                public void onComplete(File result) {
                    itemDetailView.hideLoadingIndicator();
                    sharedFile = result;
                    itemDetailView.openFileShare(filesRepository.getRelativePath(result));
                }

                @Override
                public void onError(Exception e) {
                    itemDetailView.hideLoadingIndicator();
                    // show error message for file not found
                    Log.d(TAG, "File not found - missing locally and/or remotely");
                    Log.e(TAG, e.getMessage(), e);
                }
            });
        }
    }
}
