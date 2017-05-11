package ipleiria.project.add.view.add_edit_item;

import android.content.Intent;
import android.net.Uri;

import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;

/**
 * Created by Lisboa on 07-May-17.
 */

public class AddEditContract {

    interface View extends BaseView<Presenter>{

        void createTreeView(List<Dimension> dimensions);

        void showSearchItems(List<Criteria> criterias);

        void setSelectedCriteria(Criteria criteria);

        void hideSearch();

        void setItemInfo(Item item);

        void setFilesInfo(List<Uri> receivedFiles);

        String getDescriptionText();

        void finish();

        void showEmptyDescriptionError();

        void showNoSelectedCriteriaError();
    }

    interface Presenter {

        void subscribe(Intent intent);

        void searchForCriteria(String query);

        void selectedCriteria(Criteria criteria);

        void finishAction();

    }

}
