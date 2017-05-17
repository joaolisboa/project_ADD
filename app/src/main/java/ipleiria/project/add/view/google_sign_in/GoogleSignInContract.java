package ipleiria.project.add.view.google_sign_in;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;

/**
 * Created by J on 09/05/2017.
 */

public class GoogleSignInContract {

    interface View extends BaseView<Presenter>{

        void showUnauthenticatedUser();

        void showAuthenticatedUser(String mail);

        void showProgressDialog();

        void hideProgressDialog();

        void signIn(GoogleApiClient googleApiClient);

        void requestContactsPermission();

        void requestNewContactCreation(ArrayList<ContentProviderOperation> ops);
    }

    interface Presenter extends BasePresenter{

        void buildGoogleClient(FragmentActivity fragment,
                               GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener,
                               String webClientID);

        void onActivityResult(int requestCode, int resultCode, Intent data);

        void signIn();

        void downgradeAccount();

        void createContactAlias();
    }

}
