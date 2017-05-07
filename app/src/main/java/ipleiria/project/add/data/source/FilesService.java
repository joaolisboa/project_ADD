package ipleiria.project.add.data.source;

import android.support.annotation.NonNull;

/**
 * Created by Lisboa on 06-May-17.
 */

public interface FilesService<I> {

    void init(@NonNull String token);

    void revokeToken(I callback);

    void uploadFile(I callback);

    void downloadFile(I callback);

    void moveFile(I callback);

    void deleteFile();

    void downloadThumbnail(I callback);

}
