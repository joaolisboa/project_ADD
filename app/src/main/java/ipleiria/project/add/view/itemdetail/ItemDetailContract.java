package ipleiria.project.add.view.itemdetail;

import android.support.annotation.NonNull;
import android.widget.ImageView;

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

        void openFileShare(File file);
    }

    interface Presenter extends BasePresenter{

        void checkForEmptyList();

        void deleteFile(@NonNull ItemFile file);

        void permanentlyDeleteFile(@NonNull ItemFile file);

        void restoreFile(@NonNull ItemFile file);

        void renameFile(@NonNull ItemFile file, @NonNull String newFilename);

        void onItemClicked(ItemFile clickedFile);

        void createThumbnail(ItemFile file);
    }
}
