package ipleiria.project.add;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 04-Apr-17.
 */

class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ItemViewHolder> {

    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec

    private ArrayList<ListItem> items;
    private ArrayList<ListItem> itemsPendingRemoval;
    private int lastInsertedIndex; // so we can add some more items for testing purposes

    private Handler handler = new Handler(); // hanlder for running delayed runnables
    private HashMap<ListItem, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be

    ListItemAdapter(ArrayList<ListItem> items) {
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
        final ListItem item = items.get(position);

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
            holder.descriptionTextView.setText(item.getCategory());
            holder.titleTextView.setVisibility(View.VISIBLE);
            holder.titleTextView.setText(item.getName());
            holder.undoButton.setVisibility(View.GONE);
            holder.undoButton.setOnClickListener(null);
        }
    }

    private Bitmap getbitpam(String path) {
        Bitmap imgthumBitmap = null;
        try {
            final int THUMBNAIL_SIZE = 64;

            FileInputStream fis = new FileInputStream(path);
            imgthumBitmap = BitmapFactory.decodeStream(fis);

            imgthumBitmap = Bitmap.createScaledBitmap(imgthumBitmap,
                    THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);

            ByteArrayOutputStream bytearroutstream = new ByteArrayOutputStream();
            imgthumBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytearroutstream);
        } catch (Exception ex) {
            Log.e("ListItemAdapter", ex.getMessage(), ex);
        }
        return imgthumBitmap;
    }

    /*public Bitmap getFileThumbnail(String path) {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        bitmapOptions.inJustDecodeBounds = true; // obtain the size of the image, without loading it in memory
        BitmapFactory.decodeFile(path, bitmapOptions);

        // find the best scaling factor for the desired dimensions
        int desiredWidth = 400;
        int desiredHeight = 300;
        float widthScale = (float) bitmapOptions.outWidth / desiredWidth;
        float heightScale = (float) bitmapOptions.outHeight / desiredHeight;
        float scale = Math.min(widthScale, heightScale);

        int sampleSize = 1;
        while (sampleSize < scale) {
            sampleSize *= 2;
        }
        bitmapOptions.inSampleSize = sampleSize; // this value must be a power of 2,
        // this is why you can not have an image scaled as you would like
        bitmapOptions.inJustDecodeBounds = false; // now we want to load the image

        // Let's load just the part of the image necessary for creating the thumbnail, not the whole image
        Bitmap thumbnail = BitmapFactory.decodeFile(path, bitmapOptions);

        // Save the thumbnail
        try {
            String fileDir = path.substring(0, path.lastIndexOf(File.separator));
            File thumbnailFile = new Fil()
            FileOutputStream fos = null;
            fos = new FileOutputStream(thumbnailFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, fos);

        // Use the thumbail on an ImageView or recycle it!
        thumbnail.recycle();
    }*/

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    void pendingRemoval(int position) {
        final ListItem item = items.get(position);
        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the item
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
        ListItem item = items.get(position);
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

