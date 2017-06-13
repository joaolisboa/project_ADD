package ipleiria.project.add.data.source;

import android.app.ApplicationErrorReport;
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
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.meocloud.data.MEOMetadata;
import ipleiria.project.add.utils.NetworkState;

import static ipleiria.project.add.data.model.PendingFile.DROPBOX;
import static ipleiria.project.add.data.model.PendingFile.EMAIL;
import static ipleiria.project.add.data.model.PendingFile.MEO_CLOUD;

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
    private List<PendingFile> pendingFiles;

    private EvaluationPeriod currentPeriod;

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
                    //categoryRepository.readCriteria();
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

    public void setCurrentPeriod(EvaluationPeriod currentPeriod) {
        this.currentPeriod = currentPeriod;
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentPeriod.getStartDate());
        String startYear = String.valueOf(calendar.get(Calendar.YEAR));
        calendar.setTime(currentPeriod.getEndDate());
        String endYear = String.valueOf(calendar.get(Calendar.YEAR));
        return "/" + startYear + "_" + endYear +
                "/" + criteria.getDimension().getReference() +
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

    public void getPendingFile(PendingFile pendingFile, Callback<File> callback){
        switch(pendingFile.getProvider()){

            case MEO_CLOUD:
            case DROPBOX:
                downloadPendingFile(pendingFile.getFilename(), callback);
                break;

            case EMAIL:
                File email = new File(Application.getAppContext().getFilesDir(), pendingFile.getFilename());
                callback.onComplete(email);
                break;

        }
    }

    public void getPendingFileThumbnail(String filename, final BaseCallback<File> callback){
        File localThumb = getCachedThumbnail(filename);
        if (localThumb.exists()) {
            callback.onComplete(localThumb);
        } else {
            downloadPendingThumbnail(filename, callback);
        }
    }

    private void downloadPendingThumbnail(String filename, final BaseCallback<File> callback){
        final String filePath = PENDING_PATH + filename;

        if (meoCloudService.isAvailable()) {
            // meo cloud is preferred since it can create thumbnails for special files(like pdf)
            // dropbox will serve as a fallback
            meoCloudService.downloadThumbnail(filePath, new Callback<File>() {
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

        if (meoCloudService.isAvailable()) {
            // meo cloud is preferred since it can create thumbnails for special files(like pdf)
            // dropbox will serve as a fallback
            meoCloudService.downloadThumbnail(filePath, new Callback<File>() {
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

    private void downloadPendingFile(String filename, final Callback<File> callback){
        /*final String path = PENDING_PATH + filename;
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
        }*/

        final String path = PENDING_PATH + filename;
        final String toFile = "tmp_" + filename;
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

    @Override
    public String getRelativePath(File file) {
        int start = Application.getAppContext().getFilesDir().getAbsolutePath().length();
        return file.getAbsolutePath().substring(start, file.getAbsolutePath().length());
    }

    @Override
    public void addPendingFiles(List<PendingFile> files) {
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
        pendingFiles.add(new PendingFile(new ItemFile(filename), EMAIL));
    }

    @Override
    public void getRemotePendingFiles(final ServiceCallback<List<PendingFile>> callback) {
        if(meoCloudService.isAvailable()) {
            meoCloudService.getMetadata(PENDING_PATH, new Callback<MEOMetadata>() {
                @Override
                public void onComplete(MEOMetadata result) {
                    for(MEOMetadata meoMetadata: result.getContents()){
                        if(!meoMetadata.isDir()){
                            ItemFile newFile = new ItemFile(meoMetadata.getName());
                            PendingFile pendingFile = new PendingFile(newFile, MEO_CLOUD);
                            if(!pendingFiles.contains(pendingFile)) {
                                pendingFiles.add(pendingFile);
                            }
                        }
                    }
                    callback.onMEOComplete(pendingFiles);
                }

                @Override
                public void onError(Exception e) {
                    callback.onMEOError();
                }
            });
        }
        if(dropboxService.isAvailable()){
            dropboxService.getMetadata(PENDING_PATH, new Callback<ListFolderResult>() {
                @Override
                public void onComplete(ListFolderResult result) {
                    for(Metadata metadata: result.getEntries()){
                        if(metadata instanceof FileMetadata) {
                            ItemFile newFile = new ItemFile(metadata.getName());
                            PendingFile pendingFile = new PendingFile(newFile, DROPBOX);
                            if(!pendingFiles.contains(pendingFile)) {
                                pendingFiles.add(pendingFile);
                            }
                        }
                    }
                    callback.onDropboxComplete(pendingFiles);
                }

                @Override
                public void onError(Exception e) {
                    callback.onDropboxError();
                }
            });
        }
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

    @Override
    public void uploadFile(Uri uri, String path, String filename){
        if (meoCloudService.isAvailable()) {
            meoCloudService.uploadFile(uri, path, filename);
        }
        if (dropboxService.isAvailable()) {
            // dropbox creates folders automatically so we don't need to seperate the path and filename
            dropboxService.uploadFile(uri, path + "/" + filename, null);
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
            meoCloudService.downloadThumbnail(filePath, new Callback<File>() {
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

    public List<PendingFile> getPendingFiles() {
        return pendingFiles;
    }

    // in some cases we don't care about errors, ie. downloading thumbnails
    public interface BaseCallback<I> {

        void onComplete(I result);

    }

    public interface Callback<I> extends BaseCallback<I> {

        void onError(Exception e);
    }

    public interface ServiceCallback<I>{

        void onMEOComplete(I result);

        void onMEOError();

        void onDropboxComplete(I result);

        void onDropboxError();

    }
}
