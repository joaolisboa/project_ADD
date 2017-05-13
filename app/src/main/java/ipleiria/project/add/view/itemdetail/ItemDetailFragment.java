package ipleiria.project.add.view.itemdetail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.utils.UriHelper;
import ipleiria.project.add.view.itemdetail.ItemFileAdapter;
import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.view.add_edit_item.AddEditActivity;
import ipleiria.project.add.view.items.ItemAdapter;
import ipleiria.project.add.view.items.ItemsFragment;
import ipleiria.project.add.view.items.ScrollChildSwipeRefreshLayout;

import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.EDITING_ITEM;
import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.EDITING_ITEM_KEY;
import static ipleiria.project.add.view.items.ItemsPresenter.LIST_DELETED_KEY;

/**
 * Created by Lisboa on 10-May-17.
 */

public class ItemDetailFragment extends Fragment implements ItemDetailContract.View {

    private ItemDetailContract.Presenter itemDetailPresenter;

    private ProgressDialog progressDialog;
    private ItemFileAdapter listFileAdapter;

    private TextView filesHeader;

    public ItemDetailFragment() {
    }

    public static ItemDetailFragment newInstance() {
        return new ItemDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean listDeleted = getActivity().getIntent().getBooleanExtra(LIST_DELETED_KEY, false);
        listFileAdapter = new ItemFileAdapter(new LinkedList<ItemFile>(), fileActionListener, listDeleted, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.item_detail_frag, container, false);

        filesHeader = (TextView) root.findViewById(R.id.file_label_subheader);

        // Set up files list
        ListView listView = (ListView) root.findViewById(R.id.listview);
        listView.setAdapter(listFileAdapter);

        // Set up floating action button
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_add);
        fab.setImageResource(R.drawable.add_white);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // take photo and add to current item
            }
        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        itemDetailPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        itemDetailPresenter.unsubscribe();
    }

    @Override
    public void setPresenter(ItemDetailContract.Presenter presenter) {
        this.itemDetailPresenter = presenter;
    }

    @Override
    public void setFileThumbnail(ItemFile file, File thumbnail) {
        listFileAdapter.setThumbnail(file, thumbnail);
    }

    @Override
    public void requestThumbnail(ItemFile file) {
        itemDetailPresenter.createThumbnail(file);
    }

    @Override
    public void openFileShare(File file) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);

        // TODO: 13-May-17 android never seems to delete temp files or files with deleteOnExit - fix 
        Intent shareIntent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = UriHelper.getUriFromAppfile(file.getName());
        shareIntent.setDataAndType(fileUri, type);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Open file"));
    }

    @Override
    public void showItemInfo(Item item) {
        // set item info in views
    }

    @Override
    public void showFiles(List<ItemFile> files) {
        listFileAdapter.replaceData(files);

        filesHeader.setVisibility(View.VISIBLE);
    }

    @Override
    public void showNoFiles() {
        filesHeader.setVisibility(View.GONE);
    }

    @Override
    public void showAddedFile(ItemFile file) {
        listFileAdapter.onFileAdded(file);
        filesHeader.setVisibility(View.VISIBLE);
    }

    @Override
    public void removeDeletedFile(ItemFile deletedFile) {
        listFileAdapter.onFileRemoved(deletedFile);
        itemDetailPresenter.checkForEmptyList();
    }

    @Override
    public void showLoadingIndicator() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    @Override
    public void hideLoadingIndicator() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

    private void showEditFileDialog(final ItemFile file) {
        View view = View.inflate(getContext(), R.layout.rename_file_dialog, null);
        final EditText input = (EditText) view.findViewById(R.id.new_filename);
        input.setText(file.getFilename());
        input.setSelection(file.getFilename().lastIndexOf("."));

        final InputMethodManager inputMethodManager = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle("Rename file")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemDetailPresenter.renameFile(file, input.getText().toString());
                        inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        dialog.cancel();
                    }
                })
                .create()
                .show();


    }

    FileActionListener fileActionListener = new FileActionListener() {

        @Override
        public void onFileClick(ItemFile clickedFile) {
            itemDetailPresenter.onItemClicked(clickedFile);
        }

        @Override
        public void onDeleteFile(ItemFile deletedFile) {
            itemDetailPresenter.deleteFile(deletedFile);
        }

        @Override
        public void onPermanentDeleteFile(ItemFile deletedFile) {
            itemDetailPresenter.permanentlyDeleteFile(deletedFile);
        }

        @Override
        public void onEditFile(ItemFile editedFile) {
            showEditFileDialog(editedFile);
        }

        @Override
        public void onRestoreFile(ItemFile restoredFile) {
            itemDetailPresenter.restoreFile(restoredFile);
        }
    };

    interface FileActionListener {

        void onFileClick(ItemFile clickedFile);

        void onDeleteFile(ItemFile deletedFile);

        void onPermanentDeleteFile(ItemFile deletedFile);

        void onEditFile(ItemFile editedFile);

        void onRestoreFile(ItemFile restoredFile);
    }
}
