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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.*;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.utils.UriHelper;
import ipleiria.project.add.view.ScrollChildSwipeRefreshLayout;
import ipleiria.project.add.view.add_edit_item.AddEditActivity;
import ipleiria.project.add.view.categories.CategoriesActivity;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;
import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PENDING_FILES;
import static ipleiria.project.add.view.add_edit_item.AddEditFragment.SENDING_PHOTO;
import static ipleiria.project.add.view.categories.CategoriesFragment.REQUEST_ADD_NEW_ITEM;
import static ipleiria.project.add.view.categories.CategoriesPresenter.OPEN_ITEM_ADDED;
import static ipleiria.project.add.view.main.MainActivity.TAG;
import static ipleiria.project.add.view.main.MainPresenter.REQUEST_TAKE_PHOTO;

/**
 * Created by Lisboa on 05-May-17.
 */

public class MainFragment extends Fragment implements MainContract.View,
        GoogleApiClient.OnConnectionFailedListener{

    public static final int REQUEST_AUTHORIZATION = 12345;

    private MainContract.Presenter presenter;

    private LinearLayout noPendingFilesView;
    private ListView pendingListView;
    private PendingFileAdapter listAdapter;

    private FloatingActionButton fabPhoto;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabMenu;
    private boolean fabShow = false;

    private Snackbar snackbar;

    public MainFragment() {}

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listAdapter = new PendingFileAdapter(new LinkedList<PendingFile>(), pendingActionListener, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_frag, container, false);

        noPendingFilesView = (LinearLayout) root.findViewById(R.id.noItems);
        pendingListView = (ListView) root.findViewById(R.id.pending_list);
        pendingListView.setAdapter(listAdapter);

        // Set up floating action buttons
        fabPhoto = (FloatingActionButton) getActivity().findViewById(R.id.fab_photo);
        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        fabAdd = (FloatingActionButton) getActivity().findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getContext(), AddEditActivity.class), REQUEST_ADD_NEW_ITEM);
            }
        });

        fabMenu = (FloatingActionButton) getActivity().findViewById(R.id.fab_menu);
        fabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabShow = !fabShow;
                toggleFabMenu();
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
        swipeRefreshLayout.setScrollUpChild(pendingListView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.onSwipeRefresh();
                presenter.buildGoogleClient(getActivity(), MainFragment.this, getString(R.string.default_web_client_id));
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.result(requestCode, resultCode, data);
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
    public void onResume(){
        super.onResume();
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_home);
        fabShow = false;
        toggleFabMenu();
    }

    private void toggleFabMenu(){
        if(!fabShow){
            fabPhoto.hide();
            fabAdd.hide();
            fabMenu.setImageResource(R.drawable.vertical_menu);
        }else {
            fabPhoto.show();
            fabAdd.show();
            fabMenu.setImageResource(R.drawable.close_white);
        }
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        this.presenter = checkNotNull(presenter);
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
    public void showItemAdded(String itemKey){
        Intent openItemAdded = new Intent(getContext(), CategoriesActivity.class);
        if(itemKey != null) {
            openItemAdded.putExtra("item_added_key", itemKey);
            openItemAdded.setAction(OPEN_ITEM_ADDED);
        } else{
            Log.d(TAG, "Shouldn't happen");
        }
        startActivity(openItemAdded);
        //Snackbar.make(getView(), "New item saved", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void setFileThumbnail(PendingFile file, File thumbnail) {
        listAdapter.setThumbnail(file, thumbnail);
    }

    @Override
    public void openFileShare(String filePath) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = filePath.substring(filePath.indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);
        if(ext.equals("eml")){
            type = "message/rfc822";
        }

        Intent shareIntent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = UriHelper.getUriFromAppfile(filePath);
        shareIntent.setDataAndType(fileUri, type);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Open file"));
    }

    @Override
    public void setSelectMode(boolean selectMode) {
        listAdapter.setSelectMode(selectMode);
    }

    @Override
    public void showAddToItemOption() {
        fabShow = false;
        toggleFabMenu();
        fabMenu.hide();
        if(snackbar == null){
            snackbar = Snackbar.make(getView(), "Add file(s) to item", Snackbar.LENGTH_INDEFINITE)
                    .setAction("ADD", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            presenter.addPendingFilesToItems();
                        }
                    });

            final View snackbarView = snackbar.getView();
            snackbarView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    snackbarView.getViewTreeObserver().removeOnPreDrawListener(this);
                    ((CoordinatorLayout.LayoutParams) snackbarView.getLayoutParams()).setBehavior(null);
                    return true;
                }
            });
        }

        if(!snackbar.isShown()){
            snackbar.show();
        }
    }

    @Override
    public void hideAddToItemOption() {
        fabMenu.show();
        snackbar.dismiss();
        snackbar = null;
    }

    @Override
    public void addPhotoURIToItems(String photoURI) {
        Intent intent = new Intent(getContext(), CategoriesActivity.class);
        intent.putExtra("photo_uri", photoURI);
        getActivity().startActivity(intent.setAction(SENDING_PHOTO));
    }

    @Override
    public void addFilesToItems(ArrayList<PendingFile> pendingFiles) {
        Intent intent = new Intent(getContext(), CategoriesActivity.class);
        intent.putParcelableArrayListExtra("pending_files", pendingFiles);
        getActivity().startActivity(intent.setAction(SENDING_PENDING_FILES));
    }

    @Override
    public void showLoadingIndicator(){
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(true);
            }
        });
    }

    @Override
    public void hideLoadingIndicator(){
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(false);
            }
        });
    }

    @Override
    public void showNoPendingFiles() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noPendingFilesView.setVisibility(View.VISIBLE);
                pendingListView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showPendingFiles(final List<PendingFile> pendingFiles) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.replaceData(pendingFiles);
                pendingListView.setVisibility(View.VISIBLE);
                noPendingFilesView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void removePendingFile(PendingFile file) {
        listAdapter.onFileRemoved(file);
    }

    @Override
    public boolean isFileSelected(PendingFile file){
        return presenter.isFileSelected(file);
    }

    @Override
    public void openCategories() {
        startActivity(new Intent(getContext(), CategoriesActivity.class));
        getActivity().finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    PendingActionListener pendingActionListener = new PendingActionListener() {

        @Override
        public void onFileClick(PendingFile file) {
            presenter.onFileClicked(file);
        }

        @Override
        public void onLongFileClick(PendingFile file, View view) {
            if(presenter.isFileSelected(file)) {
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
            }else{
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_light));
            }
            presenter.onFileSelected(file);
        }

        @Override
        public void onFileDelete(PendingFile file) {
            presenter.onFileRemoved(file);
        }
    };

    interface PendingActionListener{

        void onFileClick(PendingFile file);

        void onLongFileClick(PendingFile file, View view);

        void onFileDelete(PendingFile file);

    }

}
