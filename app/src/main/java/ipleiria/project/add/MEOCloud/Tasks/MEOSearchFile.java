package ipleiria.project.add.MEOCloud.Tasks;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Exceptions.InvalidQuerySizeException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingFilePathException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingParametersException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingSearchParameter;
import ipleiria.project.add.MEOCloud.HttpRequestor;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by J on 28/03/2017.
 */

public class MEOSearchFile extends AsyncTask<String, Void, MEOCloudResponse<List<MEOMetadata>>> {

    private final MEOCallback<List<MEOMetadata>> callback;
    private Exception exception;

    public MEOSearchFile(MEOCallback<List<MEOMetadata>> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<List<MEOMetadata>> result) {
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
    protected MEOCloudResponse<List<MEOMetadata>> doInBackground(String... params) {
        try {
            if (params == null) {
                throw new MissingParametersException();
            }else if (params[0] == null || params[0].isEmpty()) {
                throw new MissingFilePathException();
            }else if (params[1] == null || params[1].isEmpty()){
                throw new MissingSearchParameter();
            }

            if (params[0].startsWith("/")) {
                params[0] = params[0].substring(1);
            }

            String token = MEOCloudClient.getAccessToken();
            String remoteFilePath = params[0];

            HashMap<String, String> map = new HashMap<>();
            if (params.length > 1 && params[1] != null) {
                map.put("query", params[1]);
                if(params[1].length() < 3 || params[1].length() > 20){
                    throw new InvalidQuerySizeException();
                }
            }
            if (params.length > 2 && params[2] != null) {
                map.put("mime_type", params[2]);
            }
            if (params.length > 3 && params[3] != null) {
                map.put("file_limit", params[3]);
            }
            if (params.length > 4 && params[4] != null) {
                map.put("include_deleted", params[4]);
            }


            String path = MEOCloudAPI.API_METHOD_SEARCH + "/" + MEOCloudAPI.API_MODE + "/" + remoteFilePath;
            System.out.println(path);

            Response response = HttpRequestor.get(token, path, map);
            if (response != null) {
                MEOCloudResponse<List<MEOMetadata>> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    Type listOfLinkMetadata = new TypeToken<List<MEOMetadata>>(){}.getType();
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    List<MEOMetadata> searchResults = gson.fromJson(response.body().string(), listOfLinkMetadata);
                    meoCloudResponse.setResponse(searchResults);
                }
                return meoCloudResponse;
            }
            return null;
        } catch (IOException
                | MissingParametersException
                | MissingFilePathException
                | MissingAccessTokenException
                | InvalidQuerySizeException
                | MissingSearchParameter e) {
            exception = e;
        }
        return null;
    }
}