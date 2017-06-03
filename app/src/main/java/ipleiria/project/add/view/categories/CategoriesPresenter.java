package ipleiria.project.add.view.categories;

import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.FileUtils;
import ipleiria.project.add.view.items.ItemsContract;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesPresenter implements CategoriesContract.Presenter {

    private static final String TAG = "CATEGORIES_PRESENTER";

    // only showing dimensions
    private static final int ROOT_FOCUS = 0;
    // showing selected dimension on the top with areas below
    private static final int DIMENSION_FOCUS = 1;
    // showing selected dimension & area on the top with criterias below
    private static final int AREA_FOCUS = 2;

    private Dimension selectedDimension;
    private int currentFocus = 0;

    private final CategoryRepository categoryRepository;
    private final ItemsRepository itemsRepository;
    private final CategoriesContract.View categoriesView;

    public CategoriesPresenter(CategoriesContract.View categoriesView, CategoryRepository categoryRepository, ItemsRepository itemsRepository) {
        this.categoryRepository = categoryRepository;
        this.itemsRepository = itemsRepository;
        this.categoriesView = categoriesView;
        this.categoriesView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        refreshData();
    }


    public void refreshData(){
        categoriesView.showProgressDialog();

        categoryRepository.readData(new FilesRepository.Callback<List<Dimension>>() {
            @Override
            public void onComplete(List<Dimension> result) {
                categoriesView.showDimensions(result);

                itemsRepository.getRemoteItems(new FilesRepository.Callback<List<Item>>() {
                    @Override
                    public void onComplete(List<Item> result) {
                        FileUtils.readExcel(Application.getAppContext());
                        for(Dimension dimension: categoryRepository.getDimensions()){
                            categoriesView.setCategoryPoints(dimension);
                        }
                        categoriesView.hideProgressDialog();
                    }

                    @Override
                    public void onError(Exception e) {
                        categoriesView.hideProgressDialog();
                    }
                });

            }

            @Override
            public void onError(Exception e) {
                categoriesView.hideProgressDialog();
            }
        });
    }

    // write/read excel file to calculate points
    private void processPoints(final List<Dimension> result){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                FileUtils.readExcel(Application.getAppContext());
                categoriesView.showDimensions(result);
                categoriesView.hideProgressDialog();
            }
        });
    }

    @Override
    public void unsubscribe() {
        categoriesView.hideProgressDialog();
    }

    @Override
    public void categoryClicked(Category category) {
        if (category instanceof Dimension) {
            dimensionClicked((Dimension) category);
        } else if (category instanceof Area) {
            areaClicked((Area) category);
        } else if (category instanceof Criteria) {
            criteriaClicked((Criteria) category);
        } else {
            Log.e(TAG, "Invalid object - shouldn't happen");
        }

        switch (currentFocus) {
            case ROOT_FOCUS:
                categoriesView.setTitle("Dimensions");
                break;

            case DIMENSION_FOCUS:
                categoriesView.setTitle("Areas");
                break;

            case AREA_FOCUS:
                categoriesView.setTitle("Criterias");
                break;
        }
    }

    private void dimensionClicked(Dimension dimension) {
        selectedDimension = dimension;
        categoriesView.showSelectedDimension(dimension);
        categoriesView.showAreas(dimension.getAreas());
        currentFocus = DIMENSION_FOCUS;

    }

    private void areaClicked(Area area) {
        categoriesView.showSelectedArea(area);
        categoriesView.showCriterias(area.getCriterias());
        currentFocus = AREA_FOCUS;
    }

    private void criteriaClicked(Criteria criteria) {
        // only with AREA_FOCUS
        //categoriesView.openItemsList(criteria);
    }

    @Override
    public boolean onBackPressed() {
        switch(currentFocus){
            case DIMENSION_FOCUS:
                returnToDimensionView();
                return true;

            case AREA_FOCUS:
                returnToAreaView();
                return true;
        }

        return false;
    }

    @Override
    public void returnToDimensionView() {
        categoriesView.hideAreas();
        if (currentFocus == AREA_FOCUS) {
            categoriesView.hideCriterias();
        }
        categoriesView.showDimensions(categoryRepository.getDimensions());
        currentFocus = ROOT_FOCUS;
    }

    @Override
    public void returnToAreaView() {
        categoriesView.hideCriterias();
        categoriesView.showAreas(selectedDimension.getAreas());
        currentFocus = DIMENSION_FOCUS;
    }
}
