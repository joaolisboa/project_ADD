package ipleiria.project.add.view.setup;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.utils.NetworkState;

import static ipleiria.project.add.data.source.UserService.PERIOD_DATE_FORMAT;
import static ipleiria.project.add.data.source.UserService.USER_DATA_KEY;

/**
 * Created by Lisboa on 12-Jun-17.
 */

public class SetupPresenter implements SetupContract.Presenter{

    private final UserService userService;
    private SetupContract.View setupView;

    private SharedPreferences sharedPreferences;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    private Date startDate;
    private Date endDate;

    @SuppressLint("SimpleDateFormat")
    SetupPresenter(SetupContract.View setupView, UserService userService){
        this.setupView = setupView;
        this.setupView.setPresenter(this);

        this.userService = userService;

        calendar = Calendar.getInstance();
        sharedPreferences = Application.getAppContext().getSharedPreferences(USER_DATA_KEY,0);
        dateFormat = new SimpleDateFormat(PERIOD_DATE_FORMAT);
    }

    @Override
    public void setStartDate(int year, int month, int dayOfMonth) {
        setDate(year, month, dayOfMonth);
        startDate = calendar.getTime();

        //verify date comes before endDate
        if(endDate != null){
            if(startDate.compareTo(endDate) > 0){
                setupView.showDatesInvalid();
            }else{
                setupView.showDatesValid();
            }
        }
        setupView.setStartDateText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void setEndDate(int year, int month, int dayOfMonth) {
        setDate(year, month, dayOfMonth);
        endDate = calendar.getTime();

        //verify date comes after startDate
        if(startDate != null){
            if(endDate.compareTo(startDate) < 0){
                setupView.showDatesInvalid();
            }else{
                setupView.showDatesValid();
            }
        }
        setupView.setEndDateText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onCreateClick(String name, String department) {
        User user = userService.getUser();

        EvaluationPeriod evaluationPeriod = new EvaluationPeriod();
        evaluationPeriod.setStartDate(startDate);
        evaluationPeriod.setEndDate(endDate);
        user.setDepartment(department);
        user.addEvaluationPeriod(evaluationPeriod);
        user.setName(name);
        userService.setUser(user);
        sharedPreferences.edit().putBoolean("my_first_time", false).apply();
    }

    @Override
    public boolean isInputValid(String name) {
        if(name == null || name.isEmpty()){
            setupView.setNameError();
            return false;
        }else{
            setupView.setNameValid();
        }
        if(startDate == null || endDate == null){
            return false;
        }

        if(!NetworkState.isOnline()){
            setupView.showNoNetworkError();
            return false;
        }

        return startDate.compareTo(endDate) <= 0;
    }

    @Override
    public void setupCalendar() {
        setupView.createDatePickers(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void setDate(int year, int month, int dayOfMonth){
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }


}
