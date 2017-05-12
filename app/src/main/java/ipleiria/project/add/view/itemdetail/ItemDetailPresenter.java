package ipleiria.project.add.view.itemdetail;

import android.support.annotation.NonNull;
import android.widget.ImageView;

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

    public static final String ITEM_KEY = "item_key";

    private ItemDetailContract.View itemDetailView;
    private ItemDetailContract.FileView fileView;

    private final ItemFilesRepository itemFilesRepository;
    private final FilesRepository filesRepository;

    private Item item;
    private boolean listingDeleted;

    ItemDetailPresenter(@NonNull ItemDetailContract.View itemDetailView, FilesRepository filesRepository,
                        Item item, boolean listingDeleted){
        this.filesRepository = filesRepository;
        this.itemDetailView = itemDetailView;
        this.itemDetailView.setPresenter(this);

        this.item = item;
        this.itemFilesRepository = ItemFilesRepository.newInstance(item);
        this.listingDeleted = listingDeleted;
    }

    @Override
    public void subscribe() {
        itemDetailView.setAdapterPresenter(this);
        List<ItemFile> files = (!listingDeleted ? item.getFiles() : item.getDeletedFiles());
        itemDetailView.showFiles(files);
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
    public void createThumbnail(ItemFile file) {
        File thumbnail = filesRepository.getCachedThumbnail(file);
        if(thumbnail.exists()){
            fileView.setThumbnail(file, thumbnail);
        }else {
            File localFile = filesRepository.getLocalFile(file);
            if (localFile.exists()) {
                fileView.setThumbnail(file, localFile);
            } else {
                downloadThumbnail(file);
            }
        }
    }

    private void downloadThumbnail(final ItemFile file) {
        filesRepository.downloadThumbnail(file, new FilesRepository.Callback<File>() {
            @Override
            public void onComplete(File result) {
                fileView.setThumbnail(file, result);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    @Override
    public void setFileView(ItemDetailContract.FileView fileView) {
        this.fileView = fileView;
    }

    @Override
    public void onItemClicked(ItemFile clickedFile) {
        itemDetailView.showLoadingIndicator();
        // download file
        itemDetailView.hideLoadingIndicator();
    }
}
