package ipleiria.project.add.meocloud.exceptions;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public class MissingFilePathException extends Exception {

    public MissingFilePathException(){
        super("Missing path, second parameter in the execute() call");
    }

}
