package ipleiria.project.add.view.base;

import android.util.Log;

import com.hannesdorfmann.mosby3.mvp.MvpPresenter;

/**
 * Created by Lisboa on 23-Nov-17.
 */

public class LogPresenter<V extends ControllerView> implements MvpPresenter<V>{

    private String tag;

    public LogPresenter(String tag){
        this.tag = tag;
    }

    @Override
    public void attachView(V view) {
        Log.d(tag, "attachView to:" + this);
    }

    @Override
    public void detachView(boolean retainInstance) {
        Log.d(tag, "detach view:" + this + " - retain:" + retainInstance);
    }
}
