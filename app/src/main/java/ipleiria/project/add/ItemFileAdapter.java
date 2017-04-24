package ipleiria.project.add;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.dropbox.core.v2.files.Metadata;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxDeleteFile;
import ipleiria.project.add.Dropbox.DropboxDownloadFile;
import ipleiria.project.add.Dropbox.DropboxGetThumbnail;
import ipleiria.project.add.Dropbox.DropboxMoveFile;
import ipleiria.project.add.MEOCloud.Data.FileResponse;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOCreateFolder;
import ipleiria.project.add.MEOCloud.Tasks.MEODeleteFile;
import ipleiria.project.add.MEOCloud.Tasks.MEODownloadFile;
import ipleiria.project.add.MEOCloud.Tasks.MEOGetThumbnail;
import ipleiria.project.add.MEOCloud.Tasks.MEOMoveFile;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Item;
import ipleiria.project.add.Model.ItemFile;
import ipleiria.project.add.Utils.CircleTransformation;
import ipleiria.project.add.Utils.NetworkState;
import ipleiria.project.add.Utils.RemotePath;
import ipleiria.project.add.Utils.UriHelper;

import static ipleiria.project.add.Utils.RemotePath.TRASH_FOLDER;

/**
 * Created by J on 24/04/2017.
 */

public class ItemFileAdapter extends BaseSwipeAdapter {

    private Context context;
    private Item item;
    private List<ItemFile> listItemsFiles;
    private boolean listDeleted;

    public ItemFileAdapter(Context context, Item item, boolean listDeleted) {
        this.context = context;
        this.item = item;
        this.listItemsFiles = item.getFiles();
        this.listDeleted = listDeleted;
    }

    @Override
    public int getCount() {
        return listItemsFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return listItemsFiles.get(position);
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
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_file_item, null);
        ItemFile itemFile = listItemsFiles.get(position);

        SwipeLayout swipeLayout = (SwipeLayout) itemView.findViewById(R.id.bottom_layout_actions);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.setClickToClose(true);
        FrameLayout itemLayout = (FrameLayout) itemView.findViewById(R.id.item_view);
        final ImageView fileThumb = (ImageView) itemLayout.findViewById(R.id.file_thumbnail);
        TextView filename = (TextView) itemLayout.findViewById(R.id.filename);

        filename.setText(itemFile.getFilename());
        // TODO: 24/04/2017 get file thumbnail from drop/meo and set with Picasso
        if(NetworkState.isOnline(context)){
            if(MEOCloudClient.isClientInitialized()){
                new MEOGetThumbnail(context, new MEOCallback<File>() {
                    @Override
                    public void onComplete(File result) {
                        Picasso.with(context)
                                .load(result)
                                .resize(100, 100)
                                .placeholder(R.drawable.file_placeholder)
                                .error(R.drawable.file_placeholder)
                                .into(fileThumb);
                    }

                    @Override
                    public void onRequestError(HttpErrorException httpE) {
                        Log.e("DownloadError", httpE.getMessage(), httpE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("DownloadError", e.getMessage(), e);
                    }
                }).execute(RemotePath.getRemoteFilePath(itemFile, item.getCriteria()), null, MEOCloudAPI.THUMBNAIL_SIZE_M);
            } else if(DropboxClientFactory.isClientInitialized()){
                new DropboxGetThumbnail(context, DropboxClientFactory.getClient(),
                        new DropboxGetThumbnail.Callback(){
                            @Override
                            public void onDownloadComplete(File result) {
                                Picasso.with(context)
                                        .load(result)
                                        .resize(100, 100)
                                        .placeholder(R.drawable.file_placeholder)
                                        .error(R.drawable.file_placeholder)
                                        .into(fileThumb);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("DownloadError", e.getMessage(), e);
                            }
                        }).execute(RemotePath.getRemoteFilePath(itemFile, item.getCriteria()));
            }
        }

        if(!listDeleted){
            setDefaultListeners(position, itemView);
        }else{
            setListenerForDeletedItems(position, itemView);
        }

        return itemView;
    }

    private void setDefaultListeners(final int position, View itemView){
        ImageView buttonShare = (ImageView) itemView.findViewById(R.id.action_1);
        buttonShare.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.share_white));
        ImageView buttonEdit = (ImageView) itemView.findViewById(R.id.action_2);
        buttonEdit.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.edit_icon));
        ImageView buttonDelete = (ImageView) itemView.findViewById(R.id.action_3);
        buttonDelete.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_item_icon));

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ItemFile file = (ItemFile) getItem(position);
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Downloading file");
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                if(NetworkState.isOnline(context)){
                    if(MEOCloudClient.isClientInitialized()){
                        new MEODownloadFile(context, new MEOCallback<FileResponse>() {
                            @Override
                            public void onComplete(FileResponse result) {
                                progressDialog.dismiss();
                                shareFile(result);
                            }

                            @Override
                            public void onRequestError(HttpErrorException httpE) {
                                progressDialog.dismiss();
                                Log.e("DownloadError", httpE.getMessage(), httpE);
                            }

                            @Override
                            public void onError(Exception e) {
                                progressDialog.dismiss();
                                Log.e("DownloadError", e.getMessage(), e);
                            }
                        }).execute(RemotePath.getRemoteFilePath(file, item.getCriteria()));
                    } else if(DropboxClientFactory.isClientInitialized()){
                        new DropboxDownloadFile(context, DropboxClientFactory.getClient(),
                                new DropboxDownloadFile.Callback(){

                                    @Override
                                    public void onDownloadComplete(File result) {
                                        progressDialog.dismiss();
                                        shareFile(result);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        progressDialog.dismiss();
                                        Log.e("DownloadError", e.getMessage(), e);
                                    }
                                }).execute(RemotePath.getRemoteFilePath(file, item.getCriteria()));
                    }
                }else{
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

    private void shareFile(File file){
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_VIEW);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setDataAndType(UriHelper.getUriFromAppfile(file.getPath()), type);
        context.startActivity(Intent.createChooser(shareIntent, "Open file"));
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
                listItemsFiles.remove(item);
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
                //updateListItems(ApplicationData.getInstance().getDeletedItems());
            }
        });
    }

    public void updateListItems(List<ItemFile> itemFiles) {
        this.listItemsFiles = itemFiles;
        notifyDataSetChanged();
    }

    @Override
    public void fillValues(int position, View convertView) {
    }
}