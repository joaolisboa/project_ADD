package ipleiria.project.add.view.itemdetail;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.List;

import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.database.ItemFilesRepository;

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
                        Item item, boolean listingDeleted){
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
        itemDetailView.showFiles(files);

        // android doesn't seem to ever delete temp file or files with deleteOnExit()
        // so when activity resumes if a file was shared we delete it
        // ps: in case the user uses the app offline or already has a local file
        // we only delete the file if it start with tmp_ since that is the prefix added when
        // downloading the file
        if(sharedFile != null && sharedFile.exists() && sharedFile.getName().startsWith("tmp_")){
            sharedFile.delete();
            sharedFile = null;
        }
    }

    @Override
    public void unsubscribe() {
        ItemFilesRepository.destroyInstance();
    }

    @Override
    public void checkForEmptyList() {
        if (!listingDeleted) {
            if(item.getFiles().isEmpty()) {
                itemDetailView.showNoFiles();
            }
        } else {
            if(item.getDeletedFiles().isEmpty()) {
                itemDetailView.showNoFiles();
            }
        }
    }

    @Override
    public void deleteFile(@NonNull ItemFile file) {
        itemFilesRepository.deleteItemFile(file);
        filesRepository.deleteFile(file);
        itemDetailView.removeDeletedFile(file);
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
        if(!oldFilename.equals(newFilename)) {
            file.setFilename(newFilename);
            itemFilesRepository.renameItemFile(file);
            filesRepository.renameFile(file, oldFilename, newFilename);
            itemDetailView.showAddedFile(file);
        }
    }

    @Override
    public void createThumbnail(final ItemFile file) {
        File thumbnail = filesRepository.getCachedThumbnail(file);
        if(thumbnail.exists()){
            itemDetailView.setFileThumbnail(file, thumbnail);
        }else {
            filesRepository.getThumbnail(file, new FilesRepository.BaseCallback<File>() {
                @Override
                public void onComplete(File result) {
                    itemDetailView.setFileThumbnail(file, result);
                }
            });
        }
    }

    @Override
    public void onItemClicked(final ItemFile clickedFile) {
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
