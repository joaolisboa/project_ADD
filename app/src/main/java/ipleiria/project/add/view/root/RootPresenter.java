package ipleiria.project.add.view.root;

import android.util.Log;

import com.hannesdorfmann.mosby3.mvp.MvpPresenter;

/**
 * Created by J on 12/09/2017.
 */

public class RootPresenter implements MvpPresenter<RootView>{

    private static final String TAG = "RootPresenter";

    private RootView rootView;

    public RootPresenter() {
    }

    @Override
    public void attachView(RootView view) {
        this.rootView = view;
        Log.d(TAG, "attachView with: " + this);
    }

    @Override
    public void detachView(boolean retainInstance) {
        if(!retainInstance){
            rootView = null;
        }
    }


}
