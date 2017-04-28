package ipleiria.project.add.Utils;


import android.content.Context;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;

/**
 * Created by Lisboa on 19-Apr-17.
 */

public class PathUtils {

    public final static String TRASH_FOLDER = "/trash";

    public static String getRemoteFilePath(ItemFile file) {
        Criteria criteria = file.getParent().getCriteria();
        return "/" + criteria.getDimension().getReference() +
                "/" + criteria.getArea().getReference() +
                "/" + criteria.getReference() +
                "/" + file.getFilename();
    }

    public static String getRemotePath(Criteria criteria){
        return "/" + criteria.getDimension().getReference() +
                "/" + criteria.getArea().getReference() +
                "/" + criteria.getReference();
    }

    public static String getRemoteFilePath(String file, Criteria criteria){
        return "/" + criteria.getDimension().getReference() +
                "/" + criteria.getArea().getReference() +
                "/" + criteria.getReference() +
                "/" + file;
    }

    public static String getLocalRelativePath(Context context, File file){
        return file.getAbsolutePath().substring(context.getFilesDir().getAbsolutePath().length(), file.getAbsolutePath().length());
    }

    public static String getLocalFilePath(Context context, String filename, Criteria criteria){
        return context.getFilesDir().getAbsolutePath() + getRemoteFilePath(filename, criteria);
    }

    public static String getLocalTrashPath(Context context, String filename){
        return context.getFilesDir().getAbsolutePath() + TRASH_FOLDER + "/" + filename;
    }

    public static String getLocalFilePath(Context context, String filename){
        return context.getFilesDir().getAbsolutePath() + "/" + filename;
    }
    public static String getThumbFilename(Context context, String filename){
        return context.getFilesDir().getAbsolutePath() + "/thumb_" + filename;
    }

    public static String folderPath(String remotePath){
        return remotePath.substring(0, remotePath.lastIndexOf("/"));
    }

    public static String filename(String path){
        return path.substring(path.lastIndexOf("/")+1, path.length());
    }

    public static List<String> getRemoteFilePaths(Item item){
        List<String> paths = new LinkedList<>();
        for(ItemFile file: item.getFiles()){
            paths.add(getRemoteFilePath(file));
        }
        return paths;
    }

    public static String trashPath(ItemFile item){
        return TRASH_FOLDER + "/" + item.getFilename();
    }

}
