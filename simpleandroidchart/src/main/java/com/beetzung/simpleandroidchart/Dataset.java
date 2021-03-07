package com.beetzung.simpleandroidchart;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Dataset {
    List<Entry> entries = new ArrayList<>();
    List<Entry> points = new ArrayList<>();
    String description = "";
    boolean needsSorting = true;
    float xMax, xMin, xRange, yMax, yMin, yRange, yLast, xFirst, xLast;
    Comparator<Entry> defaultComparator = (o1, o2) -> Float.compare(o1.x, o2.x);
    int lineThickness;
    @ColorInt int lineColor, fillColor, fillAlpha;

    public Dataset() {
        lineColor = Color.BLUE;
        fillColor = Color.TRANSPARENT;
        fillAlpha = 255;
        lineThickness = 2;
    }

    public Dataset(String description) {
        this();
        this.description = description;
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    public void addEntry(float x, float y) {
        Log.d("TEMP", "addEntry() called with: x = [" + x + "], y = [" + y + "]");
        entries.add(new Entry(x, y));
    }

    public void addEntries(List<Entry> entries) {
        this.entries.addAll(entries);
    }

    public void setSortingEnabled(boolean enabled) {
        needsSorting = enabled;
    }

    public void setComparator(Comparator<Entry> comparator) {
        defaultComparator = comparator;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public void setLineThickness(int lineThickness) {
        this.lineThickness = lineThickness;
    }

    public void setFillAlpha(@IntRange(from = 0, to = 255) int fillAlpha) {
        if (fillAlpha > 255)
            this.fillAlpha = 255;
        else if (fillAlpha < 0)
            this.fillAlpha = 0;
        else
            this.fillAlpha = fillAlpha;
    }

    public void sort() {
        Collections.sort(entries, defaultComparator);
    }

    void calculatePoints(int left, int top, int right, int bottom,
                         Float forcedXMax, Float forcedXMin, Float forcedYMax, Float forcedYMin) {
        if (needsSorting)
            sort();
        xMin = entries.get(0).x;
        xMax = entries.get(0).x;
        yMin = entries.get(0).y;
        yMax = entries.get(0).y;
        for (Entry e : entries) {
            if (e.y > yMax)
                yMax = e.y;
            if (e.y < yMin)
                yMin = e.y;
            if (e.x > xMax)
                xMax = e.x;
            if (e.x < xMin)
                xMin = e.x;
        }
        yLast = entries.get(entries.size() - 1).y;
        xLast = entries.get(entries.size() - 1).x;
        xFirst = entries.get(0).x;
        if (forcedXMax != null)
            xMax = forcedXMax;
        if (forcedXMin != null)
            xMin = forcedXMin;
        if (forcedYMax != null)
            yMax = forcedYMax;
        if (forcedYMin != null)
            yMin = forcedYMin;
        xRange = xMax - xMin;
        yRange = yMax - yMin;
        for (Entry e : entries) {
            int x = left + (int) (((e.x - xMin) / xRange) * (right - left));
            int y = bottom - (int) (((e.y - yMin) / yRange) * (bottom - top));
            points.add(new Entry(x, y));
        }

        Log.d("TEMP", String.format("calculatePoints: xmin %s xmax %s xrange %s ymin %s ymax %s yrange %s", xMin, xMax, xRange, yMin, yMax, yRange));
    }

    int calculateY(float value, int top, int bottom) {
        return bottom - (int) (((value - yMin) / yRange) * (bottom - top));
    }

    @Override
    public String toString() {
        return "Dataset{" +
                "points=" + entries +
                ", description='" + description + '\'' +
                '}';
    }
}
