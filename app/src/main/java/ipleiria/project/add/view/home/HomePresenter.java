package ipleiria.project.add.view.home;

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
}
