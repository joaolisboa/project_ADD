package ipleiria.project.add.view.root;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;
import com.hannesdorfmann.mosby3.mvp.conductor.delegate.MvpConductorDelegateCallback;
import com.hannesdorfmann.mosby3.mvp.conductor.delegate.MvpConductorLifecycleListener;

import javax.inject.Inject;

import butterknife.BindView;
import ipleiria.project.add.Application;
import ipleiria.project.add.R;
import ipleiria.project.add.dagger.component.DaggerControllerComponent;
import ipleiria.project.add.dagger.module.PresenterModule;
import ipleiria.project.add.view.base.BaseController;
import ipleiria.project.add.view.home.HomeController;
import ipleiria.project.add.view.login.LoginController;
import ipleiria.project.add.view.setup.SetupActivity;

/**
 * Root controller where all controllers will be inserted into
 * this can provide universal actionbar and navigation drawer without reinstancing every time
 */

public class RootController extends BaseController implements NavigationView.OnNavigationItemSelectedListener,
            RootView, MvpConductorDelegateCallback<RootView, RootPresenter>{

    public static final String ROOT_TAG = "RootController";

    @BindView(R.id.controller_container)
    ViewGroup container;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Router router;
    private RootViewState rootViewState;
    private ActionBarDrawerToggle drawerToggle;

    @Inject
    RootPresenter rootPresenter;

    public RootController() {
        super(new Bundle());
        DaggerControllerComponent.builder()
                .repositoryComponent(Application.getRepositoryComponent())
                .presenterModule(new PresenterModule())
                .build()
                .inject(this);

        // only add lifecycle for mosby after dagger injection
        addLifecycleListener(new MvpConductorLifecycleListener<>(this));
        setRetainViewMode(Controller.RetainViewMode.RETAIN_DETACH);
    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_root, container, false);
    }

    @Override
    protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
        super.onChangeEnded(changeHandler, changeType);
        if (changeType == ControllerChangeType.PUSH_ENTER) {
            if (!router.hasRootController()) {
                // default controller
                /*if (getSharedPreferences().getBoolean("first_login", true)) {
                    //the app is being launched for first time, start setup
                    router.setRoot(RouterTransaction.with(new LoginController()).tag(LoginController.TAG));
                }else {*/
                    router.setRoot(RouterTransaction.with(new HomeController()).tag(HomeController.TAG));
                //}
            }
        }
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);
        setupUI();
    }

    @Override
    public boolean handleBack() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        // TODO: 24-Oct-17 if current controller is root but not the default page, go back to it
        return router.handleBack();
    }

    /**
     * From a Controller we can alter the state of the navDrawer to enable/disable it
     * This will enable/disable the menu button and the swiping gesture
     * If showUpIndicator is true a back button will be shown when the drawer is disabled
     * <p>
     * ie. A page like the first login page won't have access to menu or any previous page
     * so we disable menu and back arrow in the toolbar
     *
     * @param enable new state of Navigation Drawer
     */
    public void setNavigationDrawerEnabled(boolean enable, boolean showUpIndicator) {
        Log.d(ROOT_TAG, "Toggle Navigation drawer state:" + enable);

        if (enable) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            drawerToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED);
            drawerToggle.setDrawerIndicatorEnabled(true);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            drawerToggle.setDrawerIndicatorEnabled(false);
            if(showUpIndicator){
                drawerToggle.setHomeAsUpIndicator(R.drawable.arrow_back_white);
            }
        }
        drawerToggle.syncState();
    }

    private void setupNavigationDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                getActivity(), drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROOT_TAG, "DrawerToggle:pressed back");
                handleBack();
            }
        });
        drawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
    }

    private void setupUI() {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        setupNavigationDrawer();

        router = getChildRouter(container, null);
    }

    /**
     * Push controller to front
     *
     * @param controller controller being pushed
     * @param tag        controller tag, used to getController
     */
    public void changeController(Controller controller, String tag){
        router.pushController(RouterTransaction.with(controller).tag(tag));
    }

    /**
     * Might be preferred than having to instance a new Controller in changeController()
     * @param tag tag given to controller when it was created
     * @return controller if controller is in back stack
     */
    public Controller getControllerInBackStack(String tag){
        return router.getControllerWithTag(tag);
    }

    /**
     * Pushes Controller to root, so clicking the back button will close the app
     *
     * @param controller controller being pushed
     * @param tag        controller tag, used to getController
     */
    public void setRoot(Controller controller, String tag){
        router.setRoot(RouterTransaction.with(controller)
                .tag(tag)
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler()));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // TODO: 27/09/2017 add menu option for feedback
        if (item.isChecked()) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }

        switch (item.getItemId()) {
            case R.id.nav_home:
                String tag = HomeController.TAG;
                Controller controller = getControllerInBackStack(tag);
                changeController((controller == null) ? new HomeController() : controller, tag);
                break;

            case R.id.nav_categories:

                break;

            case R.id.nav_settings:

                break;

            case R.id.nav_trash:
                //Intent intent = new Intent(this, CategoriesActivity.class);
                //intent.putExtra(LIST_DELETED_KEY, true);
                break;

            case R.id.export:
                //basePresenter.exportFiles();
                drawerLayout.closeDrawer(GravityCompat.START);
                return false; // don't select item

            case R.id.create_period:
                //createPeriod();
                drawerLayout.closeDrawer(GravityCompat.START);
                return false; // don't select item
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        //item.setChecked(true);
        return true;
    }

    // MOSBY

    @NonNull
    @Override
    public RootPresenter createPresenter() {
        return rootPresenter;
    }

    @Nullable
    @Override
    public RootPresenter getPresenter() {
        return rootPresenter;
    }

    @Override
    public void setPresenter(@NonNull RootPresenter presenter) {
        this.rootPresenter = presenter;
    }

    @NonNull
    @Override
    public RootView getMvpView() {
        return this;
    }
}
