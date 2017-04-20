package ipleiria.project.add.Utils;


import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;

/**
 * Created by Lisboa on 19-Apr-17.
 */

public class RemotePath {

    public final static String TRASH_FOLDER = "/trash";

    public static String getRemoteFilePath(ItemFile file, Criteria criteria){
        return "/" + criteria.getDimension().getReference() +
                "/" + criteria.getArea().getReference() +
                "/" + criteria.getReference() +
                "/" + file.getFilename();

    }

    public static String folderPath(String remotePath){
        return remotePath.substring(0, remotePath.lastIndexOf("/"));
    }

    public static List<String> getRemoteFilePaths(Item item){
        List<String> paths = new LinkedList<>();
        for(ItemFile file: item.getFiles()){
            paths.add(getRemoteFilePath(file, item.getCriteria()));
        }
        return paths;
    }

    public static String trashPath(ItemFile item){
        return TRASH_FOLDER + "/" + item.getFilename();
    }

}
