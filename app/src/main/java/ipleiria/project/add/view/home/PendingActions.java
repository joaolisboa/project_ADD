package ipleiria.project.add.view.home;

import android.view.View;

import ipleiria.project.add.data.model.PendingFile;

/**
 * Created by Lisboa on 08-Dec-17.
 */

public interface PendingActions {

    void onFileClick(PendingFile file);

    void onLongFileClick(PendingFile file);

    void onFileDelete(PendingFile file);

}
