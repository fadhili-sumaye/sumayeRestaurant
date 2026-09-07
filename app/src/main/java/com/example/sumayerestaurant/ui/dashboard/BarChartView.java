package com.example.sumayerestaurant.ui.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lightweight vertical bar chart used by the Owner dashboard.
 * Draws up to ~30 bars with value labels on top and short labels below.
 * Supports both positive (up, green by default) and negative (down, red) values.
 */
public class BarChartView extends View {
    public static class BarEntry {
        public final String label;
        public final float value;
        public final int color;

        public BarEntry(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private static final int DEFAULT_GREEN = Color.parseColor("#FF00C853");
    private static final int DEFAULT_RED = Color.parseColor("#FFFF5252");
    private static final int LABEL_GRAY = Color.parseColor("#FFBDBDBD");
    private static final int BASELINE = Color.parseColor("#FF303030");

    private final List<BarEntry> entries = new ArrayList<>();
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String emptyText = "Hakuna data kwa kipindi hiki";
    private int emptyColor = LABEL_GRAY;

    public BarChartView(Context context) { super(context); init(); }
    public BarChartView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        linePaint.setStrokeWidth(dp(2));
        linePaint.setColor(BASELINE);
    }

    public void setEntries(List<BarEntry> list) {
        entries.clear();
        if (list != null) entries.addAll(list);
        invalidate();
    }

    public void setEmptyText(String text) { emptyText = text; invalidate(); }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int h = resolveSize(dp(190), heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        float h = getHeight();
        if (entries.isEmpty()) {
            textPaint.setColor(emptyColor);
            textPaint.setTextSize(sp(13));
            textPaint.setFakeBoldText(true);
            canvas.drawText(emptyText, w / 2f, h / 2f, textPaint);
            return;
        }

        int n = entries.size();
        float chartBottom = h - dp(18);
        float chartTop = dp(12);
        float maxAbs = 0;
        boolean hasNegative = false;
        for (BarEntry e : entries) {
            float v = Math.abs(e.value);
            if (v > maxAbs) maxAbs = v;
            if (e.value < 0) hasNegative = true;
        }
        if (maxAbs <= 0) maxAbs = 1;
        float usable = chartBottom - chartTop;
        float zeroY = hasNegative ? (chartTop + usable * 0.6f) : chartBottom;
        float posScale = zeroY - chartTop;
        float negScale = chartBottom - zeroY;

        float barSlot = w / (float) n;
        float barWidth = Math.min(barSlot * 0.62f, dp(40));

        // baseline
        canvas.drawLine(dp(4), zeroY, w - dp(4), zeroY, linePaint);

        textPaint.setColor(LABEL_GRAY);
        for (int i = 0; i < n; i++) {
            BarEntry e = entries.get(i);
            float x = i * barSlot + barSlot / 2f;
            float value = e.value;
            float barHeight;
            float top;
            if (value >= 0) {
                barHeight = (value / maxAbs) * posScale;
                top = zeroY - barHeight;
                barPaint.setColor(e.color == 0 ? DEFAULT_GREEN : e.color);
            } else {
                barHeight = (-value / maxAbs) * negScale;
                top = zeroY;
                barPaint.setColor(e.color == 0 ? DEFAULT_RED : e.color);
            }
            canvas.drawRoundRect(new RectF(x - barWidth / 2f, top, x + barWidth / 2f, top + barHeight),
                    dp(3), dp(3), barPaint);

            textPaint.setTextSize(sp(9));
            textPaint.setFakeBoldText(true);
            float valueY = value >= 0 ? Math.max(top - dp(3), chartTop) : (top + barHeight + dp(9));
            canvas.drawText(compact(value), x, valueY, textPaint);

            textPaint.setFakeBoldText(false);
            textPaint.setTextSize(sp(8));
            canvas.drawText(shortLabel(e.label), x, h - dp(4), textPaint);
        }
    }

    private String compact(float value) {
        long v = Math.round(Math.abs(value));
        boolean neg = value < 0;
        String s;
        if (v >= 1000000000L) s = String.format(Locale.US, "%.1fB", v / 1_000_000_000.0);
        else if (v >= 1000000L) s = String.format(Locale.US, "%.1fM", v / 1_000_000.0);
        else if (v >= 1000L) s = String.format(Locale.US, "%.1fk", v / 1000.0);
        else s = String.valueOf(v);
        return neg ? "-" + s : s;
    }

    private String shortLabel(String label) {
        if (label == null || label.isEmpty()) return "";
        if (label.length() <= 6) return label;
        return label.substring(label.length() - 5);
    }

    private int dp(float v) { return Math.round(getResources().getDisplayMetrics().density * v); }
    private int sp(float v) { return Math.round(getResources().getDisplayMetrics().scaledDensity * v); }
}