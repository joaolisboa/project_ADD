package ipleiria.project.add.view.categories;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Item;

/**
 * Created by Lisboa on 30-May-17.
 */

class CategoryAdapter extends BaseAdapter{

    private List<Category> items;
    private LinkedHashMap<Category, TextView> attachedPointsTextView;

    CategoryAdapter(List<Category> items){
        setList(items);

        attachedPointsTextView = new LinkedHashMap<>();
    }

    private void setList(List<Category> items){
        this.items = items;
    }

    void replaceData(List<Category> items){
        setList(items);
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
        attachView(category, viewHolder.points);

        viewHolder.name.setText(category.getFormattedString());
        viewHolder.description.setText(category.getName());
        viewHolder.points.setText(String.valueOf(category.getPoints()));

        return convertView;
    }

    void setCategoryPoints(Category category, double points){
        attachedPointsTextView.get(category).setText(String.valueOf(points));
    }

    private void attachView(Category category, TextView view) {
        attachedPointsTextView.put(category, view);
    }

    private void removeAttachedView(Category category) {
        attachedPointsTextView.remove(category);
    }

    private class ViewHolder {

        TextView name;
        TextView description;
        TextView points;

        ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.name);
            description = (TextView) view.findViewById(R.id.description);
            points = (TextView) view.findViewById(R.id.points);
        }

    }

}
