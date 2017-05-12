package ipleiria.project.add.data.source;

import android.util.Log;

import java.io.File;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.dropbox.DropboxCallback;
import ipleiria.project.add.dropbox.DropboxClientFactory;
import ipleiria.project.add.dropbox.DropboxGetThumbnail;
import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudAPI;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
import ipleiria.project.add.meocloud.tasks.MEOGetThumbnail;
import ipleiria.project.add.utils.PathUtils;

/**
 * Created by Lisboa on 06-May-17.
 */

// FilesRepository responsibility will be dealing with local/remote files while itemfilesRepository with Firebase
public class FilesRepository implements FilesDataSource {

    private static final String TAG = "FILES_REPO";

    private static final String TRASH_PATH = "/trash";
    private static final String THUMBNAIL_PREFIX = "/thumb_";

    private static FilesRepository INSTANCE = null;

    private final UserService userService;
    private final DropboxService dropboxService;
    private final MEOCloudService meoCloudService;

    public FilesRepository() {
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

    private String getFilePath(ItemFile file) {
        Criteria criteria = file.getParent().getCriteria();

        return "/" + criteria.getDimension().getReference() +
                "/" + criteria.getArea().getReference() +
                "/" + criteria.getReference() +
                "/" + file.getFilename();
    }

    @Override
    public void saveFile(ItemFile newFile) {

    }

    @Override
    public File getCachedThumbnail(ItemFile file) {
        return new File(Application.getAppContext().getCacheDir().getAbsolutePath().concat(THUMBNAIL_PREFIX).concat(file.getFilename()));
    }

    @Override
    public File getLocalFile(ItemFile file) {
        String relativeFilePath = PathUtils.getRelativeFilePath(file);
        String appdir = Application.getAppContext().getFilesDir().getAbsolutePath();

        if (!file.isDeleted()) {
            return new File(appdir.concat(relativeFilePath));
        }
        return new File(appdir.concat(TRASH_PATH).concat(relativeFilePath));
    }

    @Override
    public void downloadThumbnail(ItemFile file, final Callback<File> callback) {
        if (meoCloudService.isAvailable()) {
            // meo cloud is preferred since it can create thumbnails for some files(mp4, pdf)
            // dropbox will serve as a fallback
           MEODownloadThumbnail(getFilePath(file), callback);
        } else if (dropboxService.isAvailable()) {
            dropboxDownloadThumbnail(getFilePath(file), callback);
        }
    }

    private void dropboxDownloadThumbnail(String path, final Callback<File> callback){
        new DropboxGetThumbnail(DropboxClientFactory.getClient(), new DropboxCallback<File>() {
            @Override
            public void onComplete(File result) {
                callback.onComplete(result);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
                Log.e(TAG, e.getMessage(), e);
            }
        }).execute(path);
    }

    private void MEODownloadThumbnail(final String path, final Callback<File> callback){
        new MEOGetThumbnail(new MEOCallback<File>() {
            @Override
            public void onComplete(File result) {
                callback.onComplete(result);
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                if(dropboxService.isAvailable()) {
                    dropboxDownloadThumbnail(path, callback);
                }else{
                    callback.onError(httpE);
                    Log.e(TAG, httpE.getMessage(), httpE);
                }
            }

            @Override
            public void onError(Exception e) {
                if(dropboxService.isAvailable()) {
                    dropboxDownloadThumbnail(path, callback);
                }else{
                    callback.onError(e);
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }).execute(path, /*format*/ null, MEOCloudAPI.THUMBNAIL_SIZE_M);
    }

    @Override
    public void downloadFile(ItemFile file, final Callback<File> callback) {

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

    public interface Callback<I> {

        void onComplete(I result);

        void onError(Exception e);
    }
}
