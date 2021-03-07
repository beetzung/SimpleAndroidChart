package com.beetzung.simpleandroidchart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import java.util.ArrayList;
import java.util.List;

import static com.beetzung.simpleandroidchart.Utils.getRoundedString;

public class LineChart extends View {
    Paint linePaint, fillPaint, backgroundPaint, gridPaint, textPaintMedium, textPaint, crosshairPaint;
    Path fillPath, linePath, backgroundPath, backgroundPathTop, backgroundPathBottom,
            backgroundPathMiddleTop, backgroundPathBottomFill, backgroundPathMiddleBottom, crossHairPath;
    @ColorInt
    int backgroundColor, gridColor, crosshairColor, textColor;
    boolean roundCorners;
    float textHeight;
    List<Dataset> datasets;
    OnTouchListener onTouchListener;
    Entry markerEntry;
    float markerX, markerY;
    int topPadding, bottomPadding, leftPadding, rightPadding, textSize, cornerRadius = dpToPx(8);
    Float forcedMaxY, forcedMinY, forcedMaxX, forcedMinX;
    String rightText, leftText, markerDescription, noDataText = "No data";
    ValueFormatter xAxisValueFormatter, yAxisValueFormatter, defaultXAxisValueFormatter, defaultYAxisValueFormatter;

    public interface ValueFormatter {
        String format(float value);
    }

    public LineChart(Context context) {
        this(context, null);
    }

