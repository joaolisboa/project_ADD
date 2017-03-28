package ipleiria.project.add.MEOCloud;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;

import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.Metadata;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingFilePathException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingParametersException;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 28-Mar-17.
 */

public class DisableAccessTokenTask extends AsyncTask<String, Void, MEOCloudResponse<Void>> {

    private final DisableAccessTokenTask.Callback callback;
    private Exception exception;

    public interface Callback {
        void onComplete(MEOCloudResponse<Void> result);
        void onError(Exception e);
    }

    public DisableAccessTokenTask(DisableAccessTokenTask.Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<Void> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            callback.onComplete(result);
        }
    }

    @Override
    protected MEOCloudResponse<Void> doInBackground(String... params) {
        try {
            if(params == null){
                throw new MissingParametersException();
            }else if(params[0] == null || params[0].isEmpty()){
                throw new MissingAccessTokenException();
            }

            Response response = HttpRequestor.get(params[0], MEOCloudAPI.API_METHOD_DISABLE_TOKEN, null);
            if (response != null) {
                MEOCloudResponse<Void> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                return meoCloudResponse;
            }
            return null;
        } catch (MissingParametersException
                | MissingAccessTokenException e) {
            exception = e;
        }
        return null;
    }
}