package com.beetzung.simpleandroidchart_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.beetzung.simpleandroidchart.Dataset;
import com.beetzung.simpleandroidchart.LineChart;
import com.beetzung.simpleandroidchart_demo.R;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    int lineCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SeekBar seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lineCount = progress + 1;
                ((TextView) findViewById(R.id.text)).setText(Integer.toString(lineCount));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        LineChart chart = findViewById(R.id.lineChart);

        //Set chart X value date format (date int -> date string)
        chart.setXAxisValueFormatter(value -> {
            Date date = new Date((long) value);
            DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return dateFormat.format(date);
        });
        findViewById(R.id.set_data).setOnClickListener(v -> setData(chart));
        findViewById(R.id.clear).setOnClickListener(v -> clearData(chart));
    }

    private void setData(LineChart chart) {
        List<Dataset> datasets = new ArrayList<>();

        //Generate {lineCount} datasets
        Random rnd = new Random();
        for (int l = 0; l < lineCount; l++) {
            int lowerBound = 115;
            int upperBound = 1000 - lowerBound;
            Dataset dataset = new Dataset("Random data");
            for (long i = System.currentTimeMillis();
                 i <= System.currentTimeMillis() + 86400000;
                 i += 3600000) {
                Log.d(TAG, "setData: " + i);
                //Fill with random Y
                dataset.addEntry(i, rnd.nextInt(upperBound) + lowerBound);
            }

            //Set random colors
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            dataset.setLineColor(color);
            dataset.setFillColor(color);
            dataset.setFillAlpha(100);
            Log.d(TAG, "setData: " + dataset);

            //Add to datasets list
            datasets.add(dataset);
        }

        //Set chart data
        chart.setData(datasets);
    }

    private void clearData(LineChart chart) {

        chart.setData(null);
    }

    void asd() {
        LineChart chart = findViewById(R.id.lineChart);

        List<Dataset> datasets = new ArrayList<>();
        Dataset dataset = new Dataset("");
        dataset.addEntry(0, 2.5f);
        dataset.addEntry(1, 2.3f);
        dataset.addEntry(2, 2.2f);
        dataset.addEntry(3, 2.45f);
        dataset.addEntry(4, 2.5f);
        dataset.addEntry(5, 2.78f);
        dataset.addEntry(6, 2.9f);
        datasets.add(dataset);

        chart.setData(datasets);
    }

}