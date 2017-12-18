package ipleiria.project.add.view.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.hannesdorfmann.mosby3.mvp.conductor.delegate.MvpConductorDelegateCallback;
import com.hannesdorfmann.mosby3.mvp.conductor.delegate.MvpConductorLifecycleListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import ipleiria.project.add.Application;
import ipleiria.project.add.R;
import ipleiria.project.add.dagger.component.DaggerControllerComponent;
import ipleiria.project.add.dagger.module.PresenterModule;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.utils.BundleBuilder;
import ipleiria.project.add.view.ScrollChildSwipeRefreshLayout;
import ipleiria.project.add.view.add_edit_item.AddEditActivity;
import ipleiria.project.add.view.base.BaseController;
import ipleiria.project.add.view.main.PendingFileAdapter;

import static ipleiria.project.add.view.categories.CategoriesFragment.REQUEST_ADD_NEW_ITEM;
import static ipleiria.project.add.view.main.MainPresenter.REQUEST_TAKE_PHOTO;

/**
 * Created by Lisboa on 24-Oct-17.
 */

public class HomeController extends BaseController implements HomeView, MvpConductorDelegateCallback<HomeView, HomePresenter> {

    public final static String TAG = "HomeController";

    private static final int FAB_PHOTO = 0;
    private static final int FAB_CREATE_ACTIVITY = 1;

    private HomeViewState homeViewState;

    @BindView(R.id.no_items) RelativeLayout noPendingFilesView;
    @BindView(R.id.pending_list) ListView pendingListView;
    @BindView(R.id.refresh_layout) ScrollChildSwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.fab_photo) FloatingActionButton fabPhoto;
    @BindView(R.id.fab_add) FloatingActionButton fabAdd;
    @BindView(R.id.fab_menu) FloatingActionButton fabMenu;

    private PendingFileAdapter listAdapter;

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

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);

        listAdapter = new PendingFileAdapter(new ArrayList<PendingFile>(), pendingActionListener);
        pendingListView.setAdapter(listAdapter);
        homePresenter.onRefresh();

        // Set up floating action buttons
        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabOptionClick(FAB_PHOTO);
            }
        });
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabOptionClick(FAB_CREATE_ACTIVITY);
            }
        });
        fabMenu.setOnClickListener(fabMenuListener);

        swipeRefreshLayout.setScrollUpChild(pendingListView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showPendingFiles(new ArrayList<PendingFile>());
                homePresenter.onRefresh();
            }
        });
    }

    @Override
    public void showPendingFiles(final List<PendingFile> pendingFiles) {
        listAdapter.replaceData(pendingFiles);
        pendingListView.setVisibility(View.VISIBLE);
        noPendingFilesView.setVisibility(View.GONE);

        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void showNoPendingFiles() {
        noPendingFilesView.setVisibility(View.VISIBLE);
        pendingListView.setVisibility(View.GONE);

        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void showLoadingIndicator() {

    }

    @Override
    public void toggleFileSelected(PendingFile file, boolean select) {
        listAdapter.setFileSelected(file, select);
    }

    private void fabOptionClick(int option){
        if(homePresenter.userHasEvaluationPeriod()) {
            switch(option){
                case FAB_PHOTO: takePicture();
                    break;
                case FAB_CREATE_ACTIVITY: startActivityForResult(new Intent(getContext(), AddEditActivity.class), REQUEST_ADD_NEW_ITEM);
                    break;
            }
        }else{
            showErrorMessage(getString(R.string.hint_missing_evaluation_period));
        }
    }

    private View.OnClickListener fabMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(fabPhoto.isShown()){
                fabPhoto.hide();
                fabAdd.hide();
                fabMenu.setImageResource(R.drawable.vertical_menu);
            }else {
                fabPhoto.show();
                fabAdd.show();
                fabMenu.setImageResource(R.drawable.close_white);
            }
        }
    };

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = homePresenter.getNewImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "ipleiria.project.add.prov",
                        photoFile);
                homePresenter.setPhotoUri(photoURI);
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

    private PendingActions pendingActionListener = new PendingActions() {

        @Override
        public void onFileClick(PendingFile file) {
            homePresenter.onFileClicked(file);
        }

        @Override
        public void onLongFileClick(PendingFile file) {
            homePresenter.toggleFileSelection(file);
        }

        @Override
        public void onFileDelete(PendingFile file) {
            homePresenter.onFileRemoved(file);
        }
    };

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
