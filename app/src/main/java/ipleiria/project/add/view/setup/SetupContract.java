package ipleiria.project.add.view.setup;

import java.util.Calendar;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;

/**
 * Created by Lisboa on 12-Jun-17.
 */

public class SetupContract {

    interface View extends BaseView<Presenter>{

        void setNameError();

        void showDatesInvalid();

        void showDatesValid();

        void setNameValid();

        void setStartDateText(String date);

        void setEndDateText(String date);

        void createDatePickers(int year, int month, int dayOfMonth);

        void showNoNetworkError();

    }

    interface Presenter{

        void setStartDate(int year, int month, int dayOfMonth);

        void setEndDate(int year, int month, int dayOfMonth);

        void onCreateClick(String name, String department);

        boolean isInputValid(String name);

        void setupCalendar();
    }

}
