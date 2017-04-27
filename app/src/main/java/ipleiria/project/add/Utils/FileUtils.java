package ipleiria.project.add.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Dimension;

/**
 * Created by Lisboa on 26-Apr-17.
 */

public class FileUtils {

    public static void copyFileToLocalDir(Context context, Uri src, Criteria criteria){
        String path = context.getFilesDir().getAbsolutePath() + PathUtils.getRemotePath(criteria);
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File destFile = new File(path + "/" + UriHelper.getFileName(context, src));
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
        File localFileThumb = new File(from);
        if(localFileThumb.exists()) {
            boolean success = localFileThumb.renameTo(new File(to));
            Log.d("FILE_ACTION", "file rename successful: " + success);
        }
    }

    public static List<File> getLocalFiles(Context context){
        List<File> files = new LinkedList<>();
        System.out.println("Dimensions: " + ApplicationData.getInstance().getDimensions());
        for(Dimension dimension: ApplicationData.getInstance().getDimensions()){
            File dimensiondir = new File(context.getFilesDir().getAbsolutePath() + "/" + dimension.getReference());
            System.out.println("Dimension path: " + dimensiondir.getPath());
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

}
