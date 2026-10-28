package com.example.syncounter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

final class TrendChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MM/dd", Locale.getDefault());
    private List<DailyTotalsDatabase.Entry> entries;
    private double target;
    private boolean showDifference;

    TrendChartView(Context context) {
        super(context);
        initialize();
    }

    TrendChartView(Context context, AttributeSet attributes) {
        super(context, attributes);
        initialize();
    }

    private void initialize() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    void setData(List<DailyTotalsDatabase.Entry> entries, double target,
                 boolean showDifference) {
        this.entries = entries;
        this.target = target;
        this.showDifference = showDifference;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float left = 52 * density;
        float right = getWidth() - 18 * density;
        float top = 20 * density;
        float bottom = getHeight() - 38 * density;
        float textSize = 12 * density;

        paint.setColor(0xFF68756F);
        paint.setTextSize(textSize);
        paint.setStrokeWidth(1 * density);
        paint.setStyle(Paint.Style.FILL);

        if (entries == null || entries.isEmpty()) {
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Your saved daily totals will appear here.",
                    getWidth() / 2f, getHeight() / 2f, paint);
            return;
        }

        int count = entries.size();
        double[] values = new double[count];
        double minimum = 0;
        double maximum = 0;
        for (int i = 0; i < count; i++) {
            DailyTotalsDatabase.Entry entry = entries.get(i);
            values[i] = showDifference ? target - entry.total : entry.total;
            minimum = Math.min(minimum, values[i]);
            maximum = Math.max(maximum, values[i]);
        }
        if (!showDifference) {
            maximum = Math.max(maximum, 1);
        }
        if (maximum - minimum < 0.01) {
            maximum += 0.5;
            minimum -= 0.5;
        } else {
            double padding = (maximum - minimum) * 0.12;
            maximum += padding;
            minimum -= padding;
        }

        float plotWidth = right - left;
        float plotHeight = bottom - top;
        paint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= 4; i++) {
            float y = top + plotHeight * i / 4f;
            double value = maximum - (maximum - minimum) * i / 4.0;
            paint.setColor(0xFFE2E9E5);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(left, y, right, y, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF68756F);
            canvas.drawText(formatNumber(value), left - 7 * density, y + 4 * density, paint);
        }

        Path path = new Path();
        for (int i = 0; i < count; i++) {
            float x = count == 1 ? left + plotWidth / 2f : left + plotWidth * i / (count - 1f);
            float y = top + (float) ((maximum - values[i]) / (maximum - minimum)) * plotHeight;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        paint.setColor(0xFF287A66);
        paint.setStrokeWidth(3 * density);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < count; i++) {
            float x = count == 1 ? left + plotWidth / 2f : left + plotWidth * i / (count - 1f);
            float y = top + (float) ((maximum - values[i]) / (maximum - minimum)) * plotHeight;
            canvas.drawCircle(x, y, 4 * density, paint);
        }

        paint.setColor(0xFF68756F);
        paint.setTextAlign(Paint.Align.CENTER);
        int labelCount = Math.min(count, 5);
        for (int i = 0; i < labelCount; i++) {
            int index = labelCount == 1 ? 0 : Math.round(i * (count - 1f) / (labelCount - 1f));
            float x = count == 1 ? left + plotWidth / 2f : left + plotWidth * index / (count - 1f);
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        .parse(entries.get(index).day);
                if (date != null) {
                    canvas.drawText(dateFormat.format(date), x, getHeight() - 10 * density, paint);
                }
            } catch (java.text.ParseException exception) {
                throw new IllegalStateException("Invalid day in saved totals.", exception);
            }
        }
    }

    private String formatNumber(double value) {
        return String.format(Locale.getDefault(), "%.1f", value);
    }
}
