package ipleiria.project.add.view.categories;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import ipleiria.project.add.view.items.ScrollChildSwipeRefreshLayout;

import static ipleiria.project.add.R.id.area;
import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.CRITERIA_SELECTED;
import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.EDITING_ITEM;
import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.EDITING_ITEM_KEY;
import static ipleiria.project.add.view.itemdetail.ItemDetailPresenter.ITEM_KEY;
import static ipleiria.project.add.view.items.ItemsPresenter.LIST_DELETED_KEY;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoriesFragment extends Fragment implements CategoriesContract.View {

    private static final String TAG = "CATEGORIES_FRAGMENT";

    // on resume will show an item added message and enable swipe
    // it differs because CHANGE comes externally(from another app)
    // which disables the swiping until the file/item was added/created
    public static final int REQUEST_ADD_NEW_ITEM_CHANGE = 2090;
    // on resume will show an item added message
    public static final int REQUEST_ADD_NEW_ITEM = 2091;
    // on resume will show an item edited message
    public static  final int REQUEST_ITEM_EDIT = 2092;

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

        boolean listDeleted = getActivity().getIntent().getBooleanExtra(LIST_DELETED_KEY, false);
        categoriesAdapter = new CategoryAdapter(new LinkedList<Category>(), listDeleted);
        // if the activity as received an intent with an action(adding files to items then swipe will be disabled)
        itemsAdapter = new ItemAdapter(new LinkedList<Item>(), itemActionListener, listDeleted,
                categoriesPresenter.getIntentAction() == null);

        if(listDeleted){
            getActivity().findViewById(R.id.fab_add).setVisibility(View.GONE);
            setTitle("Trash");
        }else{
            setTitle("Dimensions");
        }
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
                categoriesPresenter.forceRefreshData();
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

        if(savedInstanceState != null){
            categoriesPresenter.restoreInstanceState(savedInstanceState);
        }

        return root;
    }

    private void addItem() {
        if(categoriesPresenter.getIntentAction() != null) {
            Intent intent = getActivity().getIntent();
            // change current intent to use a different activity, keeping extras and action
            intent.setComponent(new ComponentName(getContext(), AddEditActivity.class));
            startActivityForResult(intent, REQUEST_ADD_NEW_ITEM_CHANGE);
        }else{
            Intent intent = new Intent(getContext(), AddEditActivity.class);
            if(categoriesPresenter.getSelectedCriteria() != null){
                intent.setAction(CRITERIA_SELECTED);
                intent.putExtra(CRITERIA_SELECTED, categoriesPresenter.getSelectedCriteria().getRealReference());
            }
            startActivityForResult(intent, REQUEST_ADD_NEW_ITEM);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.items_activity_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                categoriesPresenter.searchItems(newText);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){

            case R.id.app_bar_period:
                categoriesPresenter.setPeriodSelection();

        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onBackPressed() {
        return categoriesPresenter.onBackPressed();
    }

    @Override
    public void setPresenter(CategoriesContract.Presenter presenter) {
        this.categoriesPresenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        categoriesPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        categoriesPresenter.unsubscribe();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        categoriesPresenter.onResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putAll(categoriesPresenter.saveInstanceState());
    }

    @Override
    public void showProgressDialog() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgressDialog() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

    }

    @Override
    public void setTitle(String title) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
    }

    @Override
    public void openPeriodSelection(CharSequence periods[]) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select evaluation period");
        builder.setItems(periods, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                categoriesPresenter.switchPeriod(which);
            }
        });
        builder.show();
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
        swipeRefreshLayout.setScrollUpChild(categoryListView);
        showLayout(categoryListView);
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
        hideLayout(categoryListView);

        noItemsView.findViewById(R.id.noItemsAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
    }

    @Override
    public void showNoDeletedItems() {
        TextView mainText = (TextView) noItemsView.findViewById(R.id.noItemsMain);
        mainText.setText(R.string.no_deleted_items);

        noItemsView.findViewById(R.id.noItemsAdd).setVisibility(View.GONE);

        showLayout(noItemsView);

        itemsListView.setVisibility(View.GONE);
        dimensionView.setVisibility(View.GONE);
        areaView.setVisibility(View.GONE);
        criteriaView.setVisibility(View.GONE);
        categoryListView.setVisibility(View.GONE);
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
    public void openItemDetails(Item clickedItem, boolean listingDeleted) {
        Intent intent = new Intent(getContext(), ItemDetailActivity.class);
        intent.putExtra(LIST_DELETED_KEY, listingDeleted);
        intent.putExtra(ITEM_KEY, clickedItem.getDbKey());
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
        //showMessage("Files saved to item");
    }

    @Override
    public void enableListSwipe(boolean b) {
        itemsAdapter.enableSwipe(true);
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
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
            view.findViewById(R.id.additional_info).setVisibility(View.VISIBLE);
        }

        ImageView expandableArrow = (ImageView) view.findViewById(R.id.expandable_arrow);
        expandableArrow.setVisibility(View.VISIBLE);

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView points = (TextView) view.findViewById(R.id.points);

        name.setText(category.getFormattedString());
        points.setText(String.valueOf(category.getPoints()));
        view.setOnClickListener(onClickListener);

        if(view.getVisibility() == View.GONE) {
            expand(view);
            expandableArrow.setRotation(0);
            expandableArrow.animate().rotation(180).setDuration(200).start();
        }
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
        public void onItemClick(Item clickedItem) {
            categoriesPresenter.onItemClicked(clickedItem);
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
        public void onPermanentDeleteItem(Item deletedItem) {
            categoriesPresenter.permanentlyDeleteItem(deletedItem);
        }

        @Override
        public void onRestoreItem(Item restoredItem) {
            categoriesPresenter.restoreItem(restoredItem);
        }
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
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)*2);
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
        if(targetView.getVisibility() == View.GONE) {
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
