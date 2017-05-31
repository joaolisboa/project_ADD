package ipleiria.project.add.view.categories;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesFragment extends Fragment implements CategoriesContract.View {

    private ProgressDialog progressDialog;

    private LinearLayout selectedDimensionView;
    private LinearLayout selectedAreaView;

    private LinearLayout dimensionsView;
    private LinearLayout areasView;
    private LinearLayout criteriasView;

    private CategoryAdapter dimensionsAdapter;
    private CategoryAdapter areasAdapter;
    private CategoryAdapter criteriasAdapter;

    private CategoriesContract.Presenter categoriesPresenter;

    public CategoriesFragment() {
    }

    public static CategoriesFragment newInstance() {
        return new CategoriesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dimensionsAdapter = new CategoryAdapter(new LinkedList<Category>());
        areasAdapter = new CategoryAdapter(new LinkedList<Category>());
        criteriasAdapter = new CategoryAdapter(new LinkedList<Category>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.categories_frag, container, false);

        selectedDimensionView = (LinearLayout) root.findViewById(R.id.dimension_selected);
        selectedDimensionView.setVisibility(View.GONE);
        selectedAreaView = (LinearLayout) root.findViewById(R.id.area_selected);
        selectedAreaView.setVisibility(View.GONE);

        dimensionsView = (LinearLayout) root.findViewById(R.id.dimensions);
        areasView = (LinearLayout) root.findViewById(R.id.areas);
        criteriasView = (LinearLayout) root.findViewById(R.id.criterias);

        ListView dimensionsList = (ListView) root.findViewById(R.id.dimensions_list);
        ListView areasList = (ListView) root.findViewById(R.id.areas_list);
        areasList.setVisibility(View.GONE);
        ListView criteriasList = (ListView) root.findViewById(R.id.criterias_list);
        criteriasList.setVisibility(View.GONE);

        dimensionsList.setAdapter(dimensionsAdapter);
        areasList.setAdapter(areasAdapter);
        criteriasList.setAdapter(criteriasAdapter);

        dimensionsList.setOnItemClickListener(listClickListener);
        areasList.setOnItemClickListener(listClickListener);
        criteriasList.setOnItemClickListener(listClickListener);

        setHasOptionsMenu(true);

        return root;
    }

    public boolean onBackPressed() {
        return categoriesPresenter.onBackPressed();
    }

    @Override
    public void setPresenter(CategoriesContract.Presenter presenter) {
        this.categoriesPresenter = presenter;
    }

    @Override
    public void onStart() {
        super.onStart();
        categoriesPresenter.subscribe();
    }

    @Override
    public void onStop() {
        super.onStop();
        categoriesPresenter.unsubscribe();
    }

    @Override
    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void setTitle(String title) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
    }

    @Override
    public void setDimensions(List<Dimension> dimensions) {
        dimensionsAdapter.replaceData(new LinkedList<Category>(dimensions));
    }

    @Override
    public void showDimensions(List<Dimension> dimensions) {
        hideLayout(selectedDimensionView);
        setDimensions(dimensions);
        showLayout(dimensionsView.findViewById(R.id.dimensions_list));
    }

    @Override
    public void showAreas(List<Area> areas) {
        hideLayout(selectedAreaView);
        areasAdapter.replaceData(new LinkedList<Category>(areas));
        showLayout(areasView.findViewById(R.id.areas_list));
    }

    @Override
    public void hideAreas() {
        hideLayout(areasView.findViewById(R.id.areas_list));
        hideLayout(selectedAreaView);
    }

    @Override
    public void showCriterias(List<Criteria> criterias) {
        criteriasAdapter.replaceData(new LinkedList<Category>(criterias));
        showLayout(criteriasView.findViewById(R.id.criterias_list));
    }

    @Override
    public void hideCriterias() {
        hideLayout(criteriasView.findViewById(R.id.criterias_list));
    }

    @Override
    public void showSelectedDimension(Dimension dimension) {
        hideLayout(dimensionsView.findViewById(R.id.dimensions_list));

        selectedDimensionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = selectedDimensionView.findViewById(R.id.expandable_dimension);
                if(view.getVisibility() == View.GONE) {
                    expand(view);
                }else{
                    collapse(view);
                }
            }
        });

        TextView name = (TextView) selectedDimensionView.findViewById(R.id.dimension_name);
        TextView points = (TextView) selectedDimensionView.findViewById(R.id.dimension_points);
        ImageButton cancel = (ImageButton) selectedDimensionView.findViewById(R.id.dimension_cancel);
        cancel.setOnClickListener(canceledDimensionClickListener);

        name.setText(dimension.getFormattedString());
        points.setText(String.valueOf(dimension.getPoints()));

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        selectedDimensionView.startAnimation(slideUp);
        selectedDimensionView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSelectedArea(Area area) {
        hideLayout(areasView.findViewById(R.id.areas_list));

        selectedAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = selectedAreaView.findViewById(R.id.expandable_area);
                if(view.getVisibility() == View.GONE) {
                    expand(view);
                }else{
                    collapse(view);
                }
            }
        });

        TextView name = (TextView) selectedAreaView.findViewById(R.id.area_name);
        TextView points = (TextView) selectedAreaView.findViewById(R.id.area_points);
        ImageButton cancel = (ImageButton) selectedAreaView.findViewById(R.id.area_cancel);
        cancel.setOnClickListener(canceledAreaClickListener);

        name.setText(area.getFormattedString());
        points.setText(String.valueOf(area.getPoints()));

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        selectedAreaView.startAnimation(slideUp);
        selectedAreaView.setVisibility(View.VISIBLE);
    }

    public static void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private View.OnClickListener canceledDimensionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            categoriesPresenter.returnToDimensionView();
        }
    };

    private View.OnClickListener canceledAreaClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            categoriesPresenter.returnToAreaView();
        }
    };

    private AdapterView.OnItemClickListener listClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            categoriesPresenter.categoryClicked((Category)parent.getItemAtPosition(position));
        }
    };

    private void showLayout(View targetView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = targetView.getWidth() / 2;
            int cy = targetView.getHeight() / 2;

            // get the final radius for the clipping circle
            float finalRadius = (float) Math.hypot(cx, cy);
            Animator anim = ViewAnimationUtils.createCircularReveal(targetView, cx, cy, 0, finalRadius);
            anim.start();
            targetView.setVisibility(View.VISIBLE);
        } else {
            Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            targetView.startAnimation(slideUp);
            targetView.setVisibility(View.VISIBLE);
        }
    }

    private void hideLayout(final View targetView) {
        if (targetView.getVisibility() == View.VISIBLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int cx = targetView.getWidth() / 2;
                int cy = targetView.getHeight() / 2;

                // get the initial radius for the clipping circle
                float initialRadius = (float) Math.hypot(cx, cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(targetView, cx, cy, initialRadius, 0);
                anim.start();
                targetView.setVisibility(View.GONE);
            } else {
                Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
                targetView.startAnimation(slideDown);
                targetView.setVisibility(View.GONE);
            }
        }
    }
}
