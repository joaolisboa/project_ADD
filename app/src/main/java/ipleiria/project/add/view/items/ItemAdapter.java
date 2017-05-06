package ipleiria.project.add.view.items;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.ItemDetailActivity;
import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;

import static ipleiria.project.add.ListItemActivity.CHANGING_DATA_SET;

/**
 * Created by Lisboa on 06-May-17.
 */

public class ItemAdapter extends BaseSwipeAdapter {

    private List<Item> listItems;
    private boolean listDeleted;
    private String action;
    private ItemsFragment.ItemActionListener actionsListener;

    public ItemAdapter(List<Item> listItems, ItemsFragment.ItemActionListener actionsListener, boolean listDeleted){
        setList(listItems);
        this.actionsListener = actionsListener;
        this.listDeleted = listDeleted;
    }

    private void setList(List<Item> items){
        listItems = items;
    }

    public void replaceData(List<Item> items) {
        setList(items);
        notifyDataSetChanged();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.bottom_layout_actions;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_adapter_item, null);
        Context context = parent.getContext();

        final SwipeLayout swipeLayout = (SwipeLayout) itemView.findViewById(R.id.bottom_layout_actions);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.setClickToClose(true);
        if (action != null) {
            swipeLayout.setSwipeEnabled(false);
        }
        FrameLayout itemLayout = (FrameLayout) itemView.findViewById(R.id.item_view);
        ImageView button1 = (ImageView) itemView.findViewById(R.id.action_1);
        ImageView button2 = (ImageView) itemView.findViewById(R.id.action_2);

        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onItemClick((Item) getItem(position));
            }
        });
        if (!listDeleted) {
            button1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.edit_icon));
            button2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_item_icon));
        } else {
            button1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.restore_item_white));
            button2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_forever_white));
        }

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!listDeleted) {
                    actionsListener.onEditItem((Item) getItem(position));
                }else{
                    actionsListener.onRestoreItem((Item) getItem(position));
                }
                swipeLayout.close(false);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!listDeleted) {
                    actionsListener.onDeleteItem((Item) getItem(position));
                }else{
                    actionsListener.onPermanentDeleteItem((Item) getItem(position));
                }
                swipeLayout.close(false);
            }
        });

        return itemView;
    }

    // update all views with data concerning the item
    // notifyDataSetChanged won't call generateView unless there's new items
    // so if a position is already occupied it'll call this method
    @Override
    public void fillValues(int position, View convertView) {
        Item item = (Item) getItem(position);
        TextView itemName = (TextView) convertView.findViewById(R.id.title_text_view);
        TextView itemCriteria = (TextView) convertView.findViewById(R.id.category_text_view);
        TextView numFilesView = (TextView) convertView.findViewById(R.id.num_files);

        itemName.setText(item.getDescription());
        itemCriteria.setText(item.getCategoryReference() + ". " + item.getCriteria().getName());

        int numFiles = item.getFiles(listDeleted).size();
        if (!listDeleted) {
            numFilesView.setText(convertView.getContext().getString(R.string.num_files, numFiles));
        } else {
            numFilesView.setText(convertView.getContext().getString(R.string.num_deleted_files, numFiles));
        }
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    void onItemAdded(Item item) {
        int pos = listItems.indexOf(item);
        if(pos < 0){
            listItems.add(item);
        }else{
            listItems.remove(pos);
            listItems.add(pos, item);
        }
        notifyDataSetChanged();
    }

    void onItemRemoved(Item deletedItem) {
        listItems.remove(deletedItem);
        notifyDataSetChanged();
    }
}
