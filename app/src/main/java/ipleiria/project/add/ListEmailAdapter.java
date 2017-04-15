package ipleiria.project.add;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.Email;

/**
 * Created by Lisboa on 15-Apr-17.
 */

class ListEmailAdapter extends RecyclerView.Adapter<ListEmailAdapter.ItemViewHolder> {

    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec

    private Context context;

    private List<Email> items;
    private List<Email> itemsPendingRemoval;
    private int lastInsertedIndex; // so we can add some more items, not implemented

    private Handler handler = new Handler(); // hanlder for running delayed runnables
    private HashMap<Email, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be

    ListEmailAdapter(Context context, List<Email> items) {
        this.items = items;
        this.context = context;
        itemsPendingRemoval = new LinkedList<>();
        lastInsertedIndex = items.size();
    }

    @Override
    public ListEmailAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ListEmailAdapter.ItemViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(ListEmailAdapter.ItemViewHolder holder, int position) {
        final Email item = items.get(position);

        if (itemsPendingRemoval.contains(item)) {
            // we need to show the "undo" state of the row
            holder.itemView.setBackgroundColor(Color.RED);
            holder.emailTextView.setVisibility(View.GONE);
            holder.emailStatus.setVisibility(View.GONE);
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
            holder.emailStatus.setVisibility(View.VISIBLE);
            holder.emailStatus.setImageDrawable(ContextCompat.getDrawable(context,
                    item.isVerified() ? R.drawable.circle_valid_status : R.drawable.circle_invalid_status));
            holder.emailTextView.setVisibility(View.VISIBLE);
            holder.emailTextView.setText(item.getEmail());
            holder.undoButton.setVisibility(View.GONE);
            holder.undoButton.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void pendingRemoval(int position) {
        final Email item = items.get(position);
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
        Email item = items.get(position);
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

        ImageView emailStatus;
        TextView emailTextView;
        Button undoButton;

        ItemViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.email_list_item, parent, false));
            emailStatus = (ImageView) itemView.findViewById(R.id.email_status);
            emailTextView = (TextView) itemView.findViewById(R.id.email);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);
        }

    }
}