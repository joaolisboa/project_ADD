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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ipleiria.project.add.R;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.PendingFile;
import ipleiria.project.add.view.home.PendingActions;
import ipleiria.project.add.view.itemdetail.ItemDetailFragment;

/**
 * Created by Lisboa on 13-Jun-17.
 */

public class PendingFileAdapter extends BaseSwipeAdapter {

    private PendingActions actionsListener;
    private List<PendingFile> files;
    private List<PendingFile> selectedFiles;

    private boolean selectMode = false;
    private LinkedHashMap<PendingFile, ImageView> attachedImageViews;

    public PendingFileAdapter(List<PendingFile> files, PendingActions actionsListener) {
        this.actionsListener = actionsListener;
        this.files = files;
        this.selectedFiles = new ArrayList<>();

        this.attachedImageViews = new LinkedHashMap<>();
    }

    private void setList(List<PendingFile> files) {
        this.files = files;
    }

    public void replaceData(List<PendingFile> files) {
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

        SwipeLayout swipeLayout = itemView.findViewById(R.id.bottom_layout_actions);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.setLongClickable(true);
        swipeLayout.setClickToClose(true);

        return itemView;
    }

    @Override
    public void fillValues(final int position, final View convertView) {
        FileView fileView = new FileView(convertView);
        fileView.bindFile((PendingFile) getItem(position));
    }

    private void selectFile(PendingFile file) {
        actionsListener.onLongFileClick(file);
        closeAllItems();
    }

    public void setSelectMode(boolean selectMode){
        this.selectMode = selectMode;
    }

    public void setFileSelected(PendingFile file, boolean select) {
        if(select){
            selectedFiles.add(file);
        }else{
            selectedFiles.remove(file);
        }
        notifyDataSetChanged();
    }

    private boolean isFileSelected(PendingFile file){
        return selectedFiles.contains(file);
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
        return files.get(position).hashCode();
    }

    class FileView{

        @BindView(R.id.filename) TextView filename;
        @BindView(R.id.provider) TextView provider;
        @BindView(R.id.file_thumbnail) ImageView thumbView;

        @BindView(R.id.item_view) FrameLayout itemLayout;

        @BindView(R.id.action_1) ImageView buttonShare;
        @BindView(R.id.action_2) ImageView buttonDelete;

        View convertView;

        FileView(View view){
            ButterKnife.bind(this, view);
            this.convertView = view;
        }

        void bindFile(final PendingFile file){
            filename.setText(file.getPrettyFilename());
            thumbView.setImageDrawable(ContextCompat.getDrawable(convertView.getContext(), file.getDrawableThumb()));
            provider.setText(convertView.getContext().getString(R.string.file_provider, file.getProvider()));

            if(isFileSelected(file)){
                itemLayout.setBackgroundColor(ContextCompat.getColor(convertView.getContext(), R.color.gray_light));
            }else{
                itemLayout.setBackgroundColor(ContextCompat.getColor(convertView.getContext(), R.color.white));
            }

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectMode) {
                        selectFile(file);
                    } else {
                        actionsListener.onFileClick(file);
                    }
                }
            });

            itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    selectFile(file);
                    return true;
                }
            });

            buttonShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionsListener.onFileClick(file);
                }
            });

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionsListener.onFileDelete(file);
                    closeAllItems();
                }
            });

        }

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
