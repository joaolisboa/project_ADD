package ipleiria.project.add.view.main;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.view.items.ItemsContract;

/**
 * Created by Lisboa on 05-May-17.
 */

public class MainContract {

    interface View extends BaseView<MainContract.Presenter> {

        void setLoadingIndicator(boolean active);

    }

    interface DrawerView {

        void setUserInfo(User user);

    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode, Context context);

        void setPhotoUri(Uri photoUri);
    }

}
