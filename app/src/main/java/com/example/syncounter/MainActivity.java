package com.example.syncounter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final double STEP = 0.5;
    private static final String PREFS_NAME = "syn_counter_preferences";
    private static final String TARGET_KEY = "target";
    private static final int GREEN = 0xFF287A66;
    private static final int INK = 0xFF20352D;
    private static final int MUTED = 0xFF68756F;

    private DailyTotalsDatabase database;
    private LocalDate selectedDay;
    private double target;
    private boolean showDifference;
    private LinearLayout content;
    private TextView dateButton;
    private TextView totalText;
    private TextView belowTargetText;
    private EditText targetInput;
    private TrendChartView chart;
    private Button totalModeButton;
    private Button differenceModeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFFF5F7F6);
        getWindow().setNavigationBarColor(0xFFF5F7F6);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        database = new DailyTotalsDatabase(this);
        selectedDay = LocalDate.now();
        target = Double.parseDouble(
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(TARGET_KEY, "15.0"));
        buildScreen();
        showCounter();
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF5F7F6);
        root.setPadding(dp(20), dp(14), dp(20), dp(12));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(
                    dp(20) + insets.getSystemWindowInsetLeft(),
                    dp(14) + insets.getSystemWindowInsetTop(),
                    dp(20) + insets.getSystemWindowInsetRight(),
                    dp(12) + insets.getSystemWindowInsetBottom());
            return insets;
        });

        TextView title = text("Syn Counter", 25, INK, true);
        root.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        Button counterTab = button("Counter");
        Button trendsTab = button("Trends");
        tabs.addView(counterTab, new LinearLayout.LayoutParams(0, dp(48), 1));
        tabs.addView(trendsTab, new LinearLayout.LayoutParams(0, dp(48), 1));
        root.addView(tabs, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);

        counterTab.setOnClickListener(view -> showCounter());
        trendsTab.setOnClickListener(view -> showTrends());
    }

    private void showCounter() {
        content.removeAllViews();

        dateButton = text("", 16, GREEN, true);
        dateButton.setGravity(Gravity.CENTER);
        dateButton.setBackground(rounded(0xFFFFFFFF, 14));
        dateButton.setOnClickListener(view -> showDatePicker());
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        dateParams.setMargins(0, dp(14), 0, dp(18));
        content.addView(dateButton, dateParams);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(20), dp(22), dp(20), dp(20));
        card.setBackground(rounded(0xFFFFFFFF, 20));
        TextView heading = text("Syns", 19, MUTED, true);
        heading.setGravity(Gravity.CENTER);
        card.addView(heading);

        totalText = text("", 66, INK, true);
        totalText.setGravity(Gravity.CENTER);
        card.addView(totalText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(94)));

        belowTargetText = text("", 15, MUTED, false);
        belowTargetText.setGravity(Gravity.CENTER);
        card.addView(belowTargetText);

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        Button minus = button("− 0.5");
        Button plus = button("+ 0.5");
        minus.setContentDescription("Subtract half a syn");
        plus.setContentDescription("Add half a syn");
        controls.addView(minus, new LinearLayout.LayoutParams(0, dp(56), 1));
        LinearLayout.LayoutParams plusParams = new LinearLayout.LayoutParams(0, dp(56), 1);
        plusParams.setMargins(dp(12), 0, 0, 0);
        controls.addView(plus, plusParams);
        LinearLayout.LayoutParams controlsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56));
        controlsParams.setMargins(0, dp(20), 0, 0);
        card.addView(controls, controlsParams);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        content.addView(card, cardParams);

        TextView targetLabel = text("Daily target", 15, MUTED, true);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(30));
        labelParams.setMargins(dp(2), dp(18), 0, dp(6));
        content.addView(targetLabel, labelParams);

        LinearLayout targetRow = new LinearLayout(this);
        targetRow.setGravity(Gravity.CENTER_VERTICAL);
        targetInput = new EditText(this);
        targetInput.setSingleLine(true);
        targetInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        targetInput.setHint("Target");
        targetInput.setText(formatNumber(target));
        targetInput.setSelectAllOnFocus(true);
        targetInput.setPadding(dp(14), 0, dp(14), 0);
        targetInput.setBackground(rounded(0xFFFFFFFF, 12));
        Button saveTarget = button("Save target");
        targetRow.addView(targetInput, new LinearLayout.LayoutParams(0, dp(50), 1));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                0, dp(50), 1);
        saveParams.setMargins(dp(10), 0, 0, 0);
        targetRow.addView(saveTarget, saveParams);
        content.addView(targetRow);

        minus.setOnClickListener(view -> changeTotal(-STEP));
        plus.setOnClickListener(view -> changeTotal(STEP));
        saveTarget.setOnClickListener(view -> saveTarget());
        updateCounter();
    }

    private void showTrends() {
        content.removeAllViews();
        TextView heading = text("Daily history", 22, INK, true);
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54));
        headingParams.setMargins(0, dp(12), 0, dp(4));
        content.addView(heading, headingParams);

        LinearLayout modes = new LinearLayout(this);
        totalModeButton = button("Daily total");
        differenceModeButton = button("Below target");
        modes.addView(totalModeButton, new LinearLayout.LayoutParams(0, dp(46), 1));
        LinearLayout.LayoutParams differenceParams = new LinearLayout.LayoutParams(0, dp(46), 1);
        differenceParams.setMargins(dp(8), 0, 0, 0);
        modes.addView(differenceModeButton, differenceParams);
        content.addView(modes);

        TextView chartDescription = text(
                "Showing your 30 most recent saved days", 13, MUTED, false);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(38));
        descriptionParams.setMargins(dp(2), dp(8), 0, 0);
        content.addView(chartDescription, descriptionParams);

        chart = new TrendChartView(this);
        chart.setBackground(rounded(0xFFFFFFFF, 16));
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(310));
        content.addView(chart, chartParams);

        totalModeButton.setOnClickListener(view -> {
            showDifference = false;
            updateChart();
        });
        differenceModeButton.setOnClickListener(view -> {
            showDifference = true;
            updateChart();
        });
        updateChart();
    }

    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDay = LocalDate.of(year, month + 1, dayOfMonth);
                    updateCounter();
                },
                selectedDay.getYear(),
                selectedDay.getMonthValue() - 1,
                selectedDay.getDayOfMonth());
        picker.show();
    }

    private void changeTotal(double amount) {
        double next = database.getTotal(selectedDay.format(DAY_FORMAT)) + amount;
        next = Math.max(0, Math.round(next * 2.0) / 2.0);
        database.saveTotal(selectedDay.format(DAY_FORMAT), next);
        updateCounter();
        if (chart != null) {
            updateChart();
        }
    }

    private void saveTarget() {
        String entered = targetInput.getText().toString().trim().replace(',', '.');
        try {
            double value = Double.parseDouble(entered);
            if (!Double.isFinite(value) || value < 0) {
                throw new NumberFormatException();
            }
            target = value;
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putString(TARGET_KEY, Double.toString(target))
                    .apply();
            updateCounter();
            if (chart != null) {
                updateChart();
            }
            Toast.makeText(this, "Target saved", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException exception) {
            targetInput.setError("Enter a valid target of zero or more");
        }
    }

    private void updateCounter() {
        String day = selectedDay.format(DAY_FORMAT);
        if (dateButton == null || totalText == null || belowTargetText == null) {
            return;
        }
        String label = selectedDay.equals(LocalDate.now())
                ? "Today · " + selectedDay.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
                : selectedDay.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"));
        dateButton.setText(label + "  ▾");
        double total = database.getTotal(day);
        totalText.setText(formatNumber(total));
        belowTargetText.setText(formatNumber(target - total) + " Syns below target");
    }

    private void updateChart() {
        List<DailyTotalsDatabase.Entry> entries = database.getRecentEntries(30);
        chart.setData(entries, target, showDifference);
        chart.setContentDescription(showDifference
                ? "Graph of syns below target over the most recent saved days"
                : "Graph of daily syn totals over the most recent saved days");
        if (totalModeButton != null && differenceModeButton != null) {
            totalModeButton.setBackground(rounded(showDifference ? 0xFFFFFFFF : 0xFFDCEDE6, 12));
            differenceModeButton.setBackground(rounded(showDifference ? 0xFFDCEDE6 : 0xFFFFFFFF, 12));
        }
    }

    private TextView text(String value, int sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(GREEN);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(rounded(0xFFFFFFFF, 12));
        return button;
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String formatNumber(double value) {
        return String.format(Locale.getDefault(), "%.1f", value);
    }
}
