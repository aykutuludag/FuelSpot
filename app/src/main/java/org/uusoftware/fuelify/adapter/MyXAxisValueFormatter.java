package org.uusoftware.fuelify.adapter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyXAxisValueFormatter implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.US);
        return sdf.format(new Date((long) value));
    }
}