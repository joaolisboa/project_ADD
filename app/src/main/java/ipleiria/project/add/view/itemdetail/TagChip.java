package ipleiria.project.add.view.itemdetail;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pchmn.materialchips.model.ChipInterface;

/**
 * Created by Lisboa on 21-May-17.
 */

public class TagChip implements ChipInterface {

    private String tag;

    TagChip(String tag){
        this.tag = tag;
    }

    @Override
    public Object getId() {
        return tag;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return tag;
    }

    @Override
    public String getInfo() {
        return null;
    }
}
