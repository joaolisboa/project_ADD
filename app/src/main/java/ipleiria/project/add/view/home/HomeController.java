package ipleiria.project.add.view.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.mosby3.mvp.conductor.delegate.MvpConductorDelegateCallback;
import com.hannesdorfmann.mosby3.mvp.conductor.delegate.MvpConductorLifecycleListener;

import javax.inject.Inject;

import ipleiria.project.add.Application;
import ipleiria.project.add.R;
import ipleiria.project.add.dagger.component.DaggerControllerComponent;
import ipleiria.project.add.dagger.module.PresenterModule;
import ipleiria.project.add.utils.BundleBuilder;
import ipleiria.project.add.view.base.BaseController;

/**
 * Created by Lisboa on 24-Oct-17.
 */

public class HomeController extends BaseController implements HomeView, MvpConductorDelegateCallback<HomeView, HomePresenter> {

    public final static String TAG = "HomeController";

    private HomeViewState homeViewState;

    @Inject
    HomePresenter homePresenter;

    public HomeController() {
        this(new BundleBuilder(new Bundle())
                .build());

        setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    }

    public HomeController(Bundle args) {
        super(args);
        DaggerControllerComponent.builder()
                .repositoryComponent(Application.getRepositoryComponent())
                .presenterModule(new PresenterModule())
                .build().inject(this);

        addLifecycleListener(new MvpConductorLifecycleListener<>(this));
    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_home, container, false);
    }

    // MOSBY

    @NonNull
    @Override
    public HomePresenter createPresenter() {
        return homePresenter;
    }

    @Nullable
    @Override
    public HomePresenter getPresenter() {
        return homePresenter;
    }

    @Override
    public void setPresenter(@NonNull HomePresenter presenter) {
        this.homePresenter = presenter;
    }

    @NonNull
    @Override
    public HomeView getMvpView() {
        return this;
    }
}
