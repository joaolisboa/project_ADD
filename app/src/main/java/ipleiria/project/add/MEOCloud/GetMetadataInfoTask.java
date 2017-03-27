package ipleiria.project.add.MEOCloud;

import android.content.Context;
import android.os.AsyncTask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ipleiria.project.add.MEOCloud.Data.File;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.Metadata;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingFilePathException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingParametersException;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 27-Mar-17.
 */

public class GetMetadataInfoTask extends AsyncTask<String, Void, MEOCloudResponse<Metadata>> {

    private final GetMetadataInfoTask.Callback callback;
    private Exception exception;

    public interface Callback {
        void onComplete(MEOCloudResponse<Metadata> result);
        void onError(Exception e);
    }

    public GetMetadataInfoTask(GetMetadataInfoTask.Callback callback) {
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
            if(params == null){
                throw new MissingParametersException();
            }else if(params[0] == null || params[0].isEmpty()){
                throw new MissingAccessTokenException();
            }if(params[1] == null || params[1].isEmpty()){
                throw new MissingFilePathException();
            }

            if (params[1].startsWith("/")) {
                params[1] = params[1].substring(1);
            }

            String path = MEOCloudAPI.API_METHOD_METADATA + "/" + MEOCloudAPI.API_MODE + "/" + params[1];
            System.out.println(path);

            Response response = HttpRequestor.get(params[0], path, null);
            if (response != null) {
                MEOCloudResponse<Metadata> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    String responseBody = response.body().string();
                    Metadata metadata = Metadata.fromJson(responseBody, Metadata.class);
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