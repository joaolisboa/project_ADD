package ipleiria.project.add.view.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.view.itemdetail.ItemDetailFragment;

/**
 * Created by Lisboa on 13-Jun-17.
 */

public class PendingFileAdapter extends BaseSwipeAdapter {

    private MainFragment.PendingActionListener actionsListener;
    private MainContract.View mainView;
    private List<PendingFile> files;

    private boolean selectMode = false;

    private LinkedHashMap<PendingFile, ImageView> attachedImageViews;

    public PendingFileAdapter(List<PendingFile> files, MainFragment.PendingActionListener actionsListener,
                              MainContract.View mainView) {
        this.mainView = mainView;
        this.actionsListener = actionsListener;
        this.files = files;

        this.attachedImageViews = new LinkedHashMap<>();
    }

    private void setList(List<PendingFile> files) {
        this.files = files;
    }

    void replaceData(List<PendingFile> files) {
        setList(files);
        notifyDataSetChanged();
    }

    void onFileAdded(PendingFile file) {
        int pos = files.indexOf(file);
        if (pos < 0) {
            files.add(file);
        } else {
            files.remove(pos);
            files.add(pos, file);
        }
        notifyDataSetChanged();
    }

    void onFileRemoved(PendingFile deletedFile) {
        removeAttachedView(deletedFile);
        files.remove(deletedFile);
        notifyDataSetChanged();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.bottom_layout_actions;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.pending_list_file_item, null);

        SwipeLayout swipeLayout = (SwipeLayout) itemView.findViewById(R.id.bottom_layout_actions);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.setLongClickable(true);
        swipeLayout.setClickToClose(true);

        return itemView;
    }

    @Override
    public void fillValues(final int position, final View convertView) {
        PendingFile file = (PendingFile) getItem(position);

        TextView filename = (TextView) convertView.findViewById(R.id.filename);
        ImageView thumbView = (ImageView) convertView.findViewById(R.id.file_thumbnail);

        String name = file.getFilename();
        if (name.substring(name.lastIndexOf(".") + 1).equals("eml")) {
            filename.setText(name.substring(0, name.lastIndexOf(".")));
            thumbView.setImageDrawable(ContextCompat.getDrawable(convertView.getContext(), R.drawable.email_thumbnail));
        } else {
            filename.setText(file.getFilename());
            thumbView.setImageDrawable(ContextCompat.getDrawable(convertView.getContext(), R.drawable.file_placeholder));
            /*if (!attachedImageViews.containsValue(thumbView)) {
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
            }*/
        }


        FrameLayout itemLayout = (FrameLayout) convertView.findViewById(R.id.item_view);
        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectMode) {
                    selectFile(position, v);
                } else {
                    actionsListener.onFileClick((PendingFile) getItem(position));
                }
            }
        });

        itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectFile(position, v);
                return true;
            }
        });

        ImageView buttonShare = (ImageView) convertView.findViewById(R.id.action_1);
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onFileClick((PendingFile) getItem(position));
            }
        });

        ImageView buttonDelete = (ImageView) convertView.findViewById(R.id.action_2);

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onFileDelete((PendingFile) getItem(position));
                closeItem(position);
            }
        });
    }

    private void selectFile(int position, View view) {
        actionsListener.onLongFileClick((PendingFile) getItem(position), view);
        closeItem(position);
    }

    public void setSelectMode(boolean selectMode){
        this.selectMode = selectMode;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    void setThumbnail(PendingFile file, File thumbnail) {
        ImageView imageView = attachedImageViews.get(file);
        Log.d("THUMB", "Creating thumbnail... " + thumbnail.getAbsolutePath());
        imageView.setImageDrawable(Drawable.createFromPath(thumbnail.getPath()));
    }

    private void attachImageViewToFile(PendingFile file, ImageView thumbView) {
        attachedImageViews.put(file, thumbView);
        //mainView.requestThumbnail(file);
    }

    private void removeAttachedView(PendingFile file) {
        attachedImageViews.remove(file);
    }

}
