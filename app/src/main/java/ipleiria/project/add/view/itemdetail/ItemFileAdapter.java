package ipleiria.project.add.view.itemdetail;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 10-May-17.
 */

public class ItemFileAdapter extends BaseSwipeAdapter implements ItemDetailContract.FileView {

    private ItemDetailFragment.FileActionListener actionsListener;
    private ItemDetailContract.Presenter filePresenter;

    private List<ItemFile> listFiles;
    private Map<ItemFile, ImageView> attachedImageViews;
    private boolean listingDeleted;

    public ItemFileAdapter(List<ItemFile> listFiles, ItemDetailFragment.FileActionListener actionsListener, boolean listingDeleted) {
        setList(listFiles);
        this.actionsListener = actionsListener;
        this.listingDeleted = listingDeleted;

        this.attachedImageViews = new ArrayMap<>();
    }

    public void setFilePresenter(ItemDetailContract.Presenter filePresenter){
        this.filePresenter = filePresenter;
        this.filePresenter.setFileView(this);
    }

    private void setList(List<ItemFile> files){
        listFiles = files;
    }

    void replaceData(List<ItemFile> files) {
        setList(files);
        notifyDataSetChanged();
    }

    void onFileAdded(ItemFile file) {
        int pos = listFiles.indexOf(file);
        if(pos < 0){
            listFiles.add(file);
        }else{
            listFiles.remove(pos);
            listFiles.add(pos, file);
        }
        notifyDataSetChanged();
    }

    void onFileRemoved(ItemFile deletedFile) {
        listFiles.remove(deletedFile);
        removeAttachedView(deletedFile);
        notifyDataSetChanged();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.bottom_layout_actions;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        Context context = parent.getContext();
        View itemView =  LayoutInflater.from(context).inflate(R.layout.list_file_item, null);

        SwipeLayout swipeLayout = (SwipeLayout) itemView.findViewById(R.id.bottom_layout_actions);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.setClickToClose(true);

        FrameLayout itemLayout = (FrameLayout) itemView.findViewById(R.id.item_view);
        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onFileClick((ItemFile) getItem(position));
            }
        });

        ImageView buttonShare = (ImageView) itemView.findViewById(R.id.action_1);
        buttonShare.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.share_white));
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onFileClick((ItemFile) getItem(position));
            }
        });

        ImageView button1 = (ImageView) itemView.findViewById(R.id.action_2);
        ImageView button2 = (ImageView) itemView.findViewById(R.id.action_3);

        if(!listingDeleted){
            button1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.edit_white));
            button2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_white));
        }else{
            button1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.restore_white));
            button2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_forever_white));
        }

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!listingDeleted) {
                    actionsListener.onEditFile((ItemFile) getItem(position));
                }else{
                    actionsListener.onRestoreFile((ItemFile) getItem(position));
                }
                closeItem(position);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!listingDeleted) {
                    actionsListener.onDeleteFile((ItemFile) getItem(position));
                }else{
                    actionsListener.onPermanentDeleteFile((ItemFile) getItem(position));
                }
                closeItem(position);
            }
        });

        return itemView;
    }

    @Override
    public void fillValues(int position, View convertView) {
        ItemFile file = (ItemFile) getItem(position);

        TextView filename = (TextView) convertView.findViewById(R.id.filename);
        filename.setText(file.getFilename());

        ImageView thumbView = (ImageView) convertView.findViewById(R.id.file_thumbnail);
        thumbView.setImageDrawable(ContextCompat.getDrawable(convertView.getContext(), R.drawable.file_placeholder));
        attachImageViewToFile(file, thumbView);
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

    @Override
    public void setThumbnail(ItemFile file, File thumbnail){
        ImageView imageView = attachedImageViews.get(file);
        Log.d("THUMB", "Creating thumbnail..." + thumbnail.getAbsolutePath());
        Picasso.with(imageView.getContext())
                .load(thumbnail)
                .resize(100, 100)
                .placeholder(R.drawable.file_placeholder)
                .error(R.drawable.file_placeholder)
                .into(imageView);
    }

    private void attachImageViewToFile(ItemFile file, ImageView imageView){
        if(!attachedImageViews.containsKey(file)){
            attachedImageViews.put(file, imageView);
            filePresenter.createThumbnail(file);
        }
    }

    private void removeAttachedView(ItemFile file){
        attachedImageViews.remove(file);
    }
}
