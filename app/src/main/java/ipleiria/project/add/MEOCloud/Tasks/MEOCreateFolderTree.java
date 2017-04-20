package ipleiria.project.add.MEOCloud.Tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;

import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingParametersException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingRemoteFilePathException;
import ipleiria.project.add.MEOCloud.HttpRequestor;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 20-Apr-17.
 */

public class MEOCreateFolderTree extends AsyncTask<String, Void, MEOCloudResponse<MEOMetadata>> {

    private final MEOCallback<MEOMetadata> callback;
    private Exception exception;

    public MEOCreateFolderTree(MEOCallback<MEOMetadata> callback) {
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
                callback.onRequestError(new HttpErrorException(result.getError()));
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

            String token = MEOCloudClient.getAccessToken();

            MEOCloudResponse<MEOMetadata> meoCloudResponse = new MEOCloudResponse<>();
            String fullPath = "";
            for(String path: params) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                fullPath += "/" + path;

                HashMap<String, String> bodyMap = new HashMap<>();
                bodyMap.put("root", MEOCloudAPI.API_MODE);
                bodyMap.put("path", fullPath);

                Response response = HttpRequestor.post(token, MEOCloudAPI.API_METHOD_CREATE_FOLDER, null, bodyMap);
                if (response != null) {
                    meoCloudResponse = new MEOCloudResponse<>();
                    meoCloudResponse.setCode(response.code());
                    if (response.code() == HttpStatus.OK || response.code() == HttpStatus.FORBIDDEN) {
                        String responseBody = response.body().string();
                        MEOMetadata metadata = MEOMetadata.fromJson(responseBody, MEOMetadata.class);
                        meoCloudResponse.setResponse(metadata);
                    }else {
                        return meoCloudResponse;
                    }
                }
            }
            return meoCloudResponse;
        } catch (IOException
                | MissingParametersException
                | MissingAccessTokenException
                | MissingRemoteFilePathException e) {
            exception = e;
        }
        return null;
    }
}