package ipleiria.project.add.view.settings;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.squareup.picasso.Picasso;

import java.util.List;

import ipleiria.project.add.*;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.meocloud.MEOCloudAPI;
import ipleiria.project.add.utils.CircleTransformation;
import ipleiria.project.add.utils.NetworkState;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.view.google_sign_in.GoogleSignInActivity;

/**
 * Created by Lisboa on 06-May-17.
 */

public class SettingsFragment extends Fragment implements SettingsContract.View {

    private SettingsContract.Presenter settingsPresenter;

    private ImageView meocloudState;
    private ImageView dropboxState;

    private LinearLayout dimensionWeightLimitsLayout;
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

        dimensionWeightLimitsLayout = (LinearLayout) root.findViewById(R.id.dimension_limits);

        profileImageView = (ImageView) root.findViewById(R.id.profile_pic);
        accountName = (TextView) root.findViewById(R.id.account_name);
        accountDescription = (TextView) root.findViewById(R.id.account_description);
        dropboxState = (ImageView) root.findViewById(R.id.dropbox_state);
        meocloudState = (ImageView) root.findViewById(R.id.meocloud_state);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        settingsPresenter.subscribe();
    }

    @Override
    public void onStop() {
        super.onStop();
        settingsPresenter.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_settings);
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
        accountDescription.setText(user.getEmail());

        Picasso.with(getContext())
                .load(user.getPhotoUrl())
                .resize(100, 100)
                .transform(new CircleTransformation())
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .into(profileImageView);

    }

    @Override
    public void setAnonymousUserInfo(User user) {
        accountName.setText(user.getName());
        accountDescription.setText(getString(R.string.google_sign_in_helper));
        profileImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.profile_placeholder));
    }

    @Override
    public void setMEOCloudStatus(boolean status) {
        if (status) {
            meocloudState.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.check_black));
        } else {
            meocloudState.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.check_black));
        }
    }

    @Override
    public void setDropboxStatus(boolean status) {
        if (status) {
            dropboxState.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.check_black));
        } else {
            dropboxState.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.check_black));
        }
    }

    @Override
    public void setDimensionViews(final List<Dimension> dimensions, User user) {
        dimensionWeightLimitsLayout.removeAllViewsInLayout();
        for (Dimension dimension : dimensions) {
            LinearLayout dimensionView =
                    (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dimension_weight_limit_layout, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            dimensionWeightLimitsLayout.addView(dimensionView, params);

            TextView name = (TextView) dimensionView.findViewById(R.id.name);
            TextView weightLimit = (TextView) dimensionView.findViewById(R.id.weight_limit);

            name.setText(dimension.getName());
            weightLimit.setText(String.valueOf(dimension.getWeight()));

            // this code along with createEditWeightDialog will allow only to edit one dimension
            // discarded because we need to make sure the sum of the dimension weights is 100
            /*dimensionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createEditWeightDialog(dimension);
                }
            });*/
        }

        dimensionWeightLimitsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createWeightsDialog(dimensions);
            }
        });

    }

    private void createEditWeightDialog(final Dimension dimension) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // create the custom dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();

        LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.edit_dimension_weight_dialog, null);

        final EditText weightInput = (EditText) dialogView.findViewById(R.id.weight_limit);
        weightInput.setText(String.valueOf(dimension.getWeight()));
        TextView dimensionName = (TextView) dialogView.findViewById(R.id.dimension_name);
        dimensionName.setText(dimension.getName());

        builder.setView(dialogView)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int weightInserted = Integer.valueOf(weightInput.getText().toString());
                        if (settingsPresenter.isWeightValid(dimension, weightInserted)) {
                            settingsPresenter.setWeight(dimension, weightInserted);
                            dialog.dismiss();
                        } else {
                            weightInput.setError("Limit must be between " + dimension.getMinWeight()
                                    + " and " + dimension.getMaxWeight());
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    private void createWeightsDialog(final List<Dimension> dimensions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // create the custom dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();

        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.edit_dimension_weights_dialog, null);
        final TextView sumErrorView = (TextView) layout.findViewById(R.id.weights_sum_error);

        for (final Dimension dimension : dimensions) {
            LinearLayout dimensionView = (LinearLayout) inflater.inflate(R.layout.edit_dimension_weight_dialog, null);

            final EditText weightInput = (EditText) dimensionView.findViewById(R.id.weight_limit);
            weightInput.setText(String.valueOf(dimension.getWeight()));
            TextView dimensionName = (TextView) dimensionView.findViewById(R.id.dimension_name);
            dimensionName.setText(dimension.getName());

            weightInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int weight = s.toString().isEmpty() ? 0 : Integer.valueOf(s.toString());
                    if (settingsPresenter.isWeightValid(dimension, weight)) {
                        weightInput.setError(null);
                    } else {
                        weightInput.setError("Limit must be between " + dimension.getMinWeight()
                                + " and " + dimension.getMaxWeight());
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            layout.addView(dimensionView);
        }

        builder.setView(layout)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (settingsPresenter.areWeightsValid()) {
                            if(settingsPresenter.isSumOfWeightsValid()){
                                settingsPresenter.commitDimensionWeightChanges();
                                dialog.dismiss();
                            }else{
                                sumErrorView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    public void showNoNetworkHint() {
        Toast.makeText(getContext(), "Can't establish connection", Toast.LENGTH_SHORT).show();
    }
}
