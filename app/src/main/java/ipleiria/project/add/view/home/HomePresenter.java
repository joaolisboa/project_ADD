package ipleiria.project.add.view.home;

import android.net.Uri;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.view.base.LogPresenter;

/**
 * Created by Lisboa on 24-Oct-17.
 */

public class HomePresenter extends LogPresenter<HomeView> {

    private static final String TAG = "HomePresenter";

    private HomeView homeView;

    private UserService userService;
    private ItemsRepository itemsRepository;
    private FilesRepository filesRepository;

    private List<PendingFile> selectedPendingFiles;
    private Uri photoUri;

    public HomePresenter(UserService userService, ItemsRepository itemsRepository, FilesRepository filesRepository){
        super(TAG);
        this.userService = userService;
        this.itemsRepository = itemsRepository;
        this.filesRepository = filesRepository;

        this.selectedPendingFiles = new LinkedList<>();
    }

    @Override
    public void attachView(HomeView view) {
        super.attachView(view);
        homeView = view;
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if(!retainInstance){
            homeView = null;
        }
    }

    boolean userHasEvaluationPeriod() {
        return !userService.getUser().getEvaluationPeriods().isEmpty();
    }

    File getNewImageFile() {
        return filesRepository.createImageFile();
    }

    void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    void onRefresh() {
        homeView.showLoadingIndicator();
        //getPendingFiles();

        processPendingFiles();
    }

    private void processPendingFiles() {
        if (filesRepository.getPendingFiles().isEmpty()) {
            homeView.showNoPendingFiles();
        } else {
            homeView.showPendingFiles(filesRepository.getPendingFiles());
        }
    }
}
