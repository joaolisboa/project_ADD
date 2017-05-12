package ipleiria.project.add.data.source;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by Lisboa on 06-May-17.
 */

public interface AccountService<I> {

    void init(@NonNull String token);

    boolean isAvailable();

    void revokeToken(I callback);

}
