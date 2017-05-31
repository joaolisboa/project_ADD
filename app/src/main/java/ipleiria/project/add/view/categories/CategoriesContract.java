package ipleiria.project.add.view.categories;

import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesContract {

    interface View extends BaseView<Presenter> {

        void showProgressDialog();

        void hideProgressDialog();

        void setTitle(String title);

        void setDimensions(List<Dimension> dimensions);

        void showDimensions(List<Dimension> dimensions);

        void showAreas(List<Area> areas);

        void hideAreas();

        void showCriterias(List<Criteria> criterias);

        void hideCriterias();

        void showSelectedDimension(Dimension dimension);

        void showSelectedArea(Area area);
    }

    interface Presenter extends BasePresenter {

        void categoryClicked(Category dimension);

        boolean onBackPressed();

        void returnToDimensionView();

        void returnToAreaView();
    }

}
