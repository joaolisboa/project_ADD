package ipleiria.project.add.data.source.database;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;

/**
 * Created by Lisboa on 06-May-17.
 */

public class CategoryRepository implements CategoryDataSource {

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

    public void readData(final FilesRepository.Callback<List<Dimension>> callback){
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dimensions = new LinkedList<>();
                areas = new LinkedList<>();
                criterias = new LinkedList<>();
                addDimensions(dataSnapshot);
                callback.onComplete(dimensions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage(), databaseError.toException());
                callback.onError(databaseError.toException());
            }
        });
    }

    public void addDimensions(DataSnapshot snapshot) {
        for (DataSnapshot dimensionSnap : snapshot.getChildren()) {
            addDimension(dimensionSnap);
        }
    }

    private void addDimension(DataSnapshot snapshot) {
        Dimension dimension = new Dimension(snapshot.child("name").getValue(String.class),
                snapshot.child("reference").getValue(Integer.class));
        dimension.setWeight(snapshot.child("defaultWeight").getValue(Integer.class));
        dimension.setMinWeight(snapshot.child("minWeight").getValue(Integer.class));
        dimension.setMaxWeight(snapshot.child("maxWeight").getValue(Integer.class));
        dimension.setDbKey(snapshot.getKey());

        // if user has defined his own weights then override default weight value to user defined
        User user = UserService.getInstance().getUser();
        if(user.getDimensionWeightLimit(dimension.getDbKey()) == 0){
            user.setDimensionWeightLimit(dimension.getDbKey(), dimension.getWeight());
        }else{
            dimension.setWeight(user.getDimensionWeightLimit(dimension.getDbKey()));
        }

        for (DataSnapshot areaSnap : snapshot.child("areas").getChildren()) {
            Area area = new Area(areaSnap.child("name").getValue(String.class),
                    areaSnap.child("reference").getValue(Integer.class));
            area.setDbKey(areaSnap.getKey());
            for (DataSnapshot criteriaSnap : areaSnap.child("criterias").getChildren()) {
                Criteria criteria = new Criteria(criteriaSnap.child("name").getValue(String.class),
                        criteriaSnap.child("reference").getValue(Integer.class));
                criteria.setObservations((String) criteriaSnap.child("observations").getValue());
                criteria.setRequiredDocument((String) criteriaSnap.child("requiredDocument").getValue());
                criteria.setWeightsInformation((String) criteriaSnap.child("weightsInformation").getValue());
                int readX = criteriaSnap.child("readX").getValue(Integer.class);
                int readY = criteriaSnap.child("readY").getValue(Integer.class);
                int writeX = criteriaSnap.child("writeX").getValue(Integer.class);
                int writeY = criteriaSnap.child("writeY").getValue(Integer.class);
                criteria.setReadCell(new Criteria.Coordinate(readX, readY));
                criteria.setWriteCell(new Criteria.Coordinate(writeX, writeY));
                criteria.setDbKey(criteriaSnap.getKey());
                if (!criterias.contains(criteria)) {
                    area.addCriteria(criteria);
                    criterias.add(criteria);
                }
            }
            if (!areas.contains(area)) {
                dimension.addArea(area);
                areas.add(area);
            }
        }
        if(!dimensions.contains(dimension)) {
            dimensions.add(dimension);
        }
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public Criteria getCriteria(int dimension, int area, int criteria) {
        return dimensions.get(dimension).getArea(area).getCriteria(criteria);
    }

    public Dimension getDimension(String key){
        for(Dimension dimension: dimensions){
            if(dimension.getDbKey().equals(key)){
                return dimension;
            }
        }
        return null;
    }

    public Area getArea(String key){
        for(Area area: areas){
            if(area.getDbKey().equals(key)){
                return area;
            }
        }
        return null;
    }

    public Criteria getCriteria(String key){
        for(Criteria criteria: criterias){
            if(criteria.getDbKey().equals(key)){
                return criteria;
            }
        }
        return null;
    }

    public List<Criteria> getCriterias() {
        return criterias;
    }

    public Criteria getCriteriaFromReference(String reference){
        String[] s = reference.split("\\.");
        int dimension = Integer.valueOf(s[0]) - 1;
        int area = Integer.valueOf(s[1]) - 1;
        int criteria = Integer.valueOf(s[2]) - 1;

        return getCriteria(dimension, area, criteria);
    }

    // prefered over readCategories to get data in the presenter through callbacks
    @Override
    public DatabaseReference getReference() {
        return databaseRef;
    }
}
