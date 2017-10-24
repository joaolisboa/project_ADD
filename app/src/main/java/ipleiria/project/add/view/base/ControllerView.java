package ipleiria.project.add.view.base;

import com.hannesdorfmann.mosby3.mvp.MvpView;

/**
 * Substitute for Mosby.MvpView to include generic methods shared by all controller
 */

public interface ControllerView extends MvpView {

    void showErrorMessage(String message);

}
