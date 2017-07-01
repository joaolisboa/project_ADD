package ipleiria.project.add;

import ipleiria.project.add.data.model.User;

/**
 * Created by Lisboa on 01-Jul-17.
 */

public class BaseContract {

    interface View extends BaseView<Presenter>{

        void setUserInfo(User user);

        void showProgressDialog();

        void hideProgressDialog();

        /* related only to create period */

        void setStartDateText(String date);

        void setEndDateText(String date);

        void showDatesValid();

        void showDatesInvalid();

        void showOnlineFilesExported();

        void showOfflineFilesExported();

    }

    interface Presenter extends BasePresenter{

        void exportFiles();

        void createPeriod();

        boolean areDatesValid();

        void setStartDate(int year, int month, int dayOfMonth);

        void setEndDate(int year, int month, int dayOfMonth);
    }

}
