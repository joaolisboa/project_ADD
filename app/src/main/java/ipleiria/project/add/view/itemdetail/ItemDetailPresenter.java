package ipleiria.project.add.view.itemdetail;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.io.File;
import java.util.Arrays;

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
        // set firebase listener for item files?
        itemDetailView.setAdapterPresenter(this);
        itemDetailView.showFiles(!listingDeleted ? item.getFiles() : item.getDeletedFiles());
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
        System.out.println(item);
        System.out.println("restoring file: " + file);
        System.out.println("1" + Arrays.toString(item.getFiles().toArray()));
        System.out.println("2" + Arrays.toString(item.getDeletedFiles().toArray()));
        itemFilesRepository.restoreItemFile(file);
        filesRepository.restoreFile(file);
        itemDetailView.removeDeletedFile(file);
    }

    @Override
    public void renameFile(@NonNull ItemFile file, @NonNull String newFilename) {
        String oldFilename = file.getFilename();
        file.setFilename(newFilename);
        itemFilesRepository.renameItemFile(file);
        filesRepository.renameFile(file, oldFilename, newFilename);
        itemDetailView.showAddedFile(file);
    }

    @Override
    public void createThumbnail(ItemFile file, ImageView thumbView) {
        File thumbnail = filesRepository.getFileThumbnail(file);
        if(thumbnail.exists()){
            fileView.setThumbnail(thumbView, thumbnail);
        }else {
            File localFile = filesRepository.getLocalFile(file);
            if (localFile.exists()) {
                fileView.setThumbnail(thumbView, localFile);
            } else {
                downloadThumbnail(file, thumbView);
            }
        }
    }

    @Override
    public void downloadThumbnail(ItemFile file, ImageView thumbView) {
        File downloadedFile = filesRepository.downloadFile(file);
        if(downloadedFile.exists()){
            fileView.setThumbnail(thumbView, downloadedFile);
        }
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
