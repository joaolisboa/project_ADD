package ipleiria.project.add.view.itemdetail;

import android.content.Intent;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 10-May-17.
 */

public class ItemDetailContract {

    interface View extends BaseView<Presenter>{

        void showItemInfo(Item item);

        void showFiles(List<ItemFile> files);

        void showNoFiles();

        void showAddedFile(ItemFile file);

        void removeDeletedFile(ItemFile deletedFile);

        void showLoadingIndicator();

        void hideLoadingIndicator();

        void setFileThumbnail(ItemFile file, File thumbnail);

        void requestThumbnail(ItemFile file);

        void openFileShare(String filePath);

        void showTags(List<String> tags, List<String> suggestions);

        void showNoTags();

        void openEditItemView(Item item);

        void showFileNotFound();
    }

    interface Presenter extends BasePresenter{

        void checkForEmptyList();

        void deleteFile(@NonNull ItemFile file);

        void permanentlyDeleteFile(@NonNull ItemFile file);

        void restoreFile(@NonNull ItemFile file);

        void renameFile(@NonNull ItemFile file, @NonNull String newFilename);

        void onItemClicked(ItemFile clickedFile);

        void createThumbnail(ItemFile file);

        void addTag(String tag);

        void removeTag(String tag);

        void onEditItemClicked();

        void onResult(int requestCode, int resultCode, Intent data);

    }
}
