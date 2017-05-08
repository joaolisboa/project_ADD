package ipleiria.project.add.data.source;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Category;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.User;

/**
 * Created by Lisboa on 06-May-17.
 */

public class CategoryRepository implements CategoryDataSource{

    private static final String TAG = "CATEGORY_REPO";
    private static CategoryRepository INSTANCE = null;

    private DatabaseReference databaseRef;

    private List<Dimension> dimensions;
    private List<Area> areas;
    private List<Criteria> criterias;

    private CategoryRepository() {
        this.databaseRef = FirebaseDatabase.getInstance().getReference().child("categories");
        this.databaseRef.keepSynced(true);

        this.dimensions = new LinkedList<>();
        this.areas = new LinkedList<>();
        this.criterias = new LinkedList<>();
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @return the {@link ItemsRepository} instance
     */
    public static CategoryRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CategoryRepository();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private void readCriteria(){
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dimensionSnap : dataSnapshot.getChildren()) {
                    addDimension(dimensionSnap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    public void addDimensions(DataSnapshot snapshot){
        for (DataSnapshot dimensionSnap : snapshot.getChildren()) {
            addDimension(dimensionSnap);
        }
    }

    public void addDimension(DataSnapshot snapshot){
        Dimension dimension = new Dimension(snapshot.child("name").getValue(String.class),
                snapshot.child("reference").getValue(Integer.class));
        dimension.setDbKey(snapshot.getKey());
        for (DataSnapshot areaSnap : snapshot.child("areas").getChildren()) {
            Area area = new Area(areaSnap.child("name").getValue(String.class),
                    areaSnap.child("reference").getValue(Integer.class));
            area.setDbKey(areaSnap.getKey());
            for (DataSnapshot criteriaSnap : areaSnap.child("criterias").getChildren()) {
                Criteria criteria = new Criteria(criteriaSnap.child("name").getValue(String.class),
                        criteriaSnap.child("reference").getValue(Integer.class));
                int readX = criteriaSnap.child("readX").getValue(Integer.class);
                int readY = criteriaSnap.child("readY").getValue(Integer.class);
                int writeX = criteriaSnap.child("writeX").getValue(Integer.class);
                int writeY = criteriaSnap.child("writeY").getValue(Integer.class);
                criteria.setReadCell(new Criteria.Coordinate(readX, readY));
                criteria.setWriteCell(new Criteria.Coordinate(writeX, writeY));
                criteria.setDbKey(criteriaSnap.getKey());
                area.addCriteria(criteria);
                criterias.add(criteria);
            }
            dimension.addArea(area);
            areas.add(area);
        }
        dimensions.add(dimension);
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public Criteria getCriteria(int dimension, int area, int criteria){
        return dimensions.get(dimension).getArea(area).getCriteria(criteria);
    }

    public List<Criteria> getCriterias(){
        return criterias;
    }

    // prefered over readCategories to get data in the presenter through callbacks
    @Override
    public DatabaseReference getReference() {
        return databaseRef;
    }
}
