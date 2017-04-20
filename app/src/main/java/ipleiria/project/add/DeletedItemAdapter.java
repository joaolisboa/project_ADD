package ipleiria.project.add;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.dropbox.core.v2.files.Metadata;

import java.util.List;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxDeleteFile;
import ipleiria.project.add.Dropbox.DropboxMoveFile;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEODeleteFile;
import ipleiria.project.add.MEOCloud.Tasks.MEOMoveFile;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;
import ipleiria.project.add.Utils.NetworkState;
import ipleiria.project.add.Utils.RemotePath;

/**
 * Created by J on 19/04/2017.
 */

public class DeletedItemAdapter extends BaseSwipeAdapter {

    private Context context;
    private List<Item> listItems;

    public DeletedItemAdapter(Context context, List<Item> objects) {
        this.context = context;
        this.listItems = objects;
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

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.bottom_layout_actions;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.listview_deleted_item, null);
        Item item = listItems.get(position);

        SwipeLayout swipeLayout = (SwipeLayout) itemView.findViewById(R.id.bottom_layout_actions);
        FrameLayout itemLayout = (FrameLayout) itemView.findViewById(R.id.item_view);
        TextView itemName = (TextView) itemLayout.findViewById(R.id.title_text_view);
        TextView itemCriteria = (TextView) itemLayout.findViewById(R.id.category_text_view);
        ImageView itemThumbnail = (ImageView) itemLayout.findViewById(R.id.file_thumbnail);
        ImageView restoreButton = (ImageView) itemView.findViewById(R.id.restore);
        ImageView buttonDelete = (ImageView) itemView.findViewById(R.id.delete);

        itemName.setText(item.getDescription());
        itemCriteria.setText("Criteria: " + item.getCategoryReference());
        itemThumbnail.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.default_file_thumb));
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Item item = (Item)getItem(position);
                Toast.makeText(context, "click permanently delete " + item, Toast.LENGTH_SHORT).show();
                listItems.remove(item);
                FirebaseHandler.getInstance().permanentlyDeleteItem(item.getDbKey());
                if(NetworkState.isOnline(context)){
                    for(ItemFile file: item.getFiles()) {
                        if (MEOCloudClient.isClientInitialized()) {
                            new MEODeleteFile(new MEOCallback<MEOMetadata>() {
                                @Override
                                public void onComplete(MEOMetadata result) {
                                    System.out.println("file deleted :" + result.getPath());
                                }

                                @Override
                                public void onRequestError(HttpErrorException httpE) {
                                    Log.e("PermanentDelete", httpE.getMessage(), httpE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("PermanentDelete", e.getMessage(), e);
                                }
                            }).execute(RemotePath.trashPath(file));
                        }
                        if(DropboxClientFactory.isClientInitialized()){
                            new DropboxDeleteFile(DropboxClientFactory.getClient(), new DropboxDeleteFile.Callback(){

                                @Override
                                public void onDeleteComplete(Metadata result) {
                                    System.out.println(result.getName());
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("PermanentDelete", e.getMessage(), e);
                                }
                            }).execute(RemotePath.trashPath(file));
                        }
                    }
                }
                notifyDataSetChanged();
            }
        });
        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = (Item)getItem(position);
                Toast.makeText(context, "click restore " + item, Toast.LENGTH_SHORT).show();
                item.setDeleted(false);
                if(NetworkState.isOnline(context)){
                    for(ItemFile file: item.getFiles()){
                        if(MEOCloudClient.isClientInitialized()){
                            new MEOMoveFile(new MEOCallback<MEOMetadata>() {
                                @Override
                                public void onComplete(MEOMetadata result) {
                                    System.out.println("successfully moved file: " + result.getPath());
                                }

                                @Override
                                public void onRequestError(HttpErrorException httpE) {
                                    Log.e("MoveFile" , httpE.getMessage(), httpE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("MoveFile" , e.getMessage(), e);
                                }
                            }).execute(RemotePath.trashPath(file),
                                    RemotePath.getRemoteFilePath(file, item.getCriteria()));
                        }
                        if(DropboxClientFactory.isClientInitialized()){
                            new DropboxMoveFile(DropboxClientFactory.getClient(), new DropboxMoveFile.Callback(){

                                @Override
                                public void onMoveComplete(Metadata result) {
                                    System.out.println("successfully moved file: " + result.getPathLower());
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("MoveFile" , e.getMessage(), e);
                                }
                            }).execute(RemotePath.trashPath(file),
                                    RemotePath.getRemoteFilePath(file, item.getCriteria()));
                        }
                    }
                }
                FirebaseHandler.getInstance().writeItem(item);
                updateListItems(ApplicationData.getInstance().getDeletedItems());
            }
        });
        return itemView;
    }

    public void updateListItems(List<Item> items){
        this.listItems = items;
        notifyDataSetChanged();
    }

    @Override
    public void fillValues(int position, View convertView) {}
}