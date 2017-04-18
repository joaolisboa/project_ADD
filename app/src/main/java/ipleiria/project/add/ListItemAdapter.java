package ipleiria.project.add;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.List;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Item;

/**
 * Created by Lisboa on 18-Apr-17.
 */

public class ListItemAdapter extends BaseSwipeAdapter {

    private Context context;
    private List<Item> listItems;

    //protected SwipeItemRecyclerMangerImpl mItemManger = new SwipeItemRecyclerMangerImpl(this);

    public ListItemAdapter(Context context, List<Item> objects) {
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
        View itemView = LayoutInflater.from(context).inflate(R.layout.listview_item, null);
        Item item = listItems.get(position);

        SwipeLayout swipeLayout = (SwipeLayout) itemView.findViewById(R.id.bottom_layout_actions);
        FrameLayout itemLayout = (FrameLayout) itemView.findViewById(R.id.item_view);
        TextView itemName = (TextView) itemLayout.findViewById(R.id.title_text_view);
        TextView itemCriteria = (TextView) itemLayout.findViewById(R.id.category_text_view);
        ImageView itemThumbnail = (ImageView) itemLayout.findViewById(R.id.file_thumbnail);
        ImageView buttonEdit = (ImageView) itemView.findViewById(R.id.edit);
        ImageView buttonDelete = (ImageView) itemView.findViewById(R.id.delete);
        //SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));

        itemName.setText(item.getFilename());
        itemCriteria.setText("Criteria: " + item.getCategoryReference());
        itemThumbnail.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.default_file_thumb));
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Item item = (Item)getItem(position);
                Toast.makeText(context, "click delete " + item, Toast.LENGTH_SHORT).show();
                ApplicationData.getInstance().deleteItem(item.getDbKey());
                FirebaseHandler.getInstance().deleteItem(item.getDbKey());
            }
        });
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = (Item)getItem(position);
                Toast.makeText(context, "click edit " + item, Toast.LENGTH_SHORT).show();
                Intent editIntent = new Intent(context, AddItemActivity.class);
                editIntent.putExtra("itemKey", item.getDbKey());
                context.startActivity(editIntent);
            }
        });
        return itemView;
    }

    @Override
    public void fillValues(int position, View convertView) {}
}