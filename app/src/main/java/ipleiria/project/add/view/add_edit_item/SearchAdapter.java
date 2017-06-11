package ipleiria.project.add.view.add_edit_item;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Criteria;

/**
 * Created by Lisboa on 11-Jun-17.
 */

public class SearchAdapter extends BaseAdapter {

    private List<Criteria> criterias;



    @Override
    public int getCount() {
        return criterias.size();
    }

    @Override
    public Object getItem(int position) {
        return criterias.get(position);
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
/*
        Category category = getItem(position);
        attachView(category, viewHolder.points);

        viewHolder.name.setText(category.getFormattedString());
        viewHolder.description.setText(category.getName());
        viewHolder.points.setText(String.valueOf(category.getPoints()));*/

        return null;
    }

    private class ViewHolder{

        TextView description;
        TextView points;

        ViewHolder(View view) {
            description = (TextView) view.findViewById(R.id.description);
            points = (TextView) view.findViewById(R.id.points);
        }

    }

}
