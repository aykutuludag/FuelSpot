package com.fuelspot;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class FragmentSettings extends Fragment {

    LineChart mLineChart, mLineChart2, mLineChart3;

    public static FragmentSettings newInstance() {

        Bundle args = new Bundle();

        FragmentSettings fragment = new FragmentSettings();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("İstatistikler");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        /*mLineChart = rootView.findViewById(R.id.chart1);
        if (purchaseTimes.size() > 0) {
            createTable1();
        }
        mLineChart2 = rootView.findViewById(R.id.chart2);
        if (purchaseTimes.size() > 0) {
            createTable2();
        }
        mLineChart3 = rootView.findViewById(R.id.chart3);
        if (purchaseTimes.size() > 0) {
            createTable3();
        }*/

        return rootView;
    }

   /*public void createTable1() {
        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < purchaseTimes.size(); i++) {
            float x = purchaseTimes.get(i);
            float y = Float.valueOf(String.valueOf(purchasePrices.get(i)));
            entries.add(new Entry(x, y));
        }

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        LineDataSet dataSet = new LineDataSet(entries, "Purchases over time");
        LineData data = new LineData(dataSet);
        mLineChart.setData(data);
    }

    public void createTable2() {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<Entry> entries2 = new ArrayList<>();

        for (int i = 0; i < purchaseTimes.size(); i++) {
            float x = purchaseTimes.get(i);
            float y = Float.valueOf(String.valueOf(purchaseUnitPrice.get(i)));
            entries.add(new Entry(x, y));

            float y2 = Float.valueOf(String.valueOf(purchaseUnitPrice2.get(i)));
            if (y2 > 0) {
                entries2.add(new Entry(x, y2));
            }
        }

        XAxis xAxis = mLineChart2.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        LineData data;
        LineDataSet dataSet = new LineDataSet(entries, "Primary fuel");
        if (entries2.size() > 0) {
            LineDataSet dataSet2 = new LineDataSet(entries2, "Secondary fuel");
            data = new LineData(dataSet, dataSet2);
        } else {
            data = new LineData(dataSet);
        }

        mLineChart2.setData(data);
    }

    public void createTable3() {
        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < purchaseTimes.size(); i++) {
            float x = purchaseTimes.get(i);
            float y = Float.valueOf(String.valueOf(purchasePrices.get(i) * 0.67f));
            entries.add(new Entry(x, y));
        }

        XAxis xAxis = mLineChart3.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new MyXAxisValueFormatter());

        LineDataSet dataSet = new LineDataSet(entries, "Tax over time");
        LineData data = new LineData(dataSet);
        mLineChart3.setData(data);
    }*/
}