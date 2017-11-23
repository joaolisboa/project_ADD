package ipleiria.project.add.view.login;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.hannesdorfmann.mosby3.mvp.conductor.delegate.MvpConductorDelegateCallback;
import com.hannesdorfmann.mosby3.mvp.conductor.delegate.MvpConductorLifecycleListener;

import javax.inject.Inject;

import butterknife.BindView;
import ipleiria.project.add.Application;
import ipleiria.project.add.R;
import ipleiria.project.add.dagger.component.DaggerControllerComponent;
import ipleiria.project.add.dagger.module.PresenterModule;
import ipleiria.project.add.utils.BundleBuilder;
import ipleiria.project.add.view.base.BaseController;
import ipleiria.project.add.view.home.HomeController;

/**
 * Created by Lisboa on 23-Nov-17.
 */

public class LoginController extends BaseController implements LoginView, MvpConductorDelegateCallback<LoginView, LoginPresenter> {

    public static final String TAG = "LOGIN_CONTROLLER";

    @BindView(R.id.name) EditText nameView;
    @BindView(R.id.department) Spinner departmentView;
    @BindView(R.id.start_date) EditText startDateView;
    @BindView(R.id.end_date) EditText endDateView;
    @BindView(R.id.date_error) TextView dateErrorView;
    @BindView(R.id.register) Button registerButton;

    private DatePickerDialog startDatePicker;
    private DatePickerDialog endDatePicker;

    @Inject
    LoginPresenter loginPresenter;

    public LoginController() {
        this(new BundleBuilder(new Bundle())
                .build());
    }

    public LoginController(Bundle args) {
        super(args);
        DaggerControllerComponent.builder()
                .repositoryComponent(Application.getRepositoryComponent())
                .presenterModule(new PresenterModule())
                .build().inject(this);

        addLifecycleListener(new MvpConductorLifecycleListener<>(this));
        setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_login, container, false);
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);
        getRootController().setNavigationDrawerEnabled(false, false);

        loginPresenter.setupCalendar();

        startDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDatePicker.show();
            }
        });
        endDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDatePicker.show();
            }
        });
        registerButton.setOnClickListener(registerClickListener);
    }

    View.OnClickListener registerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String name = nameView.getText().toString();
            String department = departmentView.getSelectedItem().toString().toUpperCase();

            if(loginPresenter.isInputValid(name)){
                loginPresenter.onRegisterClick(name, department);
                setRoot(new HomeController(), HomeController.TAG);
            }
        }
    };

    @Override
    public void showEmptyNameError() {
        nameView.setError(getString(R.string.empty_name));
    }

    @Override
    public void showValidName() {
        nameView.setError(null);
    }

    @Override
    public void showDatesInvalid() {
        startDateView.setError("Start date must come before end date");
        endDateView.setError("End date must come after start date");
        dateErrorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDatesValid() {
        startDateView.setError(null);
        endDateView.setError(null);
        dateErrorView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setStartDateText(String startDate) {
        startDateView.setText(startDate);
    }

    @Override
    public void setEndDateText(String endDate) {
        endDateView.setText(endDate);
    }

    @Override
    public void createDatePickers(int year, int month, int dayOfMonth) {
        startDatePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                loginPresenter.setStartDate(year, month, dayOfMonth);
            }
        }, year, month, dayOfMonth);
        endDatePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                loginPresenter.setEndDate(year, month, dayOfMonth);
            }
        }, year, month, dayOfMonth);
    }

    // MOSBY

    @NonNull
    @Override
    public LoginPresenter createPresenter() {
        return loginPresenter;
    }

    @Nullable
    @Override
    public LoginPresenter getPresenter() {
        return loginPresenter;
    }

    @Override
    public void setPresenter(@NonNull LoginPresenter presenter) {
        this.loginPresenter = loginPresenter;
    }

    @NonNull
    @Override
    public LoginView getMvpView() {
        return this;
    }
}
