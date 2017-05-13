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

import static ipleiria.project.add.utils.PathUtils.TRASH_FOLDER;

/**
 * Created by Lisboa on 19-Apr-17.
 */

public class MEOMoveFile extends AsyncTask<String, Void, MEOCloudResponse<MEOMetadata>> {

    private final MEOCallback<MEOMetadata> callback;
    private Exception exception;

    public MEOMoveFile(MEOCallback<MEOMetadata> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<MEOMetadata> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            if (result.responseSuccessful()) {
                callback.onComplete(result.getResponse());
            } else {
                callback.onRequestError(new HttpErrorException(result));
            }
        }
    }

    @Override
    protected MEOCloudResponse<MEOMetadata> doInBackground(String... params) {
        try {
            if (params == null) {
                throw new MissingParametersException();
            } else if (params[0] == null || params[0].isEmpty()
                    || params[1] == null || params[1].isEmpty()) {
                throw new MissingFilePathException();
            }

            if (params[0].startsWith("/")) {
                params[0] = params[0].substring(1);
            }

            if (params[1].startsWith("/")) {
                params[1] = params[1].substring(1);
            }

            String token = MEOCloudClient.getAccessToken();
            String fromPath = params[0];
            String toPath = params[1];

            HashMap<String, String> bodyMap = new HashMap<>();
            bodyMap.put("root", MEOCloudAPI.API_MODE);
            bodyMap.put("from_path", "/" + fromPath);
            bodyMap.put("to_path", "/" + toPath);

            String trash = MEOCloudAPI.API_METHOD_METADATA + "/" + MEOCloudAPI.API_MODE + TRASH_FOLDER;

            // if destination file already exists with same name delete source file
            Response metaResponse = HttpRequestor.get(token, trash, null);
            if (metaResponse != null) {
                if (metaResponse.code() == HttpStatus.OK) {
                    String responseBody = metaResponse.body().string();
                    MEOMetadata metadata = MEOMetadata.fromJson(responseBody, MEOMetadata.class);
                    for (MEOMetadata fileInSourceDir : metadata.getContents()) {
                        String sourceFilename = fileInSourceDir.getPath();
                        if (sourceFilename.substring(sourceFilename.lastIndexOf("/") + 1, sourceFilename.length()).equals(
                                toPath.substring(toPath.lastIndexOf("/") + 1, toPath.length()))) {
                            HashMap<String, String> map = new HashMap<>();
                            bodyMap.put("root", MEOCloudAPI.API_MODE);
                            bodyMap.put("path", "/" + fromPath);
                            HttpRequestor.post(token, MEOCloudAPI.API_METHOD_DELETE, null, map);
                            return null;
                        }
                    }
                }
            }

            Response response = HttpRequestor.post(token, MEOCloudAPI.API_METHOD_MOVE, null, bodyMap);
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
