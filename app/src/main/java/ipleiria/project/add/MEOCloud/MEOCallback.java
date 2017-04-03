package ipleiria.project.add.MEOCloud;

import ipleiria.project.add.MEOCloud.Data.FileResponse;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;

/**
 * Created by Lisboa on 30-Mar-17.
 */

public interface MEOCallback<I> {

    // http request was successful - HTTPCode = 200
    void onComplete(MEOCloudResponse<I> result);
    // http request failed - HTTPCode != 200
    // sends HTTPErrorException with exception message with corresponding http code error
    void onRequestError(HttpErrorException httpE);
    // failed to make request, could be missing parameters, access token
    // or specific exception to the method
    void onError(Exception e);

}
