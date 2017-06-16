package ipleiria.project.add.view.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.view.items.ItemsContract;

/**
 * Created by Lisboa on 05-May-17.
 */

public class MainContract {

    interface View extends BaseView<MainContract.Presenter> {

        void hideLoadingIndicator();

        void showLoadingIndicator();

        void showNoPendingFiles();

        void showPendingFiles(List<PendingFile> pendingFiles);

        void addPendingFiles(List<PendingFile> pendingFiles);

        void addPendingFile(PendingFile file);

        void removePendingFile(PendingFile file);

        void showItemAdded(String itemKey);

        void requestThumbnail(PendingFile file);

        void setFileThumbnail(PendingFile file, File thumbnail);

        void openFileShare(String filePath);

        void setSelectMode(boolean selectMode);

        void showAddToItemOption();

        void hideAddToItemOption();

        void addPhotoURIToItems(String photoURI);

        void addFilesToItems(ArrayList<PendingFile> pendingFiles);

        boolean isFileSelected(PendingFile file);
    }

    interface Presenter extends BasePresenter {

        void onSwipeRefresh();

        void result(int requestCode, int resultCode, Intent data);

        void setPhotoUri(Uri photoUri);

        void buildGoogleClient(FragmentActivity fragment,
                               GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener,
                               String webClientID);

        void createThumbnail(PendingFile file);

        void onFileClicked(final PendingFile file);

        boolean isFileSelected(PendingFile file);

        void onFileSelected(PendingFile file);

        void onFileRemoved(PendingFile file);

        void addPendingFilesToItems();

        void refreshPendingFiles();
    }

}
