package ipleiria.project.add.data.source;

import java.io.File;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.utils.PathUtils;

/**
 * Created by Lisboa on 06-May-17.
 */

// FilesRepository responsibility will be dealing with local/remote files while itemfilesRepository with Firebase
public class FilesRepository implements FilesDataSource {

    private static final String TRASH_PATH = "/trash";
    private static final String THUMBNAIL_PREFIX = "/thumb_";

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
    public void addFile(ItemFile newFile) {

    }

    @Override
    public File getFileThumbnail(ItemFile file) {
        return new File(Application.getAppContext().getCacheDir().getAbsolutePath().concat(THUMBNAIL_PREFIX).concat(file.getFilename()));
    }

    @Override
    public File getLocalFile(ItemFile file) {
        String relativeFilePath = PathUtils.getRelativeFilePath(file);
        String appdir = Application.getAppContext().getFilesDir().getAbsolutePath();

        if(!file.isDeleted()){
            return new File(appdir.concat(relativeFilePath));
        }
        return new File(appdir.concat(TRASH_PATH).concat(relativeFilePath));
    }

    @Override
    public File downloadThumbnail(ItemFile file){
        return new File("");
    }

    @Override
    public File downloadFile(ItemFile file) {
        return new File("");
    }

    @Override
    public void deleteFile(ItemFile file) {

    }

    @Override
    public void permanentlyDeleteFile(ItemFile file) {

    }

    @Override
    public void restoreFile(ItemFile file) {

    }

    @Override
    public void renameFile(ItemFile file, String oldFilename, String newFilename) {

    }
}
