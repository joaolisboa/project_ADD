package ipleiria.project.add;

import ipleiria.project.add.data.model.Item;

/**
 * Created by Lisboa on 04-Jun-17.
 */

public interface ItemClickListener {

    void onItemClick(Item clickedIem);

    void onDeleteItem(Item deletedItem);

    void onEditItem(Item editedItem);

    void onPermanentDeleteItem(Item deletedItem);

    void onRestoreItem(Item restoredItem);

}
