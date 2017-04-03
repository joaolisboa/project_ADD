package ipleiria.project.add.MEOCloud.Tasks;

import android.os.AsyncTask;

import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.HttpRequestor;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import okhttp3.Response;

/**
 * Created by Lisboa on 28-Mar-17.
 */

public class MEODisableToken extends AsyncTask<String, Void, MEOCloudResponse<Void>> {

    private final MEOCallback<Void> callback;
    private Exception exception;

    public MEODisableToken(MEOCallback<Void> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<Void> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            if(result.responseSuccessful()){
                callback.onComplete(result);
            }else{
                callback.onRequestError(new HttpErrorException(result.getError()));
            }
        }
    }

    @Override
    protected MEOCloudResponse<Void> doInBackground(String... params) {
        try {

            Response response = HttpRequestor.get(MEOCloudClient.getAccessToken(),
                                    MEOCloudAPI.API_METHOD_DISABLE_TOKEN, null);
            if (response != null) {
                MEOCloudResponse<Void> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                return meoCloudResponse;
            }
            return null;
        } catch (MissingAccessTokenException e) {
            exception = e;
        }
        return null;
    }
}