package ipleiria.project.add.meocloud.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import ipleiria.project.add.Application;
import ipleiria.project.add.meocloud.HttpRequestor;
import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudAPI;
import ipleiria.project.add.meocloud.MEOCloudClient;
import ipleiria.project.add.meocloud.data.FileResponse;
import ipleiria.project.add.meocloud.data.MEOCloudResponse;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
import ipleiria.project.add.meocloud.exceptions.MissingAccessTokenException;
import ipleiria.project.add.meocloud.exceptions.MissingFilePathException;
import ipleiria.project.add.meocloud.exceptions.MissingParametersException;
import ipleiria.project.add.utils.HttpStatus;
import ipleiria.project.add.utils.PathUtils;
import okhttp3.Response;

/**
 * Created by Lisboa on 13-May-17.
 */

public class MEODownloadTemp extends AsyncTask<String, Void, MEOCloudResponse<FileResponse>> {

    private final MEOCallback<FileResponse> callback;
    private Exception exception;

    public MEODownloadTemp(MEOCallback<FileResponse> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<FileResponse> result) {
        super.onPostExecute(result);
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
    protected MEOCloudResponse<FileResponse> doInBackground(String... params) {
        try {
            if(params == null){
                throw new MissingParametersException();
            }else if(params[0] == null || params[0].isEmpty()){
                throw new MissingFilePathException();
            }

            if (params[0].startsWith("/")) {
                params[0] = params[0].substring(1);
            }

            String token = MEOCloudClient.getAccessToken();
            String filePath = params[0];
            String filename = PathUtils.filename(filePath);

            HashMap<String, String> map = new HashMap<>();
            if(params.length > 1 && params[1] != null) {
                map.put("rev", params[1]);
            }

            String path = MEOCloudAPI.API_METHOD_FILES + "/" + MEOCloudAPI.API_MODE + "/" + filePath;

            Response response = HttpRequestor.getContent(token, path, map);
            if (response != null) {
                MEOCloudResponse<FileResponse> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    InputStream is = response.body().byteStream();

                    File tmp = new File(Application.getAppContext().getFilesDir(), filename);
                    FileOutputStream fos = new FileOutputStream(tmp);
                    byte[] buffer = new byte[1024 * 100];
                    int nBytes;
                    while((nBytes = is.read(buffer)) != -1){
                        fos.write(buffer, 0, nBytes);
                        fos.flush();
                    }
                    is.close();
                    fos.close();
                    meoCloudResponse.setResponse((FileResponse) tmp);
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