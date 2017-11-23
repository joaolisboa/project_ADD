package ipleiria.project.add.view.login;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.view.base.LogPresenter;

import static ipleiria.project.add.data.source.UserService.PERIOD_DATE_FORMAT;

/**
 * Created by Lisboa on 23-Nov-17.
 */

public class LoginPresenter extends LogPresenter<LoginView> {

    private static final String TAG = "LOGIN_PRESENTER";

    private LoginView loginView;

    private Calendar calendar;
    private Date startDate;
    private Date endDate;

    private DateFormat dateFormat;

    private UserService userService;

    @SuppressLint("SimpleDateFormat")
    public LoginPresenter(UserService userService){
        super(TAG);

        this.userService = userService;

        this.calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat(PERIOD_DATE_FORMAT);
    }

    @Override
    public void attachView(LoginView view) {
        super.attachView(view);
        this.loginView = view;
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if(!retainInstance){
            loginView = null;
        }
    }

    boolean isInputValid(String name) {
        Log.d(TAG, "isInputValid: " + name);
        if(TextUtils.isEmpty(name)){
            loginView.showEmptyNameError();
            return false;
        }else{
            loginView.showValidName();
        }

        return true;
    }

    void onRegisterClick(String name, String department) {
        Log.d(TAG, "onRegisterClick: " + name + ", " + department);
    }

    void setStartDate(int year, int month, int dayOfMonth) {
        setDate(year, month, dayOfMonth);
        startDate = calendar.getTime();
        verifyDates();
        loginView.setStartDateText(dateFormat.format(calendar.getTime()));
    }

    void setEndDate(int year, int month, int dayOfMonth) {
        setDate(year, month, dayOfMonth);
        endDate = calendar.getTime();
        verifyDates();
        loginView.setEndDateText(dateFormat.format(calendar.getTime()));
    }

    private void verifyDates(){
        if(startDate != null && endDate != null && endDate.compareTo(startDate) < 0){
            loginView.showDatesInvalid();
        }else {
            loginView.showDatesValid();
        }
    }

    void setupCalendar() {
        loginView.createDatePickers(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void setDate(int year, int month, int dayOfMonth){
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }
}
