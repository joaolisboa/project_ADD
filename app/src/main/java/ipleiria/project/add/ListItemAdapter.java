package ipleiria.project.add;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ipleiria.project.add.Model.Item;

/**
 * Created by Lisboa on 04-Apr-17.
 */

class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ItemViewHolder> {

    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec

    private List<Item> items;
    private List<Item> itemsPendingRemoval;
    private int lastInsertedIndex; // so we can add some more items, not implemented

    private Handler handler = new Handler(); // hanlder for running delayed runnables
    private HashMap<Item, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be

    ListItemAdapter(List<Item> items) {
        this.items = items;
        itemsPendingRemoval = new ArrayList<>();
        lastInsertedIndex = items.size();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final Item item = items.get(position);

        if (itemsPendingRemoval.contains(item)) {
            // we need to show the "undo" state of the row
            holder.itemView.setBackgroundColor(Color.RED);
            holder.titleTextView.setVisibility(View.GONE);
            holder.descriptionTextView.setVisibility(View.GONE);
            holder.thumbnail.setVisibility(View.GONE);
            holder.undoButton.setVisibility(View.VISIBLE);
            holder.undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // user wants to undo the removal, let's cancel the pending task
                    Runnable pendingRemovalRunnable = pendingRunnables.get(item);
                    pendingRunnables.remove(item);
                    if (pendingRemovalRunnable != null)
                        handler.removeCallbacks(pendingRemovalRunnable);
                    itemsPendingRemoval.remove(item);
                    // this will rebind the row in "normal" state
                    notifyItemChanged(items.indexOf(item));
                }
            });
        } else {
            // we need to show the "normal" state
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.thumbnail.setVisibility(View.VISIBLE);
            //holder.thumbnail.setBackgroundResource(R.drawable.default_file_thumb);
            /*if(item.getFile() != null){
                viewHolder.thumbnail.setImageBitmap(getbitpam(item.getFile().getAbsolutePath()));
            }else{
                viewHolder.thumbnail.setBackgroundResource(R.drawable.default_file_thumb);
            }*/
            holder.descriptionTextView.setVisibility(View.VISIBLE);
            //holder.descriptionTextView.setText(item.getCategory());
            holder.titleTextView.setVisibility(View.VISIBLE);
            holder.titleTextView.setText(item.getName());
            holder.undoButton.setVisibility(View.GONE);
            holder.undoButton.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void pendingRemoval(int position) {
        final Item item = items.get(position);
        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item);
            notifyItemChanged(position);
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(items.indexOf(item));
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(item, pendingRemovalRunnable);
        }
    }

    private void remove(int position) {
        Item item = items.get(position);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);
        }
        if (items.contains(item)) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    boolean isPendingRemoval(int position) {
        return itemsPendingRemoval.contains(items.get(position));
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView titleTextView;
        TextView descriptionTextView;
        Button undoButton;

        ItemViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
            thumbnail = (ImageView) itemView.findViewById(R.id.file_thumbnail);
            titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            descriptionTextView = (TextView) itemView.findViewById(R.id.category_text_view);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);
        }

    }

}

