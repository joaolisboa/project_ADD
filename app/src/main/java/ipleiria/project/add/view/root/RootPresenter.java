package ipleiria.project.add.view.root;

import android.util.Log;

import ipleiria.project.add.view.base.LogPresenter;

/**
 * Created by J on 12/09/2017.
 */

public class RootPresenter extends LogPresenter<RootView> {

    private static final String TAG = "RootPresenter";

    private RootView rootView;

    public RootPresenter() {
        super(TAG);
    }

    @Override
    public void attachView(RootView view) {
        super.attachView(view);
        this.rootView = view;
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if(!retainInstance){
            rootView = null;
        }
    }


}
