package ipleiria.project.add.view.categories;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;

/**
 * Created by Lisboa on 30-May-17.
 */

public class CategoryAdapter extends BaseAdapter{

    private boolean listingDeleted;
    private List<Category> items;
    //private LinkedHashMap<Category, TextView> attachedPointsTextView;

    public CategoryAdapter(List<Category> items, boolean listingDeleted){
        setList(items);

        this.listingDeleted = listingDeleted;
        //attachedPointsTextView = new LinkedHashMap<>();
    }

    private void setList(List<Category> categories){
        LinkedList<Category> orderedList = new LinkedList<>();

        // only reorder list if it's an instance of Dimension which is the first level
        if(!categories.isEmpty() && !(categories.get(0) instanceof Dimension)) {
            LinkedList<Category> firstItems = new LinkedList<>();
            LinkedList<Category> lastItems = new LinkedList<>();

            // order list by adding categories with items/points first
            for (Category category : categories) {
                if (!listingDeleted) {
                    if (category.getPoints() > 0) {
                        firstItems.add(category);
                    } else {
                        lastItems.add(category);
                    }
                } else {
                    if (category.getNumberOfDeletedItems() > 0) {
                        firstItems.add(category);
                    }
                }
            }
            orderedList.addAll(firstItems);
            orderedList.addAll(lastItems);
        }else{
            orderedList.addAll(categories);
        }

        this.items = orderedList;
    }

    void replaceData(List<Category> categories){
        setList(categories);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Category getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Category category = getItem(position);
        //attachView(category, viewHolder.points);

        viewHolder.name.setText(category.getFormattedString());
        if (!listingDeleted) {
            viewHolder.numItems.setText(convertView.getContext()
                    .getString(R.string.num_items, category.getNumberOfItems()));
        } else {
            viewHolder.numItems.setText(convertView.getContext()
                    .getString(R.string.num_deleted_items, category.getNumberOfDeletedItems()));
        }
        viewHolder.points.setText(String.valueOf(category.getPoints()));

        return convertView;
    }

    /*void setCategoryPoints(Category category, double points){
        attachedPointsTextView.get(category).setText(String.valueOf(points));
    }

    private void attachView(Category category, TextView view) {
        attachedPointsTextView.put(category, view);
    }

    private void removeAttachedView(Category category) {
        attachedPointsTextView.remove(category);
    }*/

    private class ViewHolder {

        TextView name;
        TextView numItems;
        TextView description;
        TextView points;

        ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.name);
            numItems = (TextView) view.findViewById(R.id.num_items);
            description = (TextView) view.findViewById(R.id.description);
            points = (TextView) view.findViewById(R.id.points);
        }

    }

}
