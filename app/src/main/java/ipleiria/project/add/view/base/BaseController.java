package ipleiria.project.add.view.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bluelinelabs.conductor.Controller;

import ipleiria.project.add.view.root.RootController;

/**
 * Base controller of which all controller will extend with helper methods
 */

public abstract class BaseController extends RefWatchingController {

    private static final String MOSBY_TAG = "MOSBY";

    // navigation drawer is always enabled unless specified. ie.AlbumDetailController
    private boolean navigationState = true;
    private boolean toolbarHideState = true;

    protected BaseController() {
    }

    protected BaseController(Bundle args) {
        super(args);
        setHasOptionsMenu(true);
    }

    protected BaseController(Bundle args, boolean navigationState) {
        this(args);
        this.navigationState = navigationState;
    }

    protected BaseController(Bundle args, boolean navigationState, boolean toolbarHideState) {
        this(args, navigationState);
        this.toolbarHideState = toolbarHideState;
    }

    // Note: This is just a quick demo of how an ActionBar *can* be accessed, not necessarily how it *should*
    // be accessed. In a production app, this would use Dagger instead.
    protected ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);
        setTitle();
    }

    private void setTitle() {
        Controller parentController = getParentController();
        while (parentController != null) {
            if (parentController instanceof BaseController && ((BaseController) parentController).getTitle() != null) {
                return;
            }
            parentController = parentController.getParentController();
        }

        String title = getTitle();
        ActionBar actionBar = getActionBar();
        if (title != null && actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    /**
     * Simplify setting a new controller as root
     * @param controller controller to be set as root
     * @param tag controller tag
     */
    public void setRoot(Controller controller, String tag){
        getRootController().setRoot(controller, tag);
    }

    /**
     * Show generic error message
     * @param message error message
     */
    public void showErrorMessage(String message){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Erro")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Easily get controller from child class to avoid constant
     * @return RootController
     */
    // only works if controller calling method isn't nested in another controller
    public RootController getRootController(){
        return (RootController) getParentController();
    }

    /**
     * Hides the keyboard
     * @param focusedView view with current focus
     */
    public void hideKeyboard(@Nullable View focusedView){
        // focused view can be null, in which case we'll find the view through the activity
        if(focusedView == null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = getActivity().getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(getActivity());
            }
            //close the keyboard
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }else{
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    /**
     * Opens keyboard
     * @param view focused view to open keyboard
     */
    public void openKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Easily fetch resource strings without calling for activity
     * @param id string resource id
     * @return resource string
     */
    public String getString(int id){
        return getActivity().getString(id);
    }

    /**
     * Easily fetch resource resources with placeholders
     * @param id string resource id
     * @param params object used for resource placeholders
     * @return resource string
     */
    public String getString(int id, Object... params){
        return getActivity().getString(id, params);
    }

    /**
     * Override method to set a title for the controller
     * @return Controller title to show in action bar
     */
    protected String getTitle(){
        return null;
    }

    public interface ActionBarProvider {
        ActionBar getSupportActionBar();
    }

}
