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

public class DeleteFileTask extends AsyncTask<String, Void, MEOCloudResponse<Metadata>> {

    private final DeleteFileTask.Callback callback;
    private Exception exception;

    public interface Callback {
        void onComplete(MEOCloudResponse<Metadata> result);

        void onError(Exception e);
    }

    public DeleteFileTask(DeleteFileTask.Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<Metadata> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            callback.onComplete(result);
        }
    }

    @Override
    protected MEOCloudResponse<Metadata> doInBackground(String... params) {
        try {
            if (params == null) {
                throw new MissingParametersException();
            } else if (params[0] == null || params[0].isEmpty()) {
                throw new MissingAccessTokenException();
            }else if (params[1] == null || params[1].isEmpty()) {
                throw new MissingFilePathException();
            }

            String token = params[0];
            String remoteFilePath = params[1];

            HashMap<String, String> bodyMap = new HashMap<>();
            bodyMap.put("root", MEOCloudAPI.API_MODE);
            bodyMap.put("path", remoteFilePath);

            String path = MEOCloudAPI.API_METHOD_DELETE;

            Response response = HttpRequestor.post(token, path, null, bodyMap);
            if (response != null) {
                MEOCloudResponse<Metadata> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    Metadata searchResults = Metadata.fromJson(response.body().string(), Metadata.class);
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