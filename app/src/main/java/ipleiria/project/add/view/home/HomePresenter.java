package ipleiria.project.add.view.home;

import android.util.Log;

import com.hannesdorfmann.mosby3.mvp.MvpPresenter;

/**
 * Created by Lisboa on 24-Oct-17.
 */

public class HomePresenter implements MvpPresenter<HomeView> {

    private static final String TAG = "HomePresenter";
    private HomeView homeView;

    @Override
    public void attachView(HomeView view) {
        Log.d(TAG, "attachView to:" + this);
        homeView = view;
    }

    @Override
    public void detachView(boolean retainInstance) {
        if(!retainInstance){
            homeView = null;
        }
    }
}
