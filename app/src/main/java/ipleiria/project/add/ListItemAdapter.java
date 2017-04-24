package ipleiria.project.add;

import android.content.Context;
import android.content.Intent;
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
import ipleiria.project.add.MEOCloud.Tasks.MEOCreateFolder;
import ipleiria.project.add.MEOCloud.Tasks.MEODeleteFile;
import ipleiria.project.add.MEOCloud.Tasks.MEOMoveFile;
import ipleiria.project.add.MEOCloud.Tasks.MEOUploadFile;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;
import ipleiria.project.add.Utils.NetworkState;
import ipleiria.project.add.Utils.RemotePath;

import static ipleiria.project.add.Utils.RemotePath.TRASH_FOLDER;

/**
 * Created by Lisboa on 18-Apr-17.
 */

public class ListItemAdapter extends BaseSwipeAdapter {

    private Context context;
    private List<Item> listItems;
    private boolean listDeleted;
    private String action;

    public ListItemAdapter(Context context, List<Item> objects, boolean listDeleted, String action) {
        this.context = context;
        this.listItems = objects;
        this.listDeleted = listDeleted;
        this.action = action;
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
        View itemView = LayoutInflater.from(context).inflate(R.layout.listview_item, null);
        Item item = listItems.get(position);

        SwipeLayout swipeLayout = (SwipeLayout) itemView.findViewById(R.id.bottom_layout_actions);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.setClickToClose(true);
        if(action != null){
            swipeLayout.setSwipeEnabled(false);
        }
        FrameLayout itemLayout = (FrameLayout) itemView.findViewById(R.id.item_view);
        TextView itemName = (TextView) itemLayout.findViewById(R.id.title_text_view);
        TextView itemCriteria = (TextView) itemLayout.findViewById(R.id.category_text_view);
        TextView numFiles = (TextView) itemLayout.findViewById(R.id.num_files);
        ImageView infoIcon = (ImageView) itemLayout.findViewById(R.id.info_icon);

        itemName.setText(item.getDescription());
        itemCriteria.setText(item.getCategoryReference() + ". " + item.getCriteria().getName());
        numFiles.setText(context.getString(R.string.num_files, item.getFiles().size()));

        infoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Item item = (Item) getItem(position);
                Intent intent = new Intent(context, ItemDetailActivity.class);
                intent.putExtra("item_key", item.getDbKey());
                context.startActivity(intent);
            }
        });

        if(!listDeleted){
            setDefaultListeners(position, itemView);
        }else{
            setListenerForDeletedItems(position, itemView);
        }

        return itemView;
    }

    private void setDefaultListeners(final int position, View itemView){
        ImageView buttonEdit = (ImageView) itemView.findViewById(R.id.action_1);
        buttonEdit.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.edit_icon));
        ImageView buttonDelete = (ImageView) itemView.findViewById(R.id.action_2);
        buttonDelete.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_item_icon));

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Item item = (Item) getItem(position);
                Toast.makeText(context, "click delete " + item, Toast.LENGTH_SHORT).show();
                ApplicationData.getInstance().deleteItem(item);
                if (NetworkState.isOnline(context)) {
                    for (final ItemFile file : item.getFiles()) {
                        if (MEOCloudClient.isClientInitialized()) {
                            new MEOCreateFolder(new MEOCallback<MEOMetadata>() {
                                @Override
                                public void onComplete(MEOMetadata result) {
                                    new MEOMoveFile(new MEOCallback<MEOMetadata>() {
                                        @Override
                                        public void onComplete(MEOMetadata result) {
                                            System.out.println("successfully moved file: " + result.getPath());
                                        }

                                        @Override
                                        public void onRequestError(HttpErrorException httpE) {
                                            Log.e("MoveFile", httpE.getMessage(), httpE);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Log.e("MoveFile", e.getMessage(), e);
                                        }
                                    }).execute(RemotePath.getRemoteFilePath(file, item.getCriteria()),
                                            RemotePath.trashPath(file));
                                }

                                @Override
                                public void onRequestError(HttpErrorException httpE) {
                                    Log.e("UploadDropError", httpE.getMessage(), httpE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("UploadDropError", e.getMessage(), e);
                                }
                            }).execute(TRASH_FOLDER);

                        }
                        if (DropboxClientFactory.isClientInitialized()) {
                            new DropboxMoveFile(DropboxClientFactory.getClient(), new DropboxMoveFile.Callback() {

                                @Override
                                public void onMoveComplete(Metadata result) {
                                    System.out.println("successfully moved file: " + result.getPathLower());
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("MoveFile", e.getMessage(), e);
                                }
                            }).execute(RemotePath.getRemoteFilePath(file, item.getCriteria()),
                                    RemotePath.trashPath(file));
                        }
                    }
                }
                notifyDataSetChanged();
            }
        });
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = (Item) getItem(position);
                Toast.makeText(context, "click edit " + item, Toast.LENGTH_SHORT).show();
                Intent editIntent = new Intent(context, AddItemActivity.class);
                editIntent.putExtra("itemKey", item.getDbKey());
                context.startActivity(editIntent);
                notifyDataSetChanged();
            }
        });
    }

    private void setListenerForDeletedItems(final int position, View itemView) {
        ImageView restoreButton = (ImageView) itemView.findViewById(R.id.action_1);
        restoreButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.restore_item_white));
        ImageView buttonDelete = (ImageView) itemView.findViewById(R.id.action_2);
        buttonDelete.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_forever_white));

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Item item = (Item) getItem(position);
                Toast.makeText(context, "click permanently delete " + item, Toast.LENGTH_SHORT).show();
                listItems.remove(item);
                ApplicationData.getInstance().permanentlyDeleteItem(item);
                if (NetworkState.isOnline(context)) {
                    for (ItemFile file : item.getFiles()) {
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
                        if (DropboxClientFactory.isClientInitialized()) {
                            new DropboxDeleteFile(DropboxClientFactory.getClient(), new DropboxDeleteFile.Callback() {

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
                Item item = (Item) getItem(position);
                Toast.makeText(context, "click restore " + item, Toast.LENGTH_SHORT).show();
                if (NetworkState.isOnline(context)) {
                    for (ItemFile file : item.getFiles()) {
                        if (MEOCloudClient.isClientInitialized()) {
                            new MEOMoveFile(new MEOCallback<MEOMetadata>() {
                                @Override
                                public void onComplete(MEOMetadata result) {
                                    System.out.println("successfully moved file: " + result.getPath());
                                }

                                @Override
                                public void onRequestError(HttpErrorException httpE) {
                                    Log.e("MoveFile", httpE.getMessage(), httpE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("MoveFile", e.getMessage(), e);
                                }
                            }).execute(RemotePath.trashPath(file),
                                    RemotePath.getRemoteFilePath(file, item.getCriteria()));
                        }
                        if (DropboxClientFactory.isClientInitialized()) {
                            new DropboxMoveFile(DropboxClientFactory.getClient(), new DropboxMoveFile.Callback() {

                                @Override
                                public void onMoveComplete(Metadata result) {
                                    System.out.println("successfully moved file: " + result.getPathLower());
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("MoveFile", e.getMessage(), e);
                                }
                            }).execute(RemotePath.trashPath(file),
                                    RemotePath.getRemoteFilePath(file, item.getCriteria()));
                        }
                    }
                }
                ApplicationData.getInstance().restoreItem(item);
                updateListItems(ApplicationData.getInstance().getDeletedItems());
            }
        });
    }

    public void updateListItems(List<Item> items) {
        this.listItems = items;
        notifyDataSetChanged();
    }

    @Override
    public void fillValues(int position, View convertView) {
    }
}