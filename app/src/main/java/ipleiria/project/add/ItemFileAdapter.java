package ipleiria.project.add;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.media.Image;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.dropbox.core.v2.files.Metadata;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.LinkedList;
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
import ipleiria.project.add.Model.Email;
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
        this.listItemsFiles = item.getFiles(listDeleted);
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
    public void fillValues(int position, View convertView) {
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View itemView =  LayoutInflater.from(context).inflate(R.layout.list_file_item, null);
        final ItemFile itemFile = listItemsFiles.get(position);

        SwipeLayout swipeLayout = (SwipeLayout) itemView.findViewById(R.id.bottom_layout_actions);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.setClickToClose(true);

        FrameLayout itemLayout = (FrameLayout) itemView.findViewById(R.id.item_view);
        TextView filename = (TextView) itemLayout.findViewById(R.id.filename);
        filename.setText(itemFile.getFilename());
        final ImageView fileThumb = (ImageView) itemLayout.findViewById(R.id.file_thumbnail);
        setThumbnail(itemFile, fileThumb);

        if (!listDeleted) {
            setDefaultListeners(position, itemView);
        } else {
            setListenerForDeletedItems(position, itemView);
        }
        return itemView;
    }

    private void downloadThumbnailFromCloud(final ImageView fileThumb, ItemFile itemFile) {
        if (NetworkState.isOnline(context)) {
            if (MEOCloudClient.isClientInitialized()) {
                new MEOGetThumbnail(context, new MEOCallback<File>() {
                    @Override
                    public void onComplete(File result) {
                        Log.d("THUMBNAIL", "Loading thumbnail from MEOCloud");
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
            } else if (DropboxClientFactory.isClientInitialized()) {
                new DropboxGetThumbnail(context, DropboxClientFactory.getClient(),
                        new DropboxGetThumbnail.Callback() {
                            @Override
                            public void onDownloadComplete(File result) {
                                Log.d("THUMBNAIL", "Loading thumbnail from Dropbox");
                                Picasso.with(context)
                                        .load(Uri.fromFile(result))
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
    }

    private void renameFileThumbnail(String from, String to){
        File localFileThumb = new File(context.getFilesDir().getAbsolutePath() + "/thumb_" + from);
        if(localFileThumb.exists()) {
            boolean success = localFileThumb.renameTo(new File(context.getFilesDir().getAbsolutePath() + "/thumb_" + to));
            Log.d("THUMBNAIL", "thumbnail rename successful?: " + success);
        }
    }

    private void deleteFileThumbnail(ItemFile itemFile){
        File localFileThumb = new File(context.getFilesDir().getAbsolutePath() + "/thumb_" + itemFile.getFilename());
        if(localFileThumb.exists()){
            boolean success = localFileThumb.delete();
            Log.d("THUMBNAIL", "thumbnail delete successful?: " + success);
        }
    }

    private void setThumbnail(final ItemFile itemFile, final ImageView fileThumb){
        File localFileThumb = new File(context.getFilesDir().getAbsolutePath() + "/thumb_" + itemFile.getFilename());
        if (localFileThumb.exists()) {
            Log.d("THUMBNAIL", "local thumbnail exists, opening local thumb");
            Picasso.with(context)
                    .load(localFileThumb)
                    .resize(100, 100)
                    .placeholder(R.drawable.file_placeholder)
                    .error(R.drawable.file_placeholder)
                    .into(fileThumb, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("THUMBNAIL", "Successfully created thumbnail from local file");
                        }

                        @Override
                        public void onError() {
                            Log.d("THUMBNAIL", "Failed to create thumbnail from local file, downloading...");
                            downloadThumbnailFromCloud(fileThumb, itemFile);
                        }
                    });
        } else {
            downloadThumbnailFromCloud(fileThumb, itemFile);
        }
    }

    private void setDefaultListeners(final int position, final View itemView) {
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
                if (NetworkState.isOnline(context)) {
                    if (MEOCloudClient.isClientInitialized()) {
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
                    } else if (DropboxClientFactory.isClientInitialized()) {
                        new DropboxDownloadFile(context, DropboxClientFactory.getClient(),
                                new DropboxDownloadFile.Callback() {

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
                } else {
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ItemFile itemfile = (ItemFile) getItem(position);
                Toast.makeText(context, "click delete " + item, Toast.LENGTH_SHORT).show();
                item.deleteFile(itemfile);
                if (NetworkState.isOnline(context)) {
                    if (MEOCloudClient.isClientInitialized()) {
                        // meo doesn't create folder so we need to ensure it's created/exists
                        new MEOCreateFolder(new MEOCallback<MEOMetadata>() {
                            @Override
                            public void onComplete(MEOMetadata result) {
                                moveFileMEO(RemotePath.getRemoteFilePath(itemfile, item.getCriteria()),
                                        RemotePath.trashPath(itemfile));
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
                        moveFileDropbox(RemotePath.getRemoteFilePath(itemfile, item.getCriteria()),
                                RemotePath.trashPath(itemfile));
                    }
                }
                ItemDetailActivity act = (ItemDetailActivity) context;
                act.setFileListView();
            }
        });
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ItemFile file = (ItemFile) getItem(position);
                View view = View.inflate(context, R.layout.rename_file_dialog, null);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setView(view)
                        .setTitle("Rename file")
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();

                final EditText input = (EditText) view.findViewById(R.id.new_filename);
                input.setText(file.getFilename());
                input.setSelection(file.getFilename().lastIndexOf("."));
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                String newFilename = input.getText().toString();
                                String oldFilename = file.getFilename();
                                file.setFilename(newFilename);
                                if (NetworkState.isOnline(context)) {
                                    if (MEOCloudClient.isClientInitialized()) {
                                        moveFileMEO(RemotePath.getRemoteFilePath(oldFilename, item.getCriteria()),
                                                RemotePath.getRemoteFilePath(newFilename, item.getCriteria()));
                                    }
                                    if (DropboxClientFactory.isClientInitialized()) {
                                        moveFileDropbox(RemotePath.getRemoteFilePath(oldFilename, item.getCriteria()),
                                                RemotePath.getRemoteFilePath(newFilename, item.getCriteria()));
                                    }
                                }
                                FirebaseHandler.getInstance().writeItem(item);
                                renameFileThumbnail(oldFilename, newFilename);
                                dialog.dismiss();
                                closeAllItems();
                                FrameLayout itemLayout = (FrameLayout) itemView.findViewById(R.id.item_view);
                                TextView filename = (TextView) itemLayout.findViewById(R.id.filename);
                                filename.setText(file.getFilename());
                                final ImageView fileThumb = (ImageView) itemLayout.findViewById(R.id.file_thumbnail);
                                setThumbnail(file, fileThumb);
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
    }

    private void moveFileMEO(String from, String to) {
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
        }).execute(from, to);
    }

    private void moveFileDropbox(String from, String to) {
        new DropboxMoveFile(DropboxClientFactory.getClient(), new DropboxMoveFile.Callback() {

            @Override
            public void onMoveComplete(Metadata result) {
                System.out.println("successfully moved file: " + result.getPathLower());
            }

            @Override
            public void onError(Exception e) {
                Log.e("MoveFile", e.getMessage(), e);
            }
        }).execute(from, to);
    }

    private void shareFile(File file) {
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
        ImageView buttonShare = (ImageView) itemView.findViewById(R.id.action_1);
        buttonShare.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.share_white));
        ImageView restoreButton = (ImageView) itemView.findViewById(R.id.action_2);
        restoreButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.restore_item_white));
        ImageView buttonDelete = (ImageView) itemView.findViewById(R.id.action_3);
        buttonDelete.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.delete_forever_white));

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ItemFile file = (ItemFile) getItem(position);
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Downloading file");
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                if (NetworkState.isOnline(context)) {
                    if (MEOCloudClient.isClientInitialized()) {
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
                        }).execute(RemotePath.trashPath(file));
                    } else if (DropboxClientFactory.isClientInitialized()) {
                        new DropboxDownloadFile(context, DropboxClientFactory.getClient(),
                                new DropboxDownloadFile.Callback() {
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
                        }).execute(RemotePath.trashPath(file));
                    }
                } else {
                    Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemFile itemFile = (ItemFile) getItem(position);
                Toast.makeText(context, "click permanently delete " + item, Toast.LENGTH_SHORT).show();
                listItemsFiles.remove(itemFile);

                if (NetworkState.isOnline(context)) {
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
                        }).execute(RemotePath.trashPath(itemFile));
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
                        }).execute(RemotePath.trashPath(itemFile));
                    }
                }
                deleteFileThumbnail(itemFile);
                item.permanentlyDeleteFile(itemFile);
                ItemDetailActivity act = (ItemDetailActivity) context;
                act.setFileListView();
            }
        });

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemFile itemFile = (ItemFile) getItem(position);
                Toast.makeText(context, "click restore " + item, Toast.LENGTH_SHORT).show();
                if (NetworkState.isOnline(context)) {
                    if (MEOCloudClient.isClientInitialized()) {
                        moveFileMEO(RemotePath.trashPath(itemFile),
                                RemotePath.getRemoteFilePath(itemFile, item.getCriteria()));
                    }
                    if (DropboxClientFactory.isClientInitialized()) {
                        moveFileDropbox(RemotePath.trashPath(itemFile),
                                RemotePath.getRemoteFilePath(itemFile, item.getCriteria()));
                    }
                }
                item.restoreFile(itemFile);
                ItemDetailActivity act = (ItemDetailActivity) context;
                act.setFileListView();
            }
        });
    }

}