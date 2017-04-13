package ipleiria.project.add.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import ipleiria.project.add.R;

/**
 * Created by Lisboa on 12-Apr-17.
 */

public class Criteria extends Category{

    private Area area;

    public Criteria(String name, int reference){
        super(name, reference);
    }

    public Dimension getDimension() {
        return area.getDimension();
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getRealReference() {
        StringBuilder ref = new StringBuilder();
        ref.append(getDimension().getReference()).append(".");
        ref.append(getArea().getReference()).append(".");
        ref.append(reference);
        return ref.toString();
    }



}