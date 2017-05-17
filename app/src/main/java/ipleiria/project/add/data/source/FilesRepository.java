package ipleiria.project.add.data.source;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.meocloud.data.MEOMetadata;
import ipleiria.project.add.utils.NetworkState;

/**
 * Created by Lisboa on 06-May-17.
 */

public class FilesRepository implements FilesDataSource {

    private static final String TAG = "FILES_REPO";

    private static final String TRASH_PATH = "/trash";
    private static final String THUMBNAIL_PREFIX = "/thumb_";
    private static final String PENDING_PATH = "";

    private static FilesRepository INSTANCE = null;

    private final DropboxService dropboxService;
    private final MEOCloudService meoCloudService;

    private List<ItemFile> localFiles;

    private List<ItemFile> pendingFiles;

    public FilesRepository() {
        UserService userService = UserService.getInstance();
        this.dropboxService = DropboxService.getInstance(userService.getDropboxToken());
        this.meoCloudService = MEOCloudService.getInstance(userService.getMeoCloudToken());

        localFiles = new LinkedList<>();
        pendingFiles = new LinkedList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final CategoryRepository categoryRepository = CategoryRepository.getInstance();

                if (categoryRepository.getDimensions().isEmpty()) {
                    categoryRepository.readCriteria();
                    categoryRepository.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            categoryRepository.addDimensions(dataSnapshot);
                            searchForLocalFiles(categoryRepository.getDimensions());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                } else {
                    searchForLocalFiles(categoryRepository.getDimensions());
                }
            }
        }).start();
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

    private void searchForLocalFiles(List<Dimension> dimensions) {
        for (Dimension dimension : dimensions) {
            File dimensiondir = new File(Application.getAppContext().getFilesDir() + "/" + dimension.getReference());
            if (dimensiondir.isDirectory()) {
                goThroughFolder(dimensiondir);
            }
        }
        Log.d(TAG, "Local files found: " + localFiles);
    }

    private void goThroughFolder(File dir) {
        for (File fileInDir : dir.listFiles()) {
            if (fileInDir.isDirectory()) {
                goThroughFolder(fileInDir);
            } else {
                localFiles.add(new ItemFile(fileInDir.getName()));
            }
        }
    }

    private String getFilePath(ItemFile file) {
        return getFilePath(file, file.isDeleted());
    }

    private String getRelativePath(Criteria criteria) {
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

    private String getFilePath(ItemFile file, Criteria newCriteria) {
        return getRelativePath(newCriteria) + "/" + file.getFilename();
    }

    private String getFilePath(ItemFile file, String newFilename) {
        return getRelativePath(file.getParent().getCriteria()) + "/" + newFilename;
    }

    @Override
    public String getRelativePath(File file) {
        int start = Application.getAppContext().getFilesDir().getAbsolutePath().length();
        return file.getAbsolutePath().substring(start, file.getAbsolutePath().length());
    }

    @Override
    public void addPendingFiles(List<ItemFile> files) {
        pendingFiles.addAll(files);
    }

    @Override
    public void saveEmailAttachment(String filename, byte[] fileByteArray) throws IOException {
        File attachment = getLocalPendingFile(filename);
        // don't bother writing attachment if it already exists in app
        if(attachment.exists()) {
            FileOutputStream f = new FileOutputStream(attachment);
            f.write(fileByteArray);
            f.close();
        }
        pendingFiles.add(new ItemFile(filename));
    }

    @Override
    public void getRemotePendingFiles(final BaseCallback<List<ItemFile>> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Semaphore semaphore = new Semaphore(-1);

                if(meoCloudService.isAvailable()) {
                    meoCloudService.getMetadata(PENDING_PATH, new BaseCallback<MEOMetadata>() {
                        @Override
                        public void onComplete(MEOMetadata result) {
                            for(MEOMetadata meoMetadata: result.getContents()){
                                if(!meoMetadata.isDir()){
                                    ItemFile newFile = new ItemFile(meoMetadata.getName());
                                    if(!pendingFiles.contains(newFile)) {
                                        pendingFiles.add(newFile);
                                    }
                                }
                            }
                            semaphore.release();
                        }
                    });
                }else{
                    semaphore.release();
                }
                if(dropboxService.isAvailable()){
                    dropboxService.getMetadata(PENDING_PATH, new BaseCallback<ListFolderResult>() {
                        @Override
                        public void onComplete(ListFolderResult result) {
                            for(Metadata metadata: result.getEntries()){
                                if(metadata instanceof FileMetadata) {
                                    ItemFile newFile = new ItemFile(metadata.getName());
                                    if(!pendingFiles.contains(newFile)) {
                                        pendingFiles.add(newFile);
                                    }
                                }
                            }
                            semaphore.release();
                        }
                    });
                }else{
                    semaphore.release();
                }
                try {
                    semaphore.acquire();
                    callback.onComplete(pendingFiles);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void saveFile(final ItemFile newFile, final Uri uri) {
        if (meoCloudService.isAvailable()) {
            String relativePath = getRelativePath(newFile.getParent().getCriteria());
            meoCloudService.uploadFile(uri, relativePath.substring(1), newFile.getFilename());
        }
        if (dropboxService.isAvailable()) {
            // dropbox creates folders automatically so we don't need to seperate the path and filename
            dropboxService.uploadFile(uri, getFilePath(newFile), null);
        }
    }

    private File getLocalPendingFile(String filename){
        return new File(Application.getAppContext().getFilesDir(), filename);
    }

    private File getCachedThumbnail(String filename) {
        return new File(Application.getAppContext().getCacheDir().getAbsolutePath().concat(THUMBNAIL_PREFIX).concat(filename));
    }

    private File getLocalFile(ItemFile file) {
        String appdir = Application.getAppContext().getFilesDir().getAbsolutePath();
        return new File(appdir.concat(getFilePath(file)));
    }

    private void renameLocalFile(File from, File to) {
        if (from.exists()) {
            String toPath = to.getAbsolutePath();
            File dir = new File(toPath.substring(0, toPath.lastIndexOf("/")));
            if (!dir.exists()) {
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
    public void getThumbnail(ItemFile file, BaseCallback<File> callback) {
        File localThumb = getCachedThumbnail(file);
        if (localThumb.exists()) {
            callback.onComplete(localThumb);
        } else {
            File localFile = getLocalFile(file);
            if (localFile.exists()) {
                callback.onComplete(localFile);
            } else {
                downloadThumbnail(file, callback);
            }
        }
    }

    @Override
    public void getFileToShare(ItemFile file, Callback<File> callback){
        File localFile = getLocalFile(file);
        if (localFile.exists()) {
            callback.onComplete(localFile);
        } else if (NetworkState.isOnline()) {
            downloadTempFile(file, callback);
        }
    }

    @Override
    public void getFile(ItemFile file, Callback<File> callback) {
        File localFile = getLocalFile(file);
        if (localFile.exists()) {
            callback.onComplete(localFile);
        } else if (NetworkState.isOnline()) {
            downloadFile(file, callback);
        }
    }

    @Override
    public void moveFile(ItemFile file, Criteria newCriteria) {
        String from = getFilePath(file);
        String to = getFilePath(file, newCriteria);
        if (meoCloudService.isAvailable()) {
            meoCloudService.moveFile(from, to);
        }
        if (dropboxService.isAvailable()) {
            dropboxService.moveFile(from, to);
        }
    }

    @Override
    public void downloadThumbnail(ItemFile file, final BaseCallback<File> callback) {
        final String filePath = getFilePath(file);

        if (meoCloudService.isAvailable()) {
            // meo cloud is preferred since it can create thumbnails for special files(like pdf)
            // dropbox will serve as a fallback
            meoCloudService.downloadThumbnail(getFilePath(file), new Callback<File>() {
                @Override
                public void onComplete(File result) {
                    callback.onComplete(result);
                }

                @Override
                public void onError(Exception e) {
                    if (dropboxService.isAvailable()) {
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

    private void downloadTempFile(ItemFile file, final Callback<File> callback) {
        final String path = getFilePath(file);
        final String toFile = "tmp_" + file.getFilename();
        if (meoCloudService.isAvailable()) {
            // dropbox will serve as a fallback
            meoCloudService.downloadTempFile(path, toFile, new Callback<File>() {
                @Override
                public void onComplete(File result) {
                    callback.onComplete(result);
                }

                @Override
                public void onError(Exception e) {
                    if (dropboxService.isAvailable()) {
                        dropboxService.downloadTempFile(path, toFile, callback);
                    }
                }
            });
        } else if (dropboxService.isAvailable()) {
            dropboxService.downloadTempFile(path, toFile, callback);
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
                    if (dropboxService.isAvailable()) {
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
        if (localThumb.exists()) {
            localThumb.delete();
        }
    }

    public List<ItemFile> getPendingFiles() {
        return pendingFiles;
    }

    // in some cases we don't care about errors, ie. downloading thumbnails
    public interface BaseCallback<I> {

        void onComplete(I result);

    }

    public interface Callback<I> extends BaseCallback<I> {

        void onError(Exception e);
    }
}
