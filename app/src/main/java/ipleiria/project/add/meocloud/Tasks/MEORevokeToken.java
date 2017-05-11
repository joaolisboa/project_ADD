package ipleiria.project.add.meocloud.tasks;

import android.os.AsyncTask;

import ipleiria.project.add.meocloud.data.MEOCloudResponse;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
import ipleiria.project.add.meocloud.exceptions.MissingAccessTokenException;
import ipleiria.project.add.meocloud.HttpRequestor;
import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudAPI;
import ipleiria.project.add.meocloud.MEOCloudClient;
import okhttp3.Response;

/**
 * Created by Lisboa on 28-Mar-17.
 */

public class MEORevokeToken extends AsyncTask<String, Void, MEOCloudResponse<Void>> {

    private final MEOCallback<Void> callback;
    private Exception exception;

    public MEORevokeToken(MEOCallback<Void> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<Void> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            if(result.responseSuccessful()){
                callback.onComplete(result.getResponse());
            }else{
                callback.onRequestError(new HttpErrorException(result.getError()));
            }
        }
    }

    @Override
    protected MEOCloudResponse<Void> doInBackground(String... params) {
        try {
            Response response = HttpRequestor.post(MEOCloudClient.getAccessToken(), MEOCloudAPI.API_METHOD_DISABLE_TOKEN, null, new byte[0]);
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