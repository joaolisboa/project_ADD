package ipleiria.project.add.view.itemdetail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.utils.UriHelper;
import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.view.add_edit_item.AddEditActivity;

import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.EDITING_ITEM;
import static ipleiria.project.add.view.add_edit_item.AddEditPresenter.EDITING_ITEM_KEY;
import static ipleiria.project.add.view.items.ItemsFragment.REQUEST_ITEM_EDIT;
import static ipleiria.project.add.view.items.ItemsPresenter.LIST_DELETED_KEY;

/**
 * Created by Lisboa on 10-May-17.
 */

public class ItemDetailFragment extends Fragment implements ItemDetailContract.View {

    private static final String TAG = "ITEM_DETAIL_ACTIVITY";

    private ItemDetailContract.Presenter itemDetailPresenter;

    private ItemFileAdapter listFileAdapter;

    private ProgressDialog progressDialog;
    private TextView filesHeader;
    private ChipsInput chipsInput;

    private TextView descriptionTextView;
    private TextView weightTextView;

    private ListView fileList;

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
        chipsInput = (ChipsInput) root.findViewById(R.id.chips_input);
        descriptionTextView = (TextView) root.findViewById(R.id.description);
        weightTextView = (TextView) root.findViewById(R.id.weight);

        // Set up files list
        fileList = (ListView) root.findViewById(R.id.file_list);
        fileList.setAdapter(listFileAdapter);

        if(getActivity().getIntent().getBooleanExtra(LIST_DELETED_KEY, false)){
            root.findViewById(R.id.text_helper).setVisibility(View.VISIBLE);
        }

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

    // when the list doesn't fit in the full layout view, it'll use it's own scrollview
    // using it's own scrollview means that the layout doesn't scroll and only the view
    // setting the height of the list to the real size means we can scroll using the layout scrollview
    public void setListViewHeightBasedOnItems() {
        ListAdapter listAdapter = fileList.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, fileList);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = fileList.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = fileList.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            fileList.setLayoutParams(params);
            fileList.requestLayout();
        } else {
            Log.d(TAG, "List has no adapter");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                itemDetailPresenter.onEditItemClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        itemDetailPresenter.subscribe();
    }

    @Override
    public void onStop() {
        super.onStop();
        itemDetailPresenter.unsubscribe();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        itemDetailPresenter.onResult(requestCode, resultCode, data);
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
    public void openFileShare(String filePath) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = filePath.substring(filePath.indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);
        if(ext.equals("eml")){
            type = "message/rfc822";
        }

        Intent shareIntent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = UriHelper.getUriFromAppfile(filePath);
        shareIntent.setDataAndType(fileUri, type);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Open file"));
    }

    @Override
    public void showTags(List<String> tags, List<String> suggestions) {
        // add existing tags to view
        for (String tag : tags) {
            chipsInput.addChip(tag, null);
        }
        // create suggestions
        List<TagChip> suggestionChips = new ArrayList<>();
        for (String tag : suggestions) {
            suggestionChips.add(new TagChip(tag));
        }
        chipsInput.setFilterableList(suggestionChips);
        chipsInput.addChipsListener(chipsListener);
        chipsInput.setShowChipDetailed(false);

        // doesn't work ?
        /*chipsInput.setChipValidator(new ChipsInput.ChipValidator() {
            @Override
            public boolean areEquals(ChipInterface chipInterface, ChipInterface chipInterface1) {
                String obj1 = StringUtils.replaceDiacriticalMarks(chipInterface.getLabel()).toLowerCase();
                String obj2 = StringUtils.replaceDiacriticalMarks(chipInterface1.getLabel()).toLowerCase();
                return obj1.contains(obj2);
            }
        });*/

        chipsInput.setVisibility(View.VISIBLE);
    }

    @Override
    public void showNoTags() {
        chipsInput.setVisibility(View.GONE);
    }

    @Override
    public void openEditItemView(Item item) {
        Intent intent = new Intent(getContext(), AddEditActivity.class);
        intent.setAction(EDITING_ITEM);
        intent.putExtra(EDITING_ITEM_KEY, item.getDbKey());
        startActivityForResult(intent, REQUEST_ITEM_EDIT);
    }

    @Override
    public void showItemInfo(Item item) {
        descriptionTextView.setText(item.getDescription());
        weightTextView.setText("Weight: " + item.getWeight());
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(item.getDescription());
    }

    @Override
    public void showFiles(List<ItemFile> files) {
        listFileAdapter.replaceData(files);

        filesHeader.setVisibility(View.VISIBLE);
        setListViewHeightBasedOnItems();
    }

    @Override
    public void showNoFiles() {
        filesHeader.setVisibility(View.GONE);
    }

    @Override
    public void showAddedFile(ItemFile file) {
        listFileAdapter.onFileAdded(file);
        filesHeader.setVisibility(View.VISIBLE);
        setListViewHeightBasedOnItems();
    }

    @Override
    public void removeDeletedFile(ItemFile deletedFile) {
        listFileAdapter.onFileRemoved(deletedFile);
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
            progressDialog.dismiss();
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

    ChipsInput.ChipsListener chipsListener = new ChipsInput.ChipsListener() {
        @Override
        public void onChipAdded(ChipInterface chipInterface, int i) {
            itemDetailPresenter.addTag(chipInterface.getLabel());
        }

        @Override
        public void onChipRemoved(ChipInterface chipInterface, int i) {
            itemDetailPresenter.removeTag(chipInterface.getLabel());
        }

        @Override
        public void onTextChanged(CharSequence charSequence) {
            if (charSequence.length() > 1 && charSequence.charAt(charSequence.length() - 1) == ',') {
                String tag = charSequence.subSequence(0, charSequence.length() - 1).toString();
                chipsInput.addChip(tag, null);
            }
        }
    };

    ActionListener<ItemFile> fileActionListener = new ActionListener<ItemFile>() {

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

    interface ActionListener<T> {

        void onFileClick(T clickedFile);

        void onDeleteFile(T deletedFile);

        void onPermanentDeleteFile(T deletedFile);

        void onEditFile(T editedFile);

        void onRestoreFile(T restoredFile);
    }
}
