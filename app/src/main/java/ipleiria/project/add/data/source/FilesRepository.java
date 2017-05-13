package ipleiria.project.add.data.source;

import android.net.Uri;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.dropbox.DropboxCallback;
import ipleiria.project.add.dropbox.DropboxClientFactory;
import ipleiria.project.add.dropbox.DropboxDeleteFile;
import ipleiria.project.add.dropbox.DropboxDownloadFile;
import ipleiria.project.add.dropbox.DropboxGetThumbnail;
import ipleiria.project.add.dropbox.DropboxMoveFile;
import ipleiria.project.add.dropbox.DropboxUploadFile;
import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudAPI;
import ipleiria.project.add.meocloud.data.FileResponse;
import ipleiria.project.add.meocloud.data.MEOMetadata;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
import ipleiria.project.add.meocloud.tasks.MEOCreateFolderTree;
import ipleiria.project.add.meocloud.tasks.MEODeleteFile;
import ipleiria.project.add.meocloud.tasks.MEODownloadFile;
import ipleiria.project.add.meocloud.tasks.MEOGetThumbnail;
import ipleiria.project.add.meocloud.tasks.MEOMoveFile;
import ipleiria.project.add.meocloud.tasks.MEOUploadFile;
import ipleiria.project.add.utils.NetworkState;
import ipleiria.project.add.utils.PathUtils;

/**
 * Created by Lisboa on 06-May-17.
 */

public class FilesRepository implements FilesDataSource {

    private static final String TAG = "FILES_REPO";

    private static final String TRASH_PATH = "/trash";
    private static final String THUMBNAIL_PREFIX = "/thumb_";

    private static FilesRepository INSTANCE = null;

    private final DropboxService dropboxService;
    private final MEOCloudService meoCloudService;

    private List<ItemFile> localFiles;

    public FilesRepository() {
        UserService userService = UserService.getInstance();
        this.dropboxService = DropboxService.getInstance(userService.getDropboxToken());
        this.meoCloudService = MEOCloudService.getInstance(userService.getMeoCloudToken());

        localFiles = new LinkedList<>();
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
        return getFilePath(file, file.isDeleted());
    }

    private String getRelativePath(Criteria criteria){
        return "/" + criteria.getDimension().getReference() +
                "/" + criteria.getArea().getReference() +
                "/" + criteria.getReference();
    }

    private String getFilePath(ItemFile file, boolean deletedPath) {
        String path = "";
        if (deletedPath) {
            path = TRASH_PATH;
        }

        path += getRelativePath(file.getParent().getCriteria()) + "/" + file.getFilename();
        return path;
    }

    private String getFilePath(ItemFile file, Criteria newCriteria){
        return getRelativePath(newCriteria) + "/" + file.getFilename();
    }

    private String getFilePath(ItemFile file, String newFilename){
        return getRelativePath(file.getParent().getCriteria()) + "/" + newFilename;
    }

    @Override
    public void saveFile(final ItemFile newFile, final Uri uri) {
        if (meoCloudService.isAvailable()){
            String relativePath = getRelativePath(newFile.getParent().getCriteria());
            meoCloudService.uploadFile(uri, relativePath.substring(1), newFile.getFilename());
        }
        if (dropboxService.isAvailable()){
            // dropbox creates folders automatically so we don't need to seperate the path and filename
            dropboxService.uploadFile(uri, getFilePath(newFile), null);
        }
    }

    private File getCachedThumbnail(String filename){
        return new File(Application.getAppContext().getCacheDir().getAbsolutePath().concat(THUMBNAIL_PREFIX).concat(filename));
    }

    private File getLocalFile(ItemFile file) {
        String appdir = Application.getAppContext().getFilesDir().getAbsolutePath();
        return new File(appdir.concat(getFilePath(file)));
    }

    private void renameLocalFile(File from, File to){
        if(from.exists()) {
            String toPath = to.getAbsolutePath();
            File dir = new File(toPath.substring(0, toPath.lastIndexOf("/")));
            if(!dir.exists()){
                dir.mkdirs();
            }
            boolean success = from.renameTo(to);
            Log.d(TAG, "file rename successful: " + success);
        }
    }

    @Override
    public File getCachedThumbnail(ItemFile file) {
        return new File(Application.getAppContext().getCacheDir().getAbsolutePath().concat(THUMBNAIL_PREFIX).concat(file.getFilename()));
    }

    @Override
    public void getThumbnail(ItemFile file, BaseCallback<File> callback){
        File localThumb = getCachedThumbnail(file);
        if(localThumb.exists()){
            callback.onComplete(localThumb);
        }else{
            downloadThumbnail(file, callback);
        }
    }

    @Override
    public void getFileToShare(ItemFile file, Callback<File> callback) {
        File localFile = getLocalFile(file);
        if(localFile.exists()){
            callback.onComplete(localFile);
        }else if(NetworkState.isOnline()){
            downloadTempFile(file, callback);
        }
    }

