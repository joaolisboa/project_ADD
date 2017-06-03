package ipleiria.project.add.view.categories;

import java.util.List;

import ipleiria.project.add.BasePresenter;
import ipleiria.project.add.BaseView;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesContract {

    interface View extends BaseView<Presenter> {

        void showProgressDialog();

        void hideProgressDialog();

        void setTitle(String title);

        void setCategoryPoints(Category category);

        void showDimensions(List<Dimension> dimensions);

        void showAreas(List<Area> areas);

        void showCriterias(List<Criteria> criterias);

        void showItemsList(List<Item> items);

        void hideSelectedDimension();

        void hideSelectedArea();

        void hideSelectedCriteria();

        void hideCategoryList();

        void showSelectedDimension(Dimension dimension);

        void showSelectedArea(Area area);

        void showSelectedCriteria(Criteria criteria);
    }

    interface Presenter extends BasePresenter {

        void categoryClicked(Category dimension);

        boolean onBackPressed();

        void returnToDimensionView();

        void returnToAreaView();

        void returnToCriteriaView();

        void refreshData();
    }

}
