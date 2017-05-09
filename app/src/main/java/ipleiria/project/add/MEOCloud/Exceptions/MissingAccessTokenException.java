package ipleiria.project.add.meocloud.exceptions;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public class MissingAccessTokenException extends Exception {

    public MissingAccessTokenException(){
        super("Access Token missing as the first parameter");
    }

}
