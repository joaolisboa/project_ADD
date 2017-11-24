package ipleiria.project.add.view.home;

import java.util.List;

import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.view.base.ControllerView;

/**
 * Created by Lisboa on 24-Oct-17.
 */

public interface HomeView extends ControllerView {

    void showPendingFiles(List<PendingFile> pendingFiles);

    void showNoPendingFiles();

    void showLoadingIndicator();
}
