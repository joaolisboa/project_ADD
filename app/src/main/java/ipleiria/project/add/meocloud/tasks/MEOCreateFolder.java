package ipleiria.project.add.meocloud.tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;

import ipleiria.project.add.meocloud.data.MEOCloudResponse;
import ipleiria.project.add.meocloud.data.MEOMetadata;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
import ipleiria.project.add.meocloud.exceptions.MissingAccessTokenException;
import ipleiria.project.add.meocloud.exceptions.MissingParametersException;
import ipleiria.project.add.meocloud.exceptions.MissingRemoteFilePathException;
import ipleiria.project.add.meocloud.HttpRequestor;
import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudAPI;
import ipleiria.project.add.meocloud.MEOCloudClient;
import ipleiria.project.add.utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 20-Apr-17.
 */

public class MEOCreateFolder extends AsyncTask<String, Void, MEOCloudResponse<MEOMetadata>> {

    private final MEOCallback<MEOMetadata> callback;
    private Exception exception;

    public MEOCreateFolder(MEOCallback<MEOMetadata> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<MEOMetadata> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            if(result.responseSuccessful() || result.getCode() == HttpStatus.FORBIDDEN){
                /*FORBIDDEN = 403 - folder already exists - not a real error*/
                callback.onComplete(result.getResponse());
            }else{
                callback.onRequestError(new HttpErrorException(result));
            }
        }
    }

    @Override
    protected MEOCloudResponse<MEOMetadata> doInBackground(String... params) {
        try {
            if(params == null){
                throw new MissingParametersException();
            }else if(params[0] == null || params[0].isEmpty()){
                throw new MissingRemoteFilePathException();
            }

            if (params[0].startsWith("/")) {
                params[0] = params[0].substring(1);
            }



            String token = MEOCloudClient.getAccessToken();

            HashMap<String, String> bodyMap = new HashMap<>();
            bodyMap.put("root", MEOCloudAPI.API_MODE);
            bodyMap.put("path", "/" + params[0]);

            Response response = HttpRequestor.post(token, MEOCloudAPI.API_METHOD_CREATE_FOLDER, null, bodyMap);
            if (response != null) {
                MEOCloudResponse<MEOMetadata> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    String responseBody = response.body().string();
                    MEOMetadata metadata = MEOMetadata.fromJson(responseBody, MEOMetadata.class);
                    meoCloudResponse.setResponse(metadata);
                }
                return meoCloudResponse;
            }
            return null;
        } catch (IOException
                | MissingParametersException
                | MissingAccessTokenException
                | MissingRemoteFilePathException e) {
            exception = e;
        }
        return null;
    }
}