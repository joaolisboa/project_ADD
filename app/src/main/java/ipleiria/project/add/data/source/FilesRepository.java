package ipleiria.project.add.data.source;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.meocloud.data.MEOMetadata;
import ipleiria.project.add.utils.NetworkState;
import ipleiria.project.add.utils.PathUtils;
import ipleiria.project.add.utils.UriHelper;

import static ipleiria.project.add.data.model.PendingFile.BOTH;
import static ipleiria.project.add.data.model.PendingFile.DROPBOX;
import static ipleiria.project.add.data.model.PendingFile.EMAIL;
import static ipleiria.project.add.data.model.PendingFile.MEO_CLOUD;
import static ipleiria.project.add.utils.FileUtils.DOC_FILENAME;
import static ipleiria.project.add.utils.FileUtils.SHEET_FILENAME;

/**
 * Created by Lisboa on 06-May-17.
 */

public class FilesRepository implements FilesDataSource {

    private static final String TAG = "FILES_REPO";

    private static final String APP_DIR = Application.getAppContext().getFilesDir().getAbsolutePath();
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

    // method returns true if files where exported to cloud services(user is onlines with services connected)
    public boolean exportFiles(String username){
        String sheetFilename = "Ficha de autoavaliação_Grelha_" + currentPeriod + "_" + username + ".xlsx";
        String docFilename = "Relatorio_" + currentPeriod + "_" + username + ".txt";

        Uri sheet = Uri.fromFile(new File(Application.getAppContext().getFilesDir(), SHEET_FILENAME));
        Uri doc = Uri.fromFile(new File(Application.getAppContext().getFilesDir(), DOC_FILENAME));

        if(NetworkState.isOnline()){
            if(meoCloudService.isAvailable() || dropboxService.isAvailable()){
                uploadFile(sheet, "exported", sheetFilename);
                uploadFile(doc, "exported", docFilename);
                // files have been uploaded
                return true;
            }
        }

        //if files are exported locally, create them here
        //... rename?

        //files can be exported locally
        return false;
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
        return "/" + currentPeriod.toStringPath() +
                "/" + criteria.getDimension().getReference() +
                "_" + criteria.getArea().getReference() +
                "_" + criteria.getReference();
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

            case BOTH:
            case MEO_CLOUD:
            case DROPBOX:
                downloadTempPendingFile(pendingFile.getFilename(), callback);
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
        final String path = PENDING_PATH + filename;
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

    private void downloadTempPendingFile(String filename, final Callback<File> callback){
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
        for(PendingFile file: files){
            if(!pendingFiles.contains(file)){
                pendingFiles.add(file);
            }
        }
    }

    public void addPendingFile(PendingFile file){
        if(!pendingFiles.contains(file)){
            pendingFiles.add(file);
        }
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
        pendingFiles = new LinkedList<>();
        if(meoCloudService.isAvailable()) {
            meoCloudService.getMetadata(PENDING_PATH, new Callback<MEOMetadata>() {
                @Override
                public void onComplete(MEOMetadata result) {
                    for(MEOMetadata meoMetadata: result.getContents()){
                        if(!meoMetadata.isDir()){
                            ItemFile newFile = new ItemFile(meoMetadata.getName());
                            PendingFile pendingFile = new PendingFile(newFile, MEO_CLOUD);
                            addPendingFile(pendingFile);
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
                            addPendingFile(pendingFile);
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
        // temp fix - in case both aren't available send generic error to stop loading progress in main
        if(!meoCloudService.isAvailable() && !dropboxService.isAvailable()){
            callback.onMEOError();
        }
    }

    @Override
    public void saveFile(final ItemFile newFile, final Uri uri) {
        if(NetworkState.isOnline()){
            if (meoCloudService.isAvailable()) {
                String relativePath = getRelativePath(newFile.getParent().getCriteria());
                meoCloudService.uploadFile(uri, relativePath.substring(1), newFile.getFilename());
            }
            if (dropboxService.isAvailable()) {
                // dropbox creates folders automatically so we don't need to seperate the path and filename
                dropboxService.uploadFile(uri, getFilePath(newFile), null);
            }
        }
        if (!meoCloudService.isAvailable() && !dropboxService.isAvailable()) {
            saveLocalFile(newFile, uri);
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

    private void saveLocalFile(ItemFile file, Uri src) {
        Context appContext = Application.getAppContext();
        String filePath = appContext.getFilesDir().getAbsolutePath().concat(getFilePath(file));
        File dir = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        System.out.println(dir.getAbsolutePath());
        File destFile = new File(filePath);
        System.out.println(destFile.getAbsolutePath());
        try {
            InputStream is = appContext.getContentResolver().openInputStream(src);
            FileOutputStream outStream = new FileOutputStream(destFile);

            int nRead;
            byte[] data = new byte[16384];

            if (is != null) {
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    outStream.write(data, 0, nRead);
                }
            }
            outStream.close();
            is.close();
        } catch (IOException e) {
            Log.e("LOCAL_FILE_COPY_OFFLIME", e.getMessage(), e);
        }
    }

    private File getLocalPendingFile(String filename){
        return new File(APP_DIR, filename);
    }

    private File getCachedThumbnail(String filename) {
        return new File(Application.getAppContext().getCacheDir().getAbsolutePath().concat(THUMBNAIL_PREFIX).concat(filename));
    }

    private File getLocalFile(ItemFile file) {
        return new File(APP_DIR.concat(getFilePath(file)));
    }

    private File getLocalFile(ItemFile file, Criteria criteria){
        return new File(APP_DIR.concat(getFilePath(file, criteria)));
    }

    private File getLocalFile(ItemFile file, String filename){
        return new File(APP_DIR.concat(getFilePath(file, filename)));
    }

    private File getLocalFile(ItemFile file, boolean deleted){
        return new File(APP_DIR.concat(getFilePath(file, deleted)));
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

    private void deleteLocalFile(ItemFile file){
        File f = getLocalFile(file);
        if(f.exists()){
            f.delete();
        }
    }

    @Override
    public File getCachedThumbnail(ItemFile file) {
        return new File(Application.getAppContext().getCacheDir().getAbsolutePath()
                .concat(THUMBNAIL_PREFIX)
                .concat(file.getFilename()));
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
        System.out.println(localFile.getAbsolutePath());
        if (localFile.exists()) {
            callback.onComplete(localFile);
        } else if (NetworkState.isOnline()) {
            downloadTempFile(file, callback);
        }else{
            callback.onError(new Exception("User is offline - no local file found"));
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
        if(!NetworkState.isOnline() || !(meoCloudService.isAvailable() && dropboxService.isAvailable())){
            File localFile = getLocalFile(file);
            if (localFile.exists()) {
                File to = getLocalFile(file, newCriteria);
                renameLocalFile(localFile, to);
            }
        }else{
            String from = getFilePath(file);
            String to = getFilePath(file, newCriteria);
            if (meoCloudService.isAvailable()) {
                meoCloudService.moveFile(from, to);
            }
            if (dropboxService.isAvailable()) {
                dropboxService.moveFile(from, to);
            }
        }
    }

    public void movePendingFile(final PendingFile pendingFile, Item item, Criteria criteria){
        //pendingFiles.remove(pendingFile);
        final String from = PENDING_PATH + pendingFile.getFilename();
        final String to = getFilePath(pendingFile.getItemFile(), criteria);
        /*if(pendingFiles.contains(pendingFile)){
            item.addFile(pendingFile.getItemFile());
        }*/

        downloadPendingFile(pendingFile.getFilename(), new Callback<File>() {
            @Override
            public void onComplete(File result) {
                System.out.println(pendingFile.getProvider());
                if(pendingFile.getProvider().equals(MEO_CLOUD) || pendingFile.getProvider().equals(BOTH)) {
                    meoCloudService.moveFile(from, to);
                    dropboxService.uploadFile(Uri.fromFile(result), getFilePath(pendingFile.getItemFile()), null);
                }else if(pendingFile.getProvider().equals(DROPBOX) || pendingFile.getProvider().equals(BOTH)) {
                    System.out.println("uploading to meo");
                    if (meoCloudService.isAvailable()) {
                        String relativePath = getRelativePath(pendingFile.getItemFile().getParent().getCriteria());
                        meoCloudService.uploadFile(Uri.fromFile(result),
                                relativePath.substring(1), pendingFile.getFilename());
                    }
                    System.out.println("dropbox move");
                    dropboxService.moveFile(from, to);
                }
            }

            @Override
            public void onError(Exception e) {}
        });
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
        } else {
            callback.onError(new Exception("User is online but no service is connected"));
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
        File fromFile = getLocalFile(file, false);
        if(fromFile.exists()){
            File toFile = getLocalFile(file, true);
            renameLocalFile(fromFile, toFile);
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
        File fromFile = getLocalFile(file, true);
        if (fromFile.exists()) {
            File toFile = getLocalFile(file, false);
            renameLocalFile(fromFile, toFile);
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
        renameLocalFile(getLocalFile(file, oldFilename), getLocalFile(file, newFilename));
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
        deleteLocalFile(file);
    }

    public List<PendingFile> getPendingFiles() {
        List<PendingFile> files = new LinkedList<>();
        for(PendingFile file: pendingFiles){
            if(file.getItemFile().getParent() == null) {
                files.add(file);
            }
        }
        return files;
    }

    public void removePendingFiles(List<PendingFile> selectedPendingFiles) {
        pendingFiles.removeAll(selectedPendingFiles);
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
