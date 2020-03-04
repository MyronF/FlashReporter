package myron.com.crimereporter;

import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DisplayStatistics extends AppCompatActivity {

    PointsGraphSeries<DataPoint> xySeries;
    GraphView mScatterPlot;
    public double mY;
    public String mX;
    MapPointer coOrdinates = new MapPointer();

    private ArrayList<XYvalue> xyValueArray;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_statistics);

        xySeries = new PointsGraphSeries<>();

        mX = coOrdinates.getReviews();
        mY = coOrdinates.getRating();
        mScatterPlot = findViewById(R.id.scatterPlot);
        xyValueArray = new ArrayList<>();

        String x = mX;
        double y = mY;

        xyValueArray.add(new XYvalue(x,y));

    }


    public void createScatterPlot(){



    }
}
