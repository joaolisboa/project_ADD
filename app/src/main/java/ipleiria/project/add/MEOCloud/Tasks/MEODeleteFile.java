package ipleiria.project.add.MEOCloud.tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;

import ipleiria.project.add.MEOCloud.data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.data.MEOMetadata;
import ipleiria.project.add.MEOCloud.exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.exceptions.MissingFilePathException;
import ipleiria.project.add.MEOCloud.exceptions.MissingParametersException;
import ipleiria.project.add.MEOCloud.HttpRequestor;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by J on 28/03/2017.
 */

public class MEODeleteFile extends AsyncTask<String, Void, MEOCloudResponse<MEOMetadata>> {

    private final MEOCallback<MEOMetadata> callback;
    private Exception exception;

    public MEODeleteFile(MEOCallback<MEOMetadata> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<MEOMetadata> result) {
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
    protected MEOCloudResponse<MEOMetadata> doInBackground(String... params) {
        try {
            if (params == null) {
                throw new MissingParametersException();
            } else if (params[0] == null || params[0].isEmpty()) {
                throw new MissingFilePathException();
            }

            if (params[0].startsWith("/")) {
                params[0] = params[0].substring(1);
            }

            String token = MEOCloudClient.getAccessToken();
            String remoteFilePath = params[0];

            HashMap<String, String> bodyMap = new HashMap<>();
            bodyMap.put("root", MEOCloudAPI.API_MODE);
            bodyMap.put("path", "/" + remoteFilePath);

            Response response = HttpRequestor.post(token, MEOCloudAPI.API_METHOD_DELETE, null, bodyMap);
            if (response != null) {
                MEOCloudResponse<MEOMetadata> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    MEOMetadata searchResults = MEOMetadata.fromJson(response.body().string(), MEOMetadata.class);
                    meoCloudResponse.setResponse(searchResults);
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