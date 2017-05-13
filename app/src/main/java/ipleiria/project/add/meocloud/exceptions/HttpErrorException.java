package ipleiria.project.add.meocloud.exceptions;

import ipleiria.project.add.meocloud.data.MEOCloudResponse;

/**
 * Created by Lisboa on 30-Mar-17.
 */

public class HttpErrorException extends Exception {

    private int errorCode;

    public HttpErrorException(MEOCloudResponse response){
        super(response.getError());
        this.errorCode = response.getCode();
    }

    public int getErrorCode(){
        return errorCode;
    }
}
