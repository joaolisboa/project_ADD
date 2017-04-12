package ipleiria.project.add;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import ipleiria.project.add.Model.Category;

/**
 * Created by Lisboa on 12-Apr-17.
 */

public class NodeHolder extends TreeNode.BaseNodeViewHolder<Category>  {

    private int layout;

    public NodeHolder(Context context, int layout) {
        super(context);
        this.layout = layout;
    }

    @Override
    public View createNodeView(TreeNode node, Category value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(layout, null, false);
        TextView tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(value.getReference() + ". " + value.getName());
        return view;
    }
}
