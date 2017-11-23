package ipleiria.project.add.view.login;

import com.hannesdorfmann.mosby3.mvp.MvpView;

import ipleiria.project.add.view.base.ControllerView;

/**
 * Created by Lisboa on 23-Nov-17.
 */

public interface LoginView extends ControllerView{

    void showEmptyNameError();

    void showValidName();

    void showDatesInvalid();

    void showDatesValid();

    void setStartDateText(String format);

    void setEndDateText(String format);

    void createDatePickers(int i, int i1, int i2);
}
