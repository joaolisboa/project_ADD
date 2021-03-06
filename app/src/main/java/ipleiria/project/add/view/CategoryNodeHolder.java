package ipleiria.project.add.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Category;

/**
 * Created by Lisboa on 12-Apr-17.
 */

public class CategoryNodeHolder extends TreeNode.BaseNodeViewHolder<Category>  {

    private int layout;

    public CategoryNodeHolder(Context context, int layout) {
        super(context);
        this.layout = layout;
    }

    @Override
    public View createNodeView(TreeNode node, Category value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(layout, null, false);

        TextView tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(value.getReference() + ". " + value.getName());

        TextView points = (TextView) view.findViewById(R.id.points);
        points.setText(String.valueOf(value.getPoints()));

        return view;
    }
}
