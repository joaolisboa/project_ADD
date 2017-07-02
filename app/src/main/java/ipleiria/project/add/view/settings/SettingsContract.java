package ipleiria.project.add.view.settings;

import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.User;

/**
 * Created by Lisboa on 06-May-17.
 */

public class SettingsContract {

    interface View extends BaseView<Presenter>{

        void showDropboxLogoutDialog();

        void showMEOCloudLogoutDialog();

        void showDropboxLogin();

        void showMEOCloudLogin();

        void setUserInfo(User user);

        void setAnonymousUserInfo(User user);

        void setMEOCloudStatus(boolean status);

        void setDropboxStatus(boolean status);

        void setDimensionViews(List<Dimension> dimensions, User user);

        void openEvaluationPeriodDialog(List<EvaluationPeriod> evaluationPeriods);
    }

    interface Presenter extends BasePresenter{
        
        void onDropboxAction();

        void onMEOCloudAction();

        void onActivityResume();

        void signOutMEOCloud();

        void signOutDropbox();

        void setLoginIntention(boolean loginIntent);

        void updateServicesStatus();

        boolean isWeightValid(Dimension dimension, int weightInserted);

        boolean areWeightsValid();

        boolean isSumOfWeightsValid();

        void setWeight(Dimension dimension, int weightInserted);

        void commitDimensionWeightChanges();

        void onEvaluationPeriodsClick();

        void deleteEvaluationPeriod(EvaluationPeriod period);
    }

}
