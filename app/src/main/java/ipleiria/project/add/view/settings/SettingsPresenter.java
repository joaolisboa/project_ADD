package ipleiria.project.add.view.settings;

import com.hannesdorfmann.mosby3.mvp.MvpPresenter;

import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.view.base.LogPresenter;

/**
 * Created by Lisboa on 18-Dec-17.
 */

public class SettingsPresenter extends LogPresenter<SettingsView> {

    private static final String TAG = "SettingsPresenter";

    private UserService userService;

    public SettingsPresenter(UserService userService){
        super(TAG);

        this.userService = userService;
    }

}
