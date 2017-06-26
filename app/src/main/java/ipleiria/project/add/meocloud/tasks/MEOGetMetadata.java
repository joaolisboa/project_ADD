package ipleiria.project.add.meocloud.tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;

import ipleiria.project.add.meocloud.data.MEOCloudResponse;
import ipleiria.project.add.meocloud.data.MEOMetadata;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
import ipleiria.project.add.meocloud.exceptions.MissingAccessTokenException;
import ipleiria.project.add.meocloud.exceptions.MissingFilePathException;
import ipleiria.project.add.meocloud.exceptions.MissingParametersException;
import ipleiria.project.add.meocloud.HttpRequestor;
import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudAPI;
import ipleiria.project.add.meocloud.MEOCloudClient;
import ipleiria.project.add.utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 27-Mar-17.
 */

public class MEOGetMetadata extends AsyncTask<String, Void, MEOCloudResponse<MEOMetadata>> {

    private final MEOCallback<MEOMetadata> callback;
    private Exception exception;

    public MEOGetMetadata(MEOCallback<MEOMetadata> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<MEOMetadata> result) {
        super.onPostExecute(result);
        if(result == null){
            exception = new Exception("NULL RESPONSE - POSSIBLE TIMEOUT");
        }
        if (exception != null) {
            callback.onError(exception);
        } else {
            if(result.responseSuccessful()){
                callback.onComplete(result.getResponse());
            }else{
                callback.onRequestError(new HttpErrorException(result));
            }
        }
    }

    @Override
    protected MEOCloudResponse<MEOMetadata> doInBackground(String... params) {
        try {
            if (params == null) {
                throw new MissingParametersException();
            } else if (params[0] == null) {
                throw new MissingFilePathException();
            }

            if (params[0].startsWith("/")) {
                params[0] = params[0].substring(1);
            }

            String token = MEOCloudClient.getAccessToken();
            String remoteFilePath = params[0];

            HashMap<String, String> map = new HashMap<>();
            if (params.length > 1 && params[1] != null) {
                map.put("file_limit", params[1]);
            }
            if (params.length > 2 && params[2] != null) {
                map.put("hash", params[2]);
            }
            // list folder - default true
            if (params.length > 3 && params[3] != null) {
                map.put("list", params[3]);
            }
            if (params.length > 4 && params[4] != null) {
                map.put("include_deleted", params[4]);
            }
            if (params.length > 5 && params[5] != null) {
                map.put("rev", params[5]);
            }

            String path = MEOCloudAPI.API_METHOD_METADATA + "/" + MEOCloudAPI.API_MODE + "/" + remoteFilePath;

            Response response = HttpRequestor.get(token, path, map);
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
                | MissingFilePathException
                | MissingAccessTokenException e) {
            exception = e;
        }
        return null;
    }
}