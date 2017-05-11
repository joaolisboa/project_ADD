package ipleiria.project.add.view.items;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;

/**
 * Created by Lisboa on 06-May-17.
 */

public class ItemAdapter extends BaseSwipeAdapter {

    private List<Item> listItems;
    private boolean listingDeleted;
    private String action;
    private ItemsFragment.ItemActionListener actionsListener;

    ItemAdapter(List<Item> listItems, ItemsFragment.ItemActionListener actionsListener, boolean listingDeleted, String action){
        setList(listItems);
        this.actionsListener = actionsListener;
        this.listingDeleted = listingDeleted;
        this.action = action;
    }

    private void setList(List<Item> items){
        listItems = items;
    }

    void replaceData(List<Item> items) {
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
        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onItemClick((Item) getItem(position));
            }
        });

        ImageView button1 = (ImageView) itemView.findViewById(R.id.action_1);
        ImageView button2 = (ImageView) itemView.findViewById(R.id.action_2);


        if (!listingDeleted) {
            button1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.edit_white));
            button2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_white));
        } else {
            button1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.restore_white));
            button2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_forever_white));
        }

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!listingDeleted) {
                    actionsListener.onEditItem((Item) getItem(position));
                }else{
                    actionsListener.onRestoreItem((Item) getItem(position));
                }
                closeItem(position);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!listingDeleted) {
                    actionsListener.onDeleteItem((Item) getItem(position));
                }else{
                    actionsListener.onPermanentDeleteItem((Item) getItem(position));
                }
                closeItem(position);
            }
        });

        return itemView;
    }

    // update all views with data concerning the item
    // notifyDataSetChanged won't call generateView unless there's new items
    // so if a position is already occupied it'll call this method
    @Override
    public void fillValues(final int position, View convertView) {
        Item item = (Item) getItem(position);

        TextView itemName = (TextView) convertView.findViewById(R.id.title_text_view);
        TextView itemCriteria = (TextView) convertView.findViewById(R.id.category_text_view);
        TextView numFilesView = (TextView) convertView.findViewById(R.id.num_files);

        itemName.setText(item.getDescription());
        itemCriteria.setText(item.getCategoryReference() + ". " + item.getCriteria().getName());

        if (!listingDeleted) {
            numFilesView.setText(convertView.getContext().getString(R.string.num_files, item.getFiles().size()));
        } else {
            numFilesView.setText(convertView.getContext().getString(R.string.num_deleted_files, item.getDeletedFiles().size()));
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
