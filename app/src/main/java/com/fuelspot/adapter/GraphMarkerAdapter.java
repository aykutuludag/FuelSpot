package com.fuelspot.adapter;

import android.content.Context;
import android.widget.TextView;

import com.fuelspot.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.fuelspot.MainActivity.shortTimeFormat;

public class GraphMarkerAdapter extends MarkerView {

    ArrayList<ILineDataSet> dataSets;
    List<Entry> benzinDegerler, dizelDegerler, lpgDegerler, elektrikDegerler;
    DecimalFormat df = new DecimalFormat("#.##");
    private SimpleDateFormat sdf = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
    private TextView textViewGasoline, textViewDiesel, textViewLPG, textViewElectricity, textViewTime;
    private MPPointF mOffset;

    public GraphMarkerAdapter(Context context, int layoutResource, ArrayList<ILineDataSet> dSets) {
        super(context, layoutResource);
        dataSets = dSets;

        textViewGasoline = findViewById(R.id.fiyatBenzin);
        textViewDiesel = findViewById(R.id.fiyatDizel);
        textViewLPG = findViewById(R.id.fiyatLPG);
        textViewElectricity = findViewById(R.id.fiyatElektrik);
        textViewTime = findViewById(R.id.textTime);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String zaman = sdf.format(new Date((long) e.getX()));
        textViewTime.setText(zaman);

        if (benzinDegerler != null) {
            benzinDegerler.clear();
        }

        if (dizelDegerler != null) {
            dizelDegerler.clear();
        }

        if (lpgDegerler != null) {
            lpgDegerler.clear();
        }

        for (int i = 0; i < dataSets.size(); i++) {
            if (i == 0) {
                benzinDegerler = dataSets.get(0).getEntriesForXValue(e.getX());
            } else if (i == 1) {
                dizelDegerler = dataSets.get(1).getEntriesForXValue(e.getX());
            } else if (i == 2) {
                lpgDegerler = dataSets.get(2).getEntriesForXValue(e.getX());
            } else {
                elektrikDegerler = dataSets.get(3).getEntriesForXValue(e.getX());
            }
        }

        if (benzinDegerler != null && benzinDegerler.size() > 0) {
            textViewGasoline.setText(df.format(benzinDegerler.get(0).getY()));
        } else {
            textViewGasoline.setText("-");
        }

        if (dizelDegerler != null && dizelDegerler.size() > 0) {
            textViewDiesel.setText(df.format(dizelDegerler.get(0).getY()));
        } else {
            textViewDiesel.setText("-");
        }

        if (lpgDegerler != null && lpgDegerler.size() > 0) {
            textViewLPG.setText(df.format(lpgDegerler.get(0).getY()));
        } else {
            textViewLPG.setText("-");
        }

        if (elektrikDegerler != null && elektrikDegerler.size() > 0) {
            textViewElectricity.setText(df.format(elektrikDegerler.get(0).getY()));
        } else {
            textViewElectricity.setText("-");
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2f), -getHeight());
        }

        return mOffset;
    }
}