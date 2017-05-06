package ipleiria.project.add.view.settings;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ipleiria.project.add.R;
import ipleiria.project.add.view.items.ItemsActivity;
import ipleiria.project.add.view.items.ScrollChildSwipeRefreshLayout;

/**
 * Created by Lisboa on 06-May-17.
 */

public class SettingsFragment extends Fragment implements SettingsContract.View{

    private SettingsContract.Presenter settingsPresenter;

    public SettingsFragment() {}

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_frag, container, false);



        return root;
    }

    @Override
    public void setPresenter(SettingsContract.Presenter presenter) {
        settingsPresenter = presenter;
    }
}
