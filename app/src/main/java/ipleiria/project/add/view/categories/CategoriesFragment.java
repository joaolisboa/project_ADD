package ipleiria.project.add.view.categories;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import ipleiria.project.add.view.add_edit_item.AddEditActivity;
import ipleiria.project.add.view.items.ScrollChildSwipeRefreshLayout;

import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesFragment extends Fragment implements CategoriesContract.View {

    private ProgressDialog progressDialog;
    private ScrollChildSwipeRefreshLayout swipeRefreshLayout;

    // views with details about the category
    private LinearLayout dimensionView;
    private LinearLayout areaView;
    //private LinearLayout criteriasView;

    private CategoryAdapter categoriesAdapter;
    private ListView categoryListView;

    private CategoriesContract.Presenter categoriesPresenter;

    public CategoriesFragment() {
    }

    public static CategoriesFragment newInstance() {
        return new CategoriesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoriesAdapter = new CategoryAdapter(new LinkedList<Category>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.categories_frag, container, false);

        categoryListView = (ListView) root.findViewById(R.id.category_list);
        categoryListView.setAdapter(categoriesAdapter);
        categoryListView.setOnItemClickListener(listClickListener);

        dimensionView = (LinearLayout) root.findViewById(R.id.dimension);
        dimensionView.setVisibility(View.GONE);
        areaView = (LinearLayout) root.findViewById(R.id.area);
        areaView.setVisibility(View.GONE);

        swipeRefreshLayout = (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(categoryListView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                categoriesPresenter.refreshData();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_add);
        fab.setImageResource(R.drawable.add_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    private void addItem() {
        /*if(itemsPresenter.getIntentAction() != null) {
            Intent intent = getActivity().getIntent();
            // change intent to use a different activity, keeping extras and action
            intent.setComponent(new ComponentName(getContext(), AddEditActivity.class));
            startActivityForResult(intent, REQUEST_ADD_NEW_ITEM_CHANGE);
        }else{*/
            startActivityForResult(new Intent(getContext(), AddEditActivity.class), REQUEST_ADD_NEW_ITEM);
        //}
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
        if(swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void setTitle(String title) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
    }

    @Override
    public void setCategoryPoints(Category category){
        categoriesAdapter.setCategoryPoints(category, category.getPoints());
    }

    @Override
    public void setDimensions(List<Dimension> dimensions) {
        categoriesAdapter.replaceData(new LinkedList<Category>(dimensions));
    }

    @Override
    public void showDimensions(List<Dimension> dimensions) {
        hideLayout(dimensionView);
        setDimensions(dimensions);
        showLayout(categoryListView);
    }

    @Override
    public void showAreas(List<Area> areas) {
        hideLayout(areaView);
        //areasAdapter.replaceData(new LinkedList<Category>(areas));
        categoriesAdapter.replaceData(new LinkedList<Category>(areas));
        //showLayout(areasView.findViewById(R.id.areas_list));
    }

    @Override
    public void hideAreas() {
        //hideLayout(dimensionsView.findViewById(R.id.dimensions_list));
        hideLayout(areaView);
    }

    @Override
    public void showCriterias(List<Criteria> criterias) {
        //criteriasAdapter.replaceData(new LinkedList<Category>(criterias));
        categoriesAdapter.replaceData(new LinkedList<Category>(criterias));
        //showLayout(dimensionsView.findViewById(R.id.dimensions_list));
    }

    @Override
    public void hideCriterias() {
        //hideLayout(dimensionsView.findViewById(R.id.dimensions_list));
    }

    @Override
    public void showSelectedDimension(Dimension dimension) {
        //hideLayout(dimensionsView.findViewById(R.id.dimensions_list));

        dimensionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = dimensionView.findViewById(R.id.expandable_dimension);
                if(view.getVisibility() == View.GONE) {
                    expand(view);
                }else{
                    collapse(view);
                }
            }
        });

        TextView name = (TextView) dimensionView.findViewById(R.id.dimension_name);
        TextView points = (TextView) dimensionView.findViewById(R.id.dimension_points);
        ImageButton cancel = (ImageButton) dimensionView.findViewById(R.id.dimension_cancel);
        cancel.setOnClickListener(canceledDimensionClickListener);

        name.setText(dimension.getFormattedString());
        points.setText(String.valueOf(dimension.getPoints()));

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        dimensionView.startAnimation(slideUp);
        dimensionView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSelectedArea(Area area) {
        //hideLayout(areasView.findViewById(R.id.areas_list));

        areaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = areaView.findViewById(R.id.expandable_area);
                if(view.getVisibility() == View.GONE) {
                    expand(view);
                }else{
                    collapse(view);
                }
            }
        });

        TextView name = (TextView) areaView.findViewById(R.id.area_name);
        TextView points = (TextView) areaView.findViewById(R.id.area_points);
        ImageButton cancel = (ImageButton) areaView.findViewById(R.id.area_cancel);
        cancel.setOnClickListener(canceledAreaClickListener);

        name.setText(area.getFormattedString());
        points.setText(String.valueOf(area.getPoints()));

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        areaView.startAnimation(slideUp);
        areaView.setVisibility(View.VISIBLE);
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