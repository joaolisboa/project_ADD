package ipleiria.project.add.view.settings;

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
 * Created by Lisboa on 18-Dec-17.
 */

public class SettingsController extends BaseController implements SettingsView, MvpConductorDelegateCallback<SettingsView, SettingsPresenter> {

    public static final String TAG = "SettingsController";

    @Inject SettingsPresenter settingsPresenter;

    public SettingsController() {
        this(new BundleBuilder(new Bundle()).build());

        setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    }

    public SettingsController(Bundle args) {
        super(args);
        DaggerControllerComponent.builder()
                .repositoryComponent(Application.getRepositoryComponent())
                .presenterModule(new PresenterModule())
                .build().inject(this);

        addLifecycleListener(new MvpConductorLifecycleListener<>(this));
    }


    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_settings, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_title);
    }

    // MOSBY

    @NonNull
    @Override
    public SettingsPresenter createPresenter() {
        return settingsPresenter;
    }

    @Nullable
    @Override
    public SettingsPresenter getPresenter() {
        return settingsPresenter;
    }

    @Override
    public void setPresenter(@NonNull SettingsPresenter presenter) {
        this.settingsPresenter = presenter;
    }

    @NonNull
    @Override
    public SettingsView getMvpView() {
        return this;
    }
}
