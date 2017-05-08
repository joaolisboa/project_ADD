package ipleiria.project.add.data.source;

import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 06-May-17.
 */

public class FilesRepository implements FilesDataSource {

    private static FilesRepository INSTANCE = null;

    private final UserService userService;
    private final DropboxService dropboxService;
    private final MEOCloudService meoCloudService;

    public FilesRepository(){
        this.userService = UserService.getInstance();
        this.dropboxService = DropboxService.getInstance(userService.getDropboxToken());
        this.meoCloudService = MEOCloudService.getInstance(userService.getMeoCloudToken());
    }

    public static FilesRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FilesRepository();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }


    @Override
    public void deleteFile(ItemFile file) {

    }

    @Override
    public void permanenetlyDeleteFile(ItemFile file) {

    }

    @Override
    public void restoreFile(ItemFile file) {

    }

    @Override
    public void saveFile(ItemFile file) {

    }
}