    @Override
    public void getFile(ItemFile file, Callback<File> callback) {
        File localFile = getLocalFile(file);
        if(localFile.exists()){
            callback.onComplete(localFile);
        }else if(NetworkState.isOnline()){
            downloadFile(file, callback);
        }
    }

    @Override
    public void moveFile(ItemFile file, Criteria newCriteria) {
        String from = getFilePath(file);
        String to = getFilePath(file, newCriteria);
        if(meoCloudService.isAvailable()){
            meoCloudService.moveFile(from, to);
        }
        if(dropboxService.isAvailable()){
            dropboxService.moveFile(from, to);
        }
    }

    @Override
    public void downloadThumbnail(ItemFile file, final BaseCallback<File> callback) {
        final String filePath = getFilePath(file);

        if (meoCloudService.isAvailable()) {
            // meo cloud is preferred since it can create thumbnails for special files(mp4, pdf)
            // dropbox will serve as a fallback
            meoCloudService.downloadThumbnail(getFilePath(file), new Callback<File>() {
                @Override
                public void onComplete(File result) {
                    callback.onComplete(result);
                }

                @Override
                public void onError(Exception e) {
                    if(dropboxService.isAvailable()){
                        dropboxService.downloadThumbnail(filePath, new Callback<File>() {
                            @Override
                            public void onComplete(File result) {
                                callback.onComplete(result);
                            }

                            @Override
                            public void onError(Exception e) {
                                // ignore
                            }
                        });
                    }
                }
            });
        } else if (dropboxService.isAvailable()) {
            dropboxService.downloadThumbnail(filePath, new Callback<File>() {
                @Override
                public void onComplete(File result) {
                    callback.onComplete(result);
                }

                @Override
                public void onError(Exception e) {
                    //ignore
                }
            });
        }
    }

    private void downloadTempFile(ItemFile file, final Callback<File> callback){
        final String path = getFilePath(file);

        if (meoCloudService.isAvailable()) {
            // dropbox will serve as a fallback
            meoCloudService.downloadTempFile(path, new Callback<File>() {
                @Override
                public void onComplete(File result) {
                    callback.onComplete(result);
                }

                @Override
                public void onError(Exception e) {
                    if(dropboxService.isAvailable()){
                        dropboxService.downloadTempFile(path, callback);
                    }
                }
            });
        } else if (dropboxService.isAvailable()) {
            dropboxService.downloadTempFile(path, callback);
        }
    }

    private void downloadFile(ItemFile file, final Callback<File> callback) {
        final String path = getFilePath(file);

        if (meoCloudService.isAvailable()) {
            // dropbox will serve as a fallback
            meoCloudService.downloadFile(path, new Callback<File>() {
                @Override
                public void onComplete(File result) {
                    callback.onComplete(result);
                }

                @Override
                public void onError(Exception e) {
                    if(dropboxService.isAvailable()){
                        dropboxService.downloadFile(path, callback);
                    }
                }
            });
        } else if (dropboxService.isAvailable()) {
            dropboxService.downloadFile(path, callback);
        }
    }

    @Override
    public void deleteFile(ItemFile file) {
        String from = getFilePath(file, false);
        String to = getFilePath(file, true);
        if (meoCloudService.isAvailable()) {
            meoCloudService.moveFile(from, to);
        }
        if (dropboxService.isAvailable()) {
            dropboxService.moveFile(from, to);
        }
    }

    @Override
    public void restoreFile(ItemFile file) {
        String from = getFilePath(file, true);
        String to = getFilePath(file, false);
        if (meoCloudService.isAvailable()) {
            meoCloudService.moveFile(from, to);
        }
        if (dropboxService.isAvailable()) {
            dropboxService.moveFile(from, to);
        }
    }

    @Override
    public void renameFile(ItemFile file, String oldFilename, String newFilename) {
        String from = getFilePath(file, oldFilename);
        String to = getFilePath(file, newFilename);
        if (meoCloudService.isAvailable()) {
            meoCloudService.moveFile(from, to);
        }
        if (dropboxService.isAvailable()) {
            dropboxService.moveFile(from, to);
        }
        // rename cached thumbnail
        renameLocalFile(getCachedThumbnail(oldFilename), getCachedThumbnail(newFilename));
    }

    @Override
    public void permanentlyDeleteFile(ItemFile file) {
        String path = getFilePath(file);
        if (meoCloudService.isAvailable()) {
            meoCloudService.deleteFile(path);
        }
        if (dropboxService.isAvailable()) {
            dropboxService.deleteFile(path);
        }
        File localThumb = getCachedThumbnail(file);
        if(localThumb.exists()){
            localThumb.delete();
        }
    }

    // in some cases we don't care about errors, ie. downloading thumbnails
    public interface BaseCallback<I> {

        void onComplete(I result);

    }

    public interface Callback<I> extends BaseCallback<I> {

        void onError(Exception e);
    }
}
