package ipleiria.project.add.view.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.view.items.ItemsContract;

/**
 * Created by Lisboa on 05-May-17.
 */

public class MainContract {

    interface View extends BaseView<MainContract.Presenter> {

        void setLoadingIndicator(boolean active);

        void showNoPendingFiles();

        void showPendingFiles(List<ItemFile> pendingFiles);

        void addPendingFiles(List<ItemFile> pendingFiles);
    }

    interface DrawerView {

        void setUserInfo(User user);

    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode, Context context);

        void setPhotoUri(Uri photoUri);

        void buildGoogleClient(FragmentActivity fragment,
                               GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener,
                               String webClientID);
    }

}
