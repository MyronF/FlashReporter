package myron.com.crimereporter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@IgnoreExtraProperties
public class ReviewLocation extends AppCompatActivity {

    //Algorithm that compares what the users have said about the same place

    private static final String TAG = "ReviewLocation";
    //variables
    private String id;
    private Double latitude;
    private Double longitude;
    private String reviews;
    private Double ratings;
    private DatabaseReference flashReporterDatabase = FirebaseDatabase.getInstance().getReference();
    private List<ReviewLocation> mapPointers = new ArrayList<ReviewLocation>();

    //widgets
    private EditText inputReview;
    private Button btnSubmit;
    private RatingBar ratingBar;
    private Integer Votes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewlocation);

        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);

        inputReview = (EditText) findViewById(R.id.Review);
        btnSubmit = (Button) findViewById(R.id.btn_review);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        btnSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Float rating = new Float(ratingBar.getRating());
                Double doubleResult = Double.parseDouble(new Float(rating).toString());

                try{
                    reviews = inputReview.getText().toString();
                    ratings = doubleResult;
                    Votes = 1;

                    writeNewItem();

                }catch (NullPointerException e){
                    Log.d(TAG, "Review " + e);
                }
            }
        });
    }


    private boolean writeNewItem() {
        try {
            String itemId = flashReporterDatabase.push().getKey();

            MapPointer item = new MapPointer(itemId, latitude, longitude, reviews, ratings, Votes);

            if (reviews.equals("")){
                Toast.makeText(this,"Failed to post the review",Toast.LENGTH_SHORT).show();
            }else if (latitude.equals(0)|| longitude.equals(0)){
                Toast.makeText(this,"Failed to post the review",Toast.LENGTH_SHORT).show();
            }else{
                flashReporterDatabase.child(itemId).setValue(item);
                Toast.makeText(this,"Review posted",Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "itemId2: " + itemId);
        } catch (DatabaseException e){
            Log.d(TAG, "itemId3: " + e.getMessage());
            return false;
        }
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        return true;
    }

}
