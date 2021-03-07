package com.beetzung.simpleandroidchart;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Utils {
    static String getRoundedString(float f, float range) {
        float rounded = round(f, getPlaces(f, range));
        String roundedString = Float.toString(rounded);
        if (getPlaces(f, range) == 0)
            roundedString = roundedString.replace(".0", "");
        return roundedString;
    }

    static float round(double f, int places) {
        if (places == 0) {
            int c = (int) ((f) + 0.5f);
            double n = f + 0.5f;
            return (n - c) % 2 == 0 ? (int) f : c;
        }
        StringBuilder pattern = new StringBuilder("#");
        pattern.append(".");
        for (int i = 0; i < places; i++) {
            pattern.append("#");
        }
        DecimalFormat df = new DecimalFormat(pattern.toString());
        df.setRoundingMode(RoundingMode.CEILING);
        return Float.parseFloat(df.format(f).replace(",", "."));
    }

    static int getPlaces(float value, float range) {
        int places;
        if (value >= 1000)
            places = 0;
        else if (value >= 100)
            places = range > 5 ? 0 : 1;
        else if (value >= 10)
            places = range < 5 ?
                    (range < 1 ?
                            (2)
                            : (1))
                    : (0);
        else
            places = range < 5 ?
                    (range < 1 ?
                            (3)
                            : (2))
                    : (0);
        return places;
    }
}