    public LineChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        linePaint = new Paint();
        fillPaint = new Paint();
        backgroundPaint = new Paint();
        textPaintMedium = new Paint();
        gridPaint = new Paint();
        crosshairPaint = new Paint();
        textPaint = new Paint();
        linePath = new Path();
        fillPath = new Path();
        backgroundPath = new Path();
        backgroundPathTop = new Path();
        backgroundPathBottom = new Path();
        backgroundPathMiddleTop = new Path();
        backgroundPathBottomFill = new Path();
        backgroundPathMiddleBottom = new Path();
        int[] attributes = new int[]{
                android.R.attr.paddingLeft,
                android.R.attr.paddingTop,
                android.R.attr.paddingBottom,
                android.R.attr.paddingRight,
                android.R.attr.padding,
                android.R.attr.textSize,
                android.R.attr.textColor
        };
        TypedArray androidAttrs = context.obtainStyledAttributes(attrs, attributes);
        try {
            int padding = androidAttrs.hasValue(4)
                    ? androidAttrs.getDimensionPixelOffset(4, 666328)
                    : 666328;
            if (padding != 666328) {
                leftPadding = padding;
                rightPadding = padding;
                topPadding = padding;
                bottomPadding = padding;
            } else {
                leftPadding = androidAttrs.getDimensionPixelSize(0, dpToPx(36));
                topPadding = androidAttrs.getDimensionPixelSize(1, dpToPx(8));
                bottomPadding = androidAttrs.getDimensionPixelSize(2, dpToPx(52));
                rightPadding = androidAttrs.getDimensionPixelSize(3, dpToPx(36));
            }
            int defTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13,
                    getResources().getDisplayMetrics());
            textSize = androidAttrs.getDimensionPixelSize(5, defTextSize);
            textColor = androidAttrs.getColor(6, Color.BLACK);
        } finally {
            androidAttrs.recycle();
        }
        TypedArray chartAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.LineChart);
        try {
            roundCorners = chartAttrs.getBoolean(R.styleable.LineChart_roundCorners, true);
            backgroundColor = chartAttrs.getColor(R.styleable.LineChart_backgroundColor, Color.LTGRAY);
            crosshairColor = chartAttrs.getColor(R.styleable.LineChart_crosshairColor, Color.RED);
            gridColor = chartAttrs.getColor(R.styleable.LineChart_gridColor, Color.GRAY);
        } finally {
            chartAttrs.recycle();
        }
        fillPath.setFillType(Path.FillType.EVEN_ODD);
        linePaint.setStyle(Paint.Style.STROKE);
        crosshairPaint.setStrokeWidth(dpToPx(1));
        onTouchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                performClick();
                return false;
            }
            Log.d("TAG", String.format("LineChart: %s %s (%s %s %s %s)", event.getX(), event.getY(), getChartInnerLeft(), getChartInnerTop(), getChartInnerRight(), getChartInnerBottom()));
            float x = event.getX();
            float y = event.getY();
            if (x > getChartInnerLeft() && x < getChartInnerRight()
                    && y > getChartInnerTop() && y < getChartInnerBottom()) {
                Log.d("TEMP", "LineChart: !!!!");
                for (Dataset dataset : datasets) {
                    for (Entry entry : dataset.points) {
                        if (entry.x > x) {
                            markerEntry = entry;
                            for (Dataset d : datasets) {
                                for (Entry e : d.points) {
                                    if (e.y < y && e.x > x) {
                                        markerEntry = e;
                                        markerDescription = d.description;
                                        markerX = d.entries.get(d.points.indexOf(e)).x;
                                        markerY = d.entries.get(d.points.indexOf(e)).y;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            invalidate();
            return true;
        };
        datasets = new ArrayList<>();
        defaultXAxisValueFormatter = Float::toString;
        defaultYAxisValueFormatter = Float::toString;
        xAxisValueFormatter = defaultXAxisValueFormatter;
        yAxisValueFormatter = defaultYAxisValueFormatter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int DEFAULT_WIDTH = 350;
        int desiredWidth = dpToPx(DEFAULT_WIDTH);
        int DEFAULT_HEIGHT = 250;
        int desiredHeight = dpToPx(DEFAULT_HEIGHT);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        backgroundPaint.setColor(backgroundColor);
        gridPaint.setColor(gridColor);
        crosshairPaint.setColor(crosshairColor);
        textPaint.setColor(adjustAlpha(textColor));
        textPaintMedium.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaintMedium.setTextSize(textSize);
        textHeight = (textPaint.descent() - textPaint.ascent()) / 2f;
        drawChartBackground(canvas);
        if (checkDatasets()) {
            for (int i = 0; i < datasets.size(); i++) {
                if (datasets.get(i).points.size() < 2) continue;
                drawDataset(canvas, datasets.get(i));
            }
            drawLabels(canvas);
            if (markerEntry != null)
                drawMarker(canvas);
        } else
            drawNoDataLabel(canvas);
    }

    private void drawNoDataLabel(Canvas canvas) {
        float width = textPaintMedium.measureText(noDataText);
        canvas.drawText(noDataText, (getChartInnerLeft() + getChartInnerRight()) / 2f - width / 2f,
                (getChartInnerBottom() + getChartInnerTop()) / 2f + textHeight / 2f, textPaintMedium);
    }

    private void drawDataset(Canvas canvas, Dataset dataset) {
        linePath.reset();
        fillPath.reset();
        fillPaint.setColor(dataset.fillColor);
        fillPaint.setAlpha(dataset.fillAlpha);
        linePaint.setColor(dataset.lineColor);
        linePaint.setStrokeWidth(dpToPx(dataset.lineThickness));
        List<Entry> points = dataset.points;
        float nextX = points.get(0).x;
        float nextY = points.get(0).y;
        int startX = getChartInnerLeft();
        float startY = nextY;
        fillPath.moveTo(startX, startY);
        fillPath.lineTo(nextX, nextY);
        linePath.moveTo(startX, startY);
        linePath.lineTo(nextX, nextY);
        for (Entry entry : points) {
            if (points.indexOf(entry) < points.size() - 1) {
                nextX = points.get(points.indexOf(entry) + 1).x;
                nextY = points.get(points.indexOf(entry) + 1).y;
            }
            fillPath.cubicTo((entry.x + nextX) / 2f, entry.y, (entry.x + nextX) / 2f, nextY, nextX, nextY);
        }
        fillPath.lineTo(getChartInnerRight(), getChartInnerBottom() - (dpToPx(roundCorners ? 16 : 0)));
        fillPath.lineTo(getChartInnerLeft(), getChartInnerBottom() - dpToPx(roundCorners ? 16 : 0));
        fillPath.lineTo(startX, startY);
        fillPath.close();
        canvas.drawPath(fillPath, fillPaint);
        for (Entry entry : points) {
            if (points.indexOf(entry) < points.size() - 1) {
                nextX = points.get(points.indexOf(entry) + 1).x;
                nextY = points.get(points.indexOf(entry) + 1).y;
            }
            linePath.moveTo(entry.x, entry.y);
            linePath.cubicTo((entry.x + nextX) / 2f, entry.y, (entry.x + nextX) / 2f, nextY, nextX, nextY);

        }
        linePath.close();
        canvas.drawPath(linePath, linePaint);
        if (roundCorners) {
            float[] bottomCorners = new float[]{
                    0, 0,
                    0, 0,
                    cornerRadius, cornerRadius,
                    cornerRadius, cornerRadius
            };
            int left = getChartInnerLeft();
            int right = getChartInnerRight();
            int bottom = getChartInnerBottom();
            RectF bottomFill = new RectF();
            bottomFill.set(left, bottom - (dpToPx(8) + cornerRadius), right, bottom);
            backgroundPathBottomFill.addRoundRect(bottomFill, bottomCorners, Path.Direction.CW);
            backgroundPathBottomFill.close();
            canvas.drawPath(backgroundPathBottomFill, fillPaint);
        }
    }

    private void drawMarker(Canvas canvas) {
        canvas.drawLine(getChartInnerLeft(), markerEntry.y, getChartInnerRight(), markerEntry.y, crosshairPaint);
        canvas.drawLine(markerEntry.x, getChartInnerTop(), markerEntry.x, getChartInnerBottom(), crosshairPaint);
        canvas.drawCircle(markerEntry.x, markerEntry.y, 8, linePaint);
        String s1 = yAxisValueFormatter.format(markerY);
        if ( markerDescription != null && !markerDescription.equals("")) {
            s1 = s1.concat(String.format(" %s", markerDescription));
        }
        float width1 = textPaint.measureText(s1);
        String s2 = xAxisValueFormatter.format(markerX);
        float width2 = textPaint.measureText(s2);
        float left = markerEntry.x + dpToPx(4);
        float top = markerEntry.y - (4 * (int) textHeight) - dpToPx(4);
        float right = markerEntry.x + (int) Math.max(width1, width2) + dpToPx(4);
        float bottom = markerEntry.y - dpToPx(4);
        if (right >= getChartInnerRight()) {
            float width = right - left;
            left -= (width + dpToPx(8));
            right -= (width + dpToPx(8));
        }
        if (top <= getChartInnerTop()) {
            float height = bottom - top;
            top += height + dpToPx(8);
            bottom += height + dpToPx(8);
        }
        float midX = (left + right) / 2;
        float midY = (bottom + top) / 2;
        RectF rect = new RectF(left - dpToPx(4), top - dpToPx(4), right + dpToPx(4), bottom + dpToPx(4));
        canvas.drawRoundRect(rect, dpToPx(8), dpToPx(8), backgroundPaint);
        Paint.Align temp = textPaintMedium.getTextAlign();
        textPaintMedium.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(s2, midX, (bottom + midY) / 2f, textPaintMedium);
        canvas.drawText(s1, midX, (top + midY) / 2f, textPaintMedium);
        textPaintMedium.setTextAlign(temp);
    }

    private void drawLabels(Canvas canvas) {
        String maxText, minText, lastText, midText;
        if (yAxisValueFormatter == defaultYAxisValueFormatter) {
            maxText = getRoundedString(getMaxY(), getRangeY());
            minText = getRoundedString(getMinY(), getRangeY());
            lastText = getRoundedString(getLastY(), getRangeY());
            midText = getRoundedString(((getMaxY() + getMinY()) / 2f), getRangeY());
        } else {
            lastText = yAxisValueFormatter.format(getLastY());
            minText = yAxisValueFormatter.format(getMinY());
            midText = yAxisValueFormatter.format((getMaxY() + getMinY()) / 2f);
            maxText = yAxisValueFormatter.format(getMaxY());
        }
        float maxWidth = textPaint.measureText(maxText);
        float minWidth = textPaint.measureText(minText);
        float midWidth = textPaint.measureText(midText);
        float lastTop = getDatasetForMeasures().calculateY(getLastY(), getChartInnerTop(), getChartInnerBottom()) + textHeight / 2f;
        float maxTop = getChartInnerTop() + dpToPx(roundCorners ? 16 : 8) + textHeight / 2f;
        float minTop = getChartInnerBottom() - dpToPx(roundCorners ? 16 : 8) - textHeight / 2f;
        float midTop = (getChartInnerBottom() + getChartInnerTop()) / 2f + textHeight / 2f;
        canvas.drawText(maxText, getChartInnerLeft() - maxWidth - dpToPx(8), maxTop, textPaint);
        canvas.drawText(minText, getChartInnerLeft() - minWidth - dpToPx(8), minTop, textPaint);
        canvas.drawText(midText, getChartInnerLeft() - midWidth - dpToPx(8), midTop, textPaint);
        String leftText = this.leftText != null ? this.leftText : xAxisValueFormatter.format(getFirstX());
        String rightText = this.rightText != null ? this.rightText : xAxisValueFormatter.format(getLastX());
        float timestampsTop = getChartInnerBottom() + textHeight + dpToPx(8);
        float rightTimestampWidth = textPaint.measureText(rightText);
        canvas.drawText(leftText, getChartInnerLeft(), timestampsTop, textPaint);
        canvas.drawText(rightText, getChartInnerRight() - rightTimestampWidth, timestampsTop, textPaint);
        canvas.drawText(lastText, getChartInnerRight() + dpToPx(8), lastTop, textPaintMedium);
    }

    private void drawChartBackground(Canvas canvas) {
        int top = getChartInnerTop();
        int height = getChartInnerBottom() - getChartInnerTop();
        int left = getChartInnerLeft();
        int right = getChartInnerRight();
        int bottom = getChartInnerBottom();
        if (!checkDatasets()) {
            RectF rectF = new RectF(left, top, right, bottom);
            if (roundCorners) {
                backgroundPath.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW);
            } else {
                backgroundPath.addRect(rectF, Path.Direction.CW);
            }
            backgroundPath.close();
            canvas.drawPath(backgroundPath, backgroundPaint);
        } else {
            float fragmentHeight = (height - 3 * dpToPx(1)) / 4f;
            int space = dpToPx(1);
            RectF topRect = new RectF();
            topRect.set(left, top, right, top + fragmentHeight);
            RectF upMiddleRect = new RectF();
            upMiddleRect.set(left, top + fragmentHeight + space, right, top + fragmentHeight + space + fragmentHeight);
            RectF bottomMiddleRect = new RectF();
            bottomMiddleRect.set(left, top + fragmentHeight + space + fragmentHeight + space, right, top + fragmentHeight * 3 + space * 2);
            RectF bottomRect = new RectF();
            bottomRect.set(left, top + 3 * (fragmentHeight + space), right, bottom);
            if (roundCorners) {
                float[] topCorners = new float[]{
                        cornerRadius, cornerRadius,
                        cornerRadius, cornerRadius,
                        0, 0,
                        0, 0
                };
                float[] bottomCorners = new float[]{
                        0, 0,
                        0, 0,
                        cornerRadius, cornerRadius,
                        cornerRadius, cornerRadius
                };
                backgroundPathTop.addRoundRect(topRect, topCorners, Path.Direction.CW);
                backgroundPathBottom.addRoundRect(bottomRect, bottomCorners, Path.Direction.CW);
            } else {
                backgroundPathTop.addRect(topRect, Path.Direction.CW);
                backgroundPathBottom.addRect(bottomRect, Path.Direction.CW);
            }
            backgroundPathTop.close();
            backgroundPathMiddleTop.addRect(upMiddleRect, Path.Direction.CW);
            backgroundPathMiddleTop.close();
            backgroundPathMiddleBottom.addRect(bottomMiddleRect, Path.Direction.CW);
            backgroundPathMiddleBottom.close();
            backgroundPathBottom.close();
            canvas.drawPath(backgroundPathTop, backgroundPaint);
            canvas.drawPath(backgroundPathMiddleTop, backgroundPaint);
            canvas.drawPath(backgroundPathMiddleBottom, backgroundPaint);
            canvas.drawPath(backgroundPathBottom, backgroundPaint);
        }
    }

    @UiThread
    public void setData(List<Dataset> data) {
        markerEntry = null;
        datasets = data;
        if (checkDatasets()) {
            for (Dataset dataset : datasets) {
                dataset.calculatePoints(
                        getChartInnerLeft(),
                        getChartInnerTop() + dpToPx(roundCorners ? 16 : 8),
                        getChartInnerRight(),
                        getChartInnerBottom() - dpToPx(roundCorners ? 16 : 8),
                        forcedMaxX, forcedMinX, forcedMaxY, forcedMinY
                );
            }
            setOnTouchListener(onTouchListener);
        } else {
            setOnTouchListener(null);
        }
        invalidate();
    }

    private boolean checkDatasets() {
        if (datasets == null || datasets.size() == 0)
            return false;
        for (Dataset dataset : datasets) {
            if (dataset.entries.size() > 2)
                return true;
        }
        return false;
    }

    private int getChartInnerTop() {
        return topPadding;
    }

    private int getChartInnerBottom() {
        return getHeight() - bottomPadding;
    }

    private int getChartInnerLeft() {
        return leftPadding;
    }

    private int getChartInnerRight() {
        return getWidth() - rightPadding;
    }

    private float getMaxY() {
        return getDatasetForMeasures().yMax;
    }

    private Dataset getDatasetForMeasures() {
        return datasets.get(0);
    }

    private float getMinY() {
        return getDatasetForMeasures().yMin;
    }

    private float getLastY() {
        return getDatasetForMeasures().yLast;
    }

    private float getRangeY() {
        return getDatasetForMeasures().yRange;
    }

    private float getFirstX() {
        return getDatasetForMeasures().xFirst;
    }

    private float getLastX() {
        return getDatasetForMeasures().xLast;
    }

    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @ColorInt
    private static int adjustAlpha(@ColorInt int color) {
        int alpha = Math.round(Color.alpha(color) * (float) 0.5);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public void setMaxX(Float forcedMaxX) {
        this.forcedMaxX = forcedMaxX;
    }

    public void setMaxY(Float forcedMaxY) {
        this.forcedMaxY = forcedMaxY;
    }

    public void setMinX(Float forcedMinX) {
        this.forcedMinX = forcedMinX;
    }

    public void setMinY(Float forcedMinY) {
        this.forcedMinY = forcedMinY;
    }

    public void setYAxisValueFormatter(ValueFormatter yAxisValueFormatter) {
        this.yAxisValueFormatter = yAxisValueFormatter;
    }

    public void setXAxisValueFormatter(ValueFormatter xAxisValueFormatter) {
        this.xAxisValueFormatter = xAxisValueFormatter;
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public void setNoDataText(String noDataText) {
        this.noDataText = noDataText;
    }

    public void setRoundCorners(boolean roundCorners) {
        this.roundCorners = roundCorners;
    }
}