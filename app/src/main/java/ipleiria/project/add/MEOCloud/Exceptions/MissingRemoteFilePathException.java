package ipleiria.project.add.MEOCloud.Exceptions;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public class MissingRemoteFilePathException extends Exception {
    public MissingRemoteFilePathException(){
        super("Missing file path/name for remote server");
    }
}
