package ipleiria.project.add.MEOCloud;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.Metadata;
import ipleiria.project.add.MEOCloud.Exceptions.InvalidQuerySizeException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingFilePathException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingParametersException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingSearchParameter;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by J on 28/03/2017.
 */

public class SearchFileTask extends AsyncTask<String, Void, MEOCloudResponse<List<Metadata>>> {

    private final SearchFileTask.Callback callback;
    private Exception exception;

    public interface Callback {
        void onComplete(MEOCloudResponse<List<Metadata>> result);

        void onError(Exception e);
    }

    public SearchFileTask(SearchFileTask.Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<List<Metadata>> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            callback.onComplete(result);
        }
    }

    @Override
    protected MEOCloudResponse<List<Metadata>> doInBackground(String... params) {
        try {
            if (params == null) {
                throw new MissingParametersException();
            } else if (params[0] == null || params[0].isEmpty()) {
                throw new MissingAccessTokenException();
            }else if (params[1] == null || params[1].isEmpty()) {
                throw new MissingFilePathException();
            }else if (params[2] == null || params[2].isEmpty()){
                throw new MissingSearchParameter();
            }

            if (params[1].startsWith("/")) {
                params[1] = params[1].substring(1);
            }

            String token = params[0];
            String remoteFilePath = params[1];

            HashMap<String, String> map = new HashMap<>();
            if (params.length > 2 && params[2] != null) {
                map.put("query", params[2]);
                if(params[2].length() < 3 || params[2].length() > 20){
                    throw new InvalidQuerySizeException();
                }
            }
            if (params.length > 3 && params[3] != null) {
                map.put("mime_type", params[3]);
            }
            if (params.length > 4 && params[4] != null) {
                map.put("file_limit", params[4]);
            }
            if (params.length > 5 && params[5] != null) {
                map.put("include_deleted", params[5]);
            }


            String path = MEOCloudAPI.API_METHOD_SEARCH + "/" + MEOCloudAPI.API_MODE + "/" + remoteFilePath;
            System.out.println(path);

            Response response = HttpRequestor.get(token, path, map);
            if (response != null) {
                MEOCloudResponse<List<Metadata>> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    Type listOfLinkMetadata = new TypeToken<List<Metadata>>(){}.getType();
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    List<Metadata> searchResults = gson.fromJson(response.body().string(), listOfLinkMetadata);
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