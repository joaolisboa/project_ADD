package ipleiria.project.add.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOCreateFolderTree;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Dimension;
import ipleiria.project.add.Model.ItemFile;

import static ipleiria.project.add.Utils.PathUtils.TRASH_FOLDER;

/**
 * Created by Lisboa on 26-Apr-17.
 */

public class FileUtils {

    public static void copyFileToLocalDir(Context context, Uri src, Criteria criteria){
        String filename = UriHelper.getFileName(context, src);
        String path = PathUtils.getLocalFilePath(context, filename, criteria);
        File dir = new File(path.substring(0, path.lastIndexOf("/")));
        if(!dir.exists()){
            dir.mkdirs();
        }
        File destFile = new File(path);
        try {
            InputStream is = context.getContentResolver().openInputStream(src);
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
        }catch(IOException e){
            Log.e("LOCAL_FILE_COPY_OFFLIME", e.getMessage(), e);
        }
    }

    public static void deleteFile(String path){
        File file = new File(path);
        if(file.exists()){
            boolean success = file.delete();
            Log.d("FILE_ACTION", "file delete successful? " + success);
        }
    }

    public static void renameFile(String from, String to){
        File src = new File(from);
        if(src.exists()) {
            File dir = new File(to.substring(0, to.lastIndexOf("/")));
            if(!dir.exists()){
                dir.mkdirs();
            }
            File dest = new File(to);
            boolean success = src.renameTo(dest);
            Log.d("FILE_ACTION", "file rename successful: " + success);
        }
    }

    public static List<File> getLocalFiles(Context context){
        List<File> files = new LinkedList<>();
        for(Dimension dimension: ApplicationData.getInstance().getDimensions()){
            File dimensiondir = new File(context.getFilesDir().getAbsolutePath() + "/" + dimension.getReference());
            if(dimensiondir.isDirectory()){
                goThroughFolder(dimensiondir, files);
            }
        }
        System.out.println(files);
        return files;
    }

    private static void goThroughFolder(File dir, List<File> files){
        for(File fileInDir: dir.listFiles()){
            if(fileInDir.isDirectory()){
                goThroughFolder(fileInDir, files);
            }else{
                files.add(fileInDir);
            }
        }
    }

    public static File getUserThumbnail(Context context){
        return new File(context.getFilesDir() + "/user_thumb.jpg");
    }

    public static void moveFilesToNewDir(final Context context, List<ItemFile> files, Criteria oldCriteria) {
        if(NetworkState.isOnline(context)){
            for(ItemFile itemFile: files){
                final String newPath = PathUtils.getLocalFilePath(context, itemFile.getFilename(), itemFile.getParent().getCriteria());
                final String oldPath = PathUtils.getLocalFilePath(context, itemFile.getFilename(), oldCriteria);

                if(MEOCloudClient.isClientInitialized()) {
                    String[] splitPath = newPath.substring(1, newPath.lastIndexOf("/")).split("/");
                    String dimensionPath = splitPath[0];
                    String areaPath = splitPath[1];
                    String criteriaPath = splitPath[2];

                    new MEOCreateFolderTree(new MEOCallback<MEOMetadata>() {
                        @Override
                        public void onComplete(MEOMetadata result) {
                            CloudHandler.moveFileMEO(oldPath, newPath);
                        }

                        @Override
                        public void onRequestError(HttpErrorException httpE) {
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    }).execute(dimensionPath, areaPath, criteriaPath);
                }
                if(DropboxClientFactory.isClientInitialized()){
                    CloudHandler.moveFileMEO(oldPath, newPath);
                }
            }
        }else{
            for(ItemFile itemFile: files){
                renameFile(PathUtils.getLocalFilePath(context, itemFile.getFilename(), itemFile.getParent().getCriteria()),
                        PathUtils.getLocalFilePath(context, itemFile.getFilename(), itemFile.getParent().getCriteria()));
            }
        }
    }

    public static List<File> getLocalDeletedFiles(Context context) {
        List<File> files = new LinkedList<>();
        File trashDir = new File(context.getFilesDir() + TRASH_FOLDER);
        if(!trashDir.exists()){
            trashDir.mkdirs();
            if(trashDir.list() != null && trashDir.list().length == 0){
                Collections.addAll(files, trashDir.listFiles());
            }
        }
        System.out.println(files);
        return files;
    }
}
