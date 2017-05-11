package ipleiria.project.add.meocloud;

import ipleiria.project.add.meocloud.exceptions.HttpErrorException;

/**
 * Created by Lisboa on 30-Mar-17.
 */

public interface MEOCallback<I> {

    // http request was successful - HTTPCode = 200
    void onComplete(I result);
    // http request failed - HTTPCode != 200
    // sends HTTPErrorException with exception message with corresponding http code error
    void onRequestError(HttpErrorException httpE);
    // failed to make request, could be missing parameters, access token
    // or specific exception to the method
    void onError(Exception e);

}
