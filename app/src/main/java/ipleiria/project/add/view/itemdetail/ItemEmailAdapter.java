package ipleiria.project.add.view.itemdetail;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.LinkedHashMap;
import java.util.List;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.Email;
import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by J on 07/06/2017.
 */

class ItemEmailAdapter extends BaseSwipeAdapter {

    private ItemDetailFragment.ActionListener actionsListener;

    private List<Email> listEmails;
    private boolean listingDeleted;

    public ItemEmailAdapter(List<Email> listEmails, ItemDetailFragment.ActionListener actionsListener,
                            boolean listingDeleted) {
        this.actionsListener = actionsListener;
        this.listingDeleted = listingDeleted;
        setList(listEmails);
    }

    private void setList(List<Email> emails) {
        this.listEmails = emails;
    }

    public void replaceData(List<Email> emails) {
        setList(emails);
        notifyDataSetChanged();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.bottom_layout_actions;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        return null;
    }

    @Override
    public void fillValues(int position, View convertView) {

    }

    @Override
    public int getCount() {
        return listEmails.size();
    }

    @Override
    public Object getItem(int position) {
        return (Email)listEmails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void onEmailAdded(Email email) {

    }

    public void onEmailRemoved(Email email) {
    }
}
