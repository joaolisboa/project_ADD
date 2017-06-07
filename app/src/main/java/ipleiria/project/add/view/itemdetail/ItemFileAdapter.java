package ipleiria.project.add.view.itemdetail;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 10-May-17.
 */

public class ItemFileAdapter extends BaseSwipeAdapter {

    private ItemDetailContract.View itemsView;
    private ItemDetailFragment.ActionListener actionsListener;

    private List<ItemFile> listFiles;
    private LinkedHashMap<ItemFile, ImageView> attachedImageViews;
    private boolean listingDeleted;

    ItemFileAdapter(List<ItemFile> listFiles, ItemDetailFragment.ActionListener actionsListener,
                    boolean listingDeleted, ItemDetailContract.View itemsView) {
        setList(listFiles);
        this.itemsView = itemsView;
        this.actionsListener = actionsListener;
        this.listingDeleted = listingDeleted;

        this.attachedImageViews = new LinkedHashMap<>();
    }

    private void setList(List<ItemFile> files) {
        listFiles = files;
    }

    void replaceData(List<ItemFile> files) {
        setList(files);
        //notifyDataSetChanged();
    }

    void onFileAdded(ItemFile file) {
        int pos = listFiles.indexOf(file);
        if (pos < 0) {
            listFiles.add(file);
        } else {
            listFiles.remove(pos);
            listFiles.add(pos, file);
        }
        //notifyDataSetChanged();
    }

    void onFileRemoved(ItemFile deletedFile) {
        removeAttachedView(deletedFile);
        listFiles.remove(deletedFile);
        //notifyDataSetChanged();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.bottom_layout_actions;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_file_item, null);

        SwipeLayout swipeLayout = (SwipeLayout) itemView.findViewById(R.id.bottom_layout_actions);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.setClickToClose(true);

        ImageView button1 = (ImageView) itemView.findViewById(R.id.action_2);
        ImageView button2 = (ImageView) itemView.findViewById(R.id.action_3);

        ImageView buttonShare = (ImageView) itemView.findViewById(R.id.action_1);
        buttonShare.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.share_white));

        if (!listingDeleted) {
            button1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.edit_white));
            button2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_white));
        } else {
            button1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.restore_white));
            button2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_forever_white));
        }

        return itemView;
    }

    @Override
    public void fillValues(final int position, View convertView) {
        ItemFile file = (ItemFile) getItem(position);

        TextView filename = (TextView) convertView.findViewById(R.id.filename);
        filename.setText(file.getFilename());

        ImageView thumbView = (ImageView) convertView.findViewById(R.id.file_thumbnail);
        //thumbView.setImageDrawable(ContextCompat.getDrawable(convertView.getContext(), R.drawable.file_placeholder));
        if (!attachedImageViews.containsValue(thumbView)) {
            ImageView currentFilePreviousThumb = attachedImageViews.get(file);
            if (currentFilePreviousThumb != null && thumbView != currentFilePreviousThumb) {
                // if item already an imageview it'll reach here and reuse the thumb
                thumbView.setImageDrawable(currentFilePreviousThumb.getDrawable());
                attachedImageViews.remove(file);
                attachedImageViews.put(file, thumbView);
            } else {
                // should only reach here on the first run
                thumbView.setImageDrawable(ContextCompat.getDrawable(convertView.getContext(), R.drawable.file_placeholder));
                attachImageViewToFile(file, thumbView);
            }
        }

        FrameLayout itemLayout = (FrameLayout) convertView.findViewById(R.id.item_view);
        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onFileClick((ItemFile) getItem(position));
            }
        });

        ImageView buttonShare = (ImageView) convertView.findViewById(R.id.action_1);
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 15-May-17 change action from ACTION_VIEW to ACTION_SEND to share file?
                actionsListener.onFileClick((ItemFile) getItem(position));
            }
        });

        ImageView button1 = (ImageView) convertView.findViewById(R.id.action_2);
        ImageView button2 = (ImageView) convertView.findViewById(R.id.action_3);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!listingDeleted) {
                    actionsListener.onEditFile((ItemFile) getItem(position));
                } else {
                    actionsListener.onRestoreFile((ItemFile) getItem(position));
                }
                closeItem(position);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!listingDeleted) {
                    actionsListener.onDeleteFile((ItemFile) getItem(position));
                } else {
                    actionsListener.onPermanentDeleteFile((ItemFile) getItem(position));
                }
                closeItem(position);
            }
        });
    }

    @Override
    public Object getItem(int position) {
        return listFiles.get(position);
    }

    @Override
    public int getCount() {
        return listFiles.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    void setThumbnail(ItemFile file, File thumbnail) {
        ImageView imageView = attachedImageViews.get(file);
        Log.d("THUMB", "Creating thumbnail... " + thumbnail.getAbsolutePath());
        imageView.setImageDrawable(Drawable.createFromPath(thumbnail.getPath()));
    }

    private void attachImageViewToFile(ItemFile file, ImageView thumbView) {
        attachedImageViews.put(file, thumbView);
        itemsView.requestThumbnail(file);
    }

    private void removeAttachedView(ItemFile file) {
        attachedImageViews.remove(file);
    }
}
