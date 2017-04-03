package ipleiria.project.add.MEOCloud.Tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import ipleiria.project.add.MEOCloud.Data.FileResponse;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingFilePathException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingParametersException;
import ipleiria.project.add.MEOCloud.HttpRequestor;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public class MEODownloadFile extends AsyncTask<String, Void, MEOCloudResponse<FileResponse>> {

    private final MEOCallback<FileResponse> callback;
    private Exception exception;
    private Context context;

    public MEODownloadFile(Context context, MEOCallback<FileResponse> callback) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<FileResponse> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            if(result.responseSuccessful()){
                callback.onComplete(result);
            }else{
                callback.onRequestError(new HttpErrorException(result.getError()));
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

            HashMap<String, String> map = new HashMap<>();
            System.out.println(params.length);
            if(params.length > 1 && params[1] != null) {
                map.put("rev", params[1]);
            }

            String path = MEOCloudAPI.API_METHOD_FILES + "/" + MEOCloudAPI.API_MODE + "/" + filePath;
            System.out.println(path);

            Response response = HttpRequestor.getContent(token, path, map);
            if (response != null) {
                MEOCloudResponse<FileResponse> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    InputStream is = response.body().byteStream();
                    FileOutputStream fos = context.openFileOutput(filePath, Context.MODE_PRIVATE);
                    byte[] buffer = new byte[1024 * 100];
                    int nBytes;
                    while((nBytes = is.read(buffer)) != -1){
                        fos.write(buffer, 0, nBytes);
                        fos.flush();
                    }
                    is.close();
                    fos.close();
                    String downloadPath = context.getFilesDir().getAbsolutePath() + "/" + filePath;
                    meoCloudResponse.setResponse(new FileResponse(downloadPath));
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