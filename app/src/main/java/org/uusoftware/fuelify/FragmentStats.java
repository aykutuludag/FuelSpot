package org.uusoftware.fuelify;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.uusoftware.fuelify.adapter.MyXAxisValueFormatter;

import java.util.ArrayList;

import static org.uusoftware.fuelify.MainActivity.purchasePrices;
import static org.uusoftware.fuelify.MainActivity.purchaseTimes;

public class FragmentStats extends Fragment {

    LineChart mLineChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        mLineChart = rootView.findViewById(R.id.chart);
        createTable1();

        return rootView;
    }

    public void createTable1() {
        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < purchaseTimes.size(); i++) {
            float x = purchaseTimes.get(i);
            System.out.println("SATIN ALMA TARİHİ: " + purchaseTimes.get(i));
            float y = Float.valueOf(String.valueOf(purchasePrices.get(i)));
            entries.add(new Entry(x, y));
        }

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        LineDataSet dataSet = new LineDataSet(entries, "Time series");
        LineData data = new LineData(dataSet);
        mLineChart.setData(data);
    }
}
