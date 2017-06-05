package ipleiria.project.add.view.categories;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import ipleiria.project.add.ItemClickListener;
import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.view.add_edit_item.AddEditActivity;
import ipleiria.project.add.view.itemdetail.ItemDetailActivity;
import ipleiria.project.add.view.items.ItemAdapter;
import ipleiria.project.add.view.items.ItemsFragment;
import ipleiria.project.add.view.items.ScrollChildSwipeRefreshLayout;

import static ipleiria.project.add.R.id.area;
import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.EDITING_ITEM;
import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.EDITING_ITEM_KEY;
import static ipleiria.project.add.view.itemdetail.ItemDetailPresenter.ITEM_KEY;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ADD_NEW_ITEM;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ITEM_EDIT;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesFragment extends Fragment implements CategoriesContract.View {

    private ProgressDialog progressDialog;
    private ScrollChildSwipeRefreshLayout swipeRefreshLayout;

    // views with details about the category
    private LinearLayout dimensionView;
    private LinearLayout areaView;
    private LinearLayout criteriaView;
    private LinearLayout noItemsView;

    private CategoryAdapter categoriesAdapter;
    private ListView categoryListView;

    private ListView itemsListView;
    private ItemAdapter itemsAdapter;

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
        itemsAdapter = new ItemAdapter(new LinkedList<Item>(), itemActionListener, false, true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.categories_frag, container, false);

        categoryListView = (ListView) root.findViewById(R.id.category_list);
        categoryListView.setAdapter(categoriesAdapter);
        categoryListView.setOnItemClickListener(listClickListener);

        itemsListView = (ListView) root.findViewById(R.id.items_list);
        itemsListView.setAdapter(itemsAdapter);

        dimensionView = (LinearLayout) root.findViewById(R.id.dimension);
        areaView = (LinearLayout) root.findViewById(area);
        criteriaView = (LinearLayout) root.findViewById(R.id.criteria);
        noItemsView = (LinearLayout) root.findViewById(R.id.noItems);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        categoriesPresenter.onResult(requestCode, resultCode, data);
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
    public void showDimensions(List<Dimension> dimensions) {
        categoriesAdapter.replaceData(new LinkedList<Category>(dimensions));
        showCategoryList();
    }

    @Override
    public void showAreas(List<Area> areas) {
        categoriesAdapter.replaceData(new LinkedList<Category>(areas));
        showCategoryList();
    }

    @Override
    public void showCriterias(List<Criteria> criterias) {
        categoriesAdapter.replaceData(new LinkedList<Category>(criterias));
        showCategoryList();
    }

    private void showCategoryList(){
        showLayout(categoryListView);
        swipeRefreshLayout.setScrollUpChild(categoryListView);
        hideLayout(noItemsView);
        hideLayout(itemsListView);
    }

    @Override
    public void hideSelectedDimension(){
        hideLayout(dimensionView);
    }

    @Override
    public void hideSelectedArea() {
        hideLayout(areaView);
    }

    @Override
    public void hideSelectedCriteria() {
        hideLayout(criteriaView);
    }

    @Override
    public void hideCategoryList() {
        hideLayout(categoryListView);
    }

    @Override
    public void showSelectedDimension(Dimension dimension) {
        setSelectedViewInfo(dimension, dimensionView, canceledDimensionClickListener);
    }

    @Override
    public void showSelectedArea(Area area) {
        setSelectedViewInfo(area, areaView, canceledAreaClickListener);
    }

    @Override
    public void showSelectedCriteria(Criteria criteria){
        setSelectedViewInfo(criteria, criteriaView, canceledCriteriaClickListener);
    }

    @Override
    public void showNoItems() {
        showLayout(noItemsView);
        hideLayout(itemsListView);
    }

    @Override
    public void showItemsList(List<Item> items) {
        itemsAdapter.replaceData(items);
        showLayout(itemsListView);
        swipeRefreshLayout.setScrollUpChild(itemsListView);
        hideLayout(noItemsView);
    }

    @Override
    public void removeDeletedItem(Item item) {
        itemsAdapter.onItemRemoved(item);
    }

    @Override
    public void openItemDetails(Item clickedIem) {
        Intent intent = new Intent(getContext(), ItemDetailActivity.class);
        intent.putExtra(ITEM_KEY, clickedIem.getDbKey());
        startActivity(intent);
    }

    @Override
    public void showItemEditedMessage() {
        showMessage("Item saved");
    }

    @Override
    public void showItemAddedMessage(){
        showMessage("New item saved");
    }

    @Override
    public void showFilesAddedMessage() {
        showMessage("Files saved to item");
    }

    @Override
    public void enableListSwipe(boolean b) {
        itemsAdapter.enableSwipe(true);
    }

    @Override
    public void showSearchItems(List<Item> matchingItems) {
        itemsAdapter.replaceData(matchingItems);
        itemsListView.setVisibility(View.VISIBLE);

        noItemsView.setVisibility(View.GONE);
        dimensionView.setVisibility(View.GONE);
        areaView.setVisibility(View.GONE);
        criteriaView.setVisibility(View.GONE);
        categoryListView.setVisibility(View.GONE);
    }

    private void showMessage(String message){
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    private void setSelectedViewInfo(Category category, final View view, View.OnClickListener onClickListener){
        if(category instanceof Criteria) {
            final View expandable = view.findViewById(R.id.expandable);
            String observations = ((Criteria)category).getObservations();
            TextView observationView = (TextView) expandable.findViewById(R.id.observations);
            if(observations != null && !observations.isEmpty()) {
                observationView.setText(observations);
            }else{
                observationView.setText("No observations");
            }

            final View expandableArrow = view.findViewById(R.id.expandable_arrow);
            expandableArrow.setVisibility(View.VISIBLE);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (expandable.getVisibility() == View.GONE) {
                        expand(expandable);
                        expandableArrow.animate().rotation(180).setDuration(200).start();
                    } else {
                        collapse(expandable);
                        expandableArrow.animate().rotation(0).setDuration(200).start();
                    }
                }
            });
        }

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView points = (TextView) view.findViewById(R.id.points);
        ImageButton cancel = (ImageButton) view.findViewById(R.id.cancel);

        name.setText(category.getFormattedString());
        points.setText(String.valueOf(category.getPoints()));
        cancel.setOnClickListener(onClickListener);

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
        view.startAnimation(slideUp);
        view.setVisibility(View.VISIBLE);
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

    private View.OnClickListener canceledCriteriaClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            categoriesPresenter.returnToCriteriaView();
        }
    };

    private AdapterView.OnItemClickListener listClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            categoriesPresenter.categoryClicked((Category)parent.getItemAtPosition(position));
        }
    };

    ItemClickListener itemActionListener = new ItemClickListener() {

        @Override
        public void onItemClick(Item clickedIem) {
            categoriesPresenter.onItemClicked(clickedIem);
        }

        @Override
        public void onDeleteItem(Item deletedItem) {
            categoriesPresenter.deleteItem(deletedItem);
        }

        @Override
        public void onEditItem(Item editedItem) {
            Intent intent = new Intent(getContext(), AddEditActivity.class);
            intent.setAction(EDITING_ITEM);
            intent.putExtra(EDITING_ITEM_KEY, editedItem.getDbKey());
            startActivityForResult(intent, REQUEST_ITEM_EDIT);
        }

        @Override
        public void onPermanentDeleteItem(Item deletedItem) {}

        @Override
        public void onRestoreItem(Item restoredItem) {}
    };

    public void expand(final View v) {
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
