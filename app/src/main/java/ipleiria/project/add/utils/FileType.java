package ipleiria.project.add.utils;

/**
 * Created by Lisboa on 08-Dec-17.
 */

public class FileType {

    public static final int _FILE = 00000;
    public static final int EMAIL_FILE = 00001;

    public static int getFileType(String filename){
        if(filename.substring(filename.lastIndexOf(".") + 1).equals("eml")){
            return EMAIL_FILE;
        }

        return _FILE;
    }

}
