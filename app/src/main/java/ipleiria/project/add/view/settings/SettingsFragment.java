package ipleiria.project.add.view.settings;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.squareup.picasso.Picasso;

import ipleiria.project.add.*;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.Utils.CircleTransformation;
import ipleiria.project.add.Utils.NetworkState;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.view.GoogleSignInActivity;

/**
 * Created by Lisboa on 06-May-17.
 */

public class SettingsFragment extends Fragment implements SettingsContract.View {

    private SettingsContract.Presenter settingsPresenter;

    private ImageView meocloudState;
    private ImageView dropboxState;

    private ImageView profileImageView;
    private TextView accountName;
    private TextView accountDescription;

    public SettingsFragment() {
    }

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
        View root = inflater.inflate(R.layout.settings_frag, container, false);

        profileImageView = (ImageView) root.findViewById(R.id.profile_pic);
        accountName = (TextView) root.findViewById(R.id.account_name);
        accountDescription = (TextView) root.findViewById(R.id.account_description);
        dropboxState = (ImageView) root.findViewById(R.id.dropbox_state);
        meocloudState = (ImageView) root.findViewById(R.id.meocloud_state);

        settingsPresenter.updateUserInfo();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        settingsPresenter.onActivityResume();
    }

    @Override
    public void setPresenter(SettingsContract.Presenter presenter) {
        settingsPresenter = presenter;
    }

    public void onDropboxClick() {
        settingsPresenter.onDropboxAction();
    }

    public void onMEOCloudClick() {
        settingsPresenter.onMEOCloudAction();
    }

    public void onGoogleAccountClick() {
        if (NetworkState.isOnline()) {
            settingsPresenter.setLoginIntention(true);
            startActivity(new Intent(getContext(), GoogleSignInActivity.class));
        } else {
            showNoNetworkHint();
        }
    }

    @Override
    public void showDropboxLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to disconnect Dropbox?")
                .setTitle("Confirm");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (NetworkState.isOnline(getContext())) {
                    settingsPresenter.signOutDropbox();
                } else {
                    showNoNetworkHint();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void showMEOCloudLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to disconnect MEO Cloud?")
                .setTitle("Confirm");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (NetworkState.isOnline()) {
                    settingsPresenter.signOutMEOCloud();
                } else {
                    showNoNetworkHint();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void showDropboxLogin() {
        if (NetworkState.isOnline()) {
            Auth.startOAuth2Authentication(getContext(), getString(R.string.dropbox_app_key));
            settingsPresenter.setLoginIntention(true);
        } else {
            showNoNetworkHint();
        }
    }

    @Override
    public void showMEOCloudLogin() {
        if (NetworkState.isOnline()) {
            MEOCloudAPI.startOAuth2Authentication(getContext(), getString(R.string.meo_consumer_key));
            settingsPresenter.setLoginIntention(true);
        } else {
            showNoNetworkHint();
        }
    }

    @Override
    public void setUserInfo(User user) {
        accountName.setText(user.getName());
        accountDescription.setText(getString(R.string.account_syncing_status, user.getEmail()));

        Picasso.with(getContext())
                .load(user.getPhoto_url())
                .resize(100, 100)
                .transform(new CircleTransformation())
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .into(profileImageView);

    }

    @Override
    public void setAnonymousUserInfo() {
        accountName.setText(R.string.anonymous_name);
        accountDescription.setText(getString(R.string.google_sign_in_helper));
        profileImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.profile_placeholder));
    }

    @Override
    public void setMEOCloudStatus(boolean status) {
        if(status){
            meocloudState.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.check_black));
        }else{
            meocloudState.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.check_black));
        }
    }

    @Override
    public void setDropboxStatus(boolean status) {
        if(status){
            dropboxState.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.check_black));
        }else{
            dropboxState.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.check_black));
        }
    }

    public void showNoNetworkHint() {
        Toast.makeText(getContext(), "Can't establish connection", Toast.LENGTH_SHORT).show();
    }
}
