package ipleiria.project.add.MEOCloud;

import ipleiria.project.add.MEOCloud.Data.FileResponse;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;

/**
 * Created by Lisboa on 30-Mar-17.
 */

public interface MEOCallback<I> {

    void onComplete(MEOCloudResponse<I> result);
    void onRequestError(HttpErrorException httpE);
    void onError(Exception e);

}
