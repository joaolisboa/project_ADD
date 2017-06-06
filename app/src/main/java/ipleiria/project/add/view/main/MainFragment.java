package ipleiria.project.add.view.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.*;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.utils.UriHelper;
import ipleiria.project.add.view.add_edit_item.AddEditActivity;
import ipleiria.project.add.view.items.ItemsActivity;
import ipleiria.project.add.view.items.ScrollChildSwipeRefreshLayout;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM;
import static ipleiria.project.add.view.main.MainActivity.TAG;
import static ipleiria.project.add.view.main.MainPresenter.REQUEST_TAKE_PHOTO;

/**
 * Created by Lisboa on 05-May-17.
 */

public class MainFragment extends Fragment implements MainContract.View,
        GoogleApiClient.OnConnectionFailedListener{

    public static final int REQUEST_AUTHORIZATION = 12345;

    private MainContract.Presenter presenter;

    private ListView pendingListView;
    private ArrayAdapter<String> listAdapter;

    public MainFragment() {}

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,
                new LinkedList<String>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_frag, container, false);

        pendingListView = (ListView) root.findViewById(R.id.pending_list);
        pendingListView.setAdapter(listAdapter);

        // Set up floating action button
        final FloatingActionButton fabPhoto = (FloatingActionButton) getActivity().findViewById(R.id.fab_photo);
        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        final FloatingActionButton fabAdd = (FloatingActionButton) getActivity().findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getContext(), AddEditActivity.class), REQUEST_ADD_NEW_ITEM);
            }
        });

        FloatingActionButton fabMenu = (FloatingActionButton) getActivity().findViewById(R.id.fab_menu);
        fabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fabPhoto.isShown() || fabAdd.isShown()){
                    fabPhoto.hide();
                    fabAdd.hide();
                }else {
                    fabPhoto.show();
                    fabAdd.show();
                }
            }
        });

        // Set up progress indicator
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );

        // Set the scrolling view in the custom SwipeRefreshLayout.
        //swipeRefreshLayout.setScrollUpChild(listView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.onSwipeRefresh();
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "ipleiria.project.add.store",
                        photoFile);
                presenter.setPhotoUri(photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // give permission to camera to read/write to URI - required for Android 4.4
                List<ResolveInfo> resolvedIntentActivities = getContext().getPackageManager().
                        queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                    String packageName = resolvedIntentInfo.activityInfo.packageName;
                    getContext().grantUriPermission(packageName, photoURI,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws Exception {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.result(requestCode, resultCode, getContext());
    }

    @Override
    public void showItemAddedMessage(){
        Snackbar.make(getView(), "New item saved", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onStart(){
        super.onStart();
        presenter.subscribe();
        presenter.buildGoogleClient(getActivity(), this, getString(R.string.default_web_client_id));
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.unsubscribe();
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    @Override
    public void setLoadingIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showNoPendingFiles() {

    }

    @Override
    public void showPendingFiles(List<ItemFile> pendingFiles) {
        listAdapter.clear();
        for(ItemFile file: pendingFiles) {
            addPendingFile(file);
        }
    }

    @Override
    public void addPendingFile(ItemFile file){
        listAdapter.add(file.getFilename());
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void addPendingFiles(List<ItemFile> pendingFiles) {
        for(ItemFile file: pendingFiles) {
            addPendingFile(file);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
