package com.example.sumayerestaurant.util;

import com.example.sumayerestaurant.R;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.Context;

/**
 * Centralized helper that applies the SUMAYE dark + green design system to views
 * that are built programmatically in Java (Phase 7/8 screens without layout XML).
 * All colors resolve through @color resources so the palette stays in one place.
 */
public final class ThemeUtil {

    private ThemeUtil() {}

    @ColorInt public static int primary(Context c)     { return ContextCompat.getColor(c, R.color.primary); }
    @ColorInt public static int onPrimary(Context c)   { return ContextCompat.getColor(c, R.color.color_on_primary); }
    @ColorInt public static int textPrimary(Context c) { return ContextCompat.getColor(c, R.color.text_primary); }
    @ColorInt public static int textSecondary(Context c){ return ContextCompat.getColor(c, R.color.secondary_text); }
    @ColorInt public static int surface(Context c)     { return ContextCompat.getColor(c, R.color.surface); }
    @ColorInt public static int surfaceCard(Context c) { return ContextCompat.getColor(c, R.color.card_secondary); }
    @ColorInt public static int background(Context c)  { return ContextCompat.getColor(c, R.color.background); }
    @ColorInt public static int divider(Context c)     { return ContextCompat.getColor(c, R.color.divider); }
    @ColorInt public static int greenBright(Context c) { return ContextCompat.getColor(c, R.color.green_bright); }
    @ColorInt public static int error(Context c)       { return ContextCompat.getColor(c, R.color.error_color); }
    @ColorInt public static int warning(Context c)     { return ContextCompat.getColor(c, R.color.warning_color); }

    /** Applies the dark theme + Poppins sizing to a root container and its descendants. */
    public static void applyRootTheme(View root) {
        root.setBackgroundColor(background(root.getContext()));
    }

    /** Styles a Text label with the primary/white color scheme. */
    public static TextView styleTitle(TextView tv, int sp) {
        tv.setTextColor(textPrimary(tv.getContext()));
        tv.setTextSize(sp);
        tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return tv;
    }

    public static TextView styleLabel(TextView tv, @Dimension float sp) {
        tv.setTextColor(primary(tv.getContext()));
        tv.setTextSize(sp);
        tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return tv;
    }

    /** Styles a button as the primary green CTA. */
    public static Button stylePrimaryButton(Button b) {
        Context c = b.getContext();
        b.setTextColor(onPrimary(c));
        b.setBackgroundTintList(ColorStateList.valueOf(primary(c)));
        b.setMinHeight(dp(c, 54));
        b.setTextSize(16);
        b.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return b;
    }

    /** Styles a button as an outlined ghost action. */
    public static Button styleGhostButton(Button b) {
        Context c = b.getContext();
        b.setTextColor(primary(c));
        b.setBackgroundTintList(ColorStateList.valueOf(surface(c)));
        b.setMinHeight(dp(c, 54));
        b.setTextSize(15);
        b.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return b;
    }

    public static Button styleDangerButton(Button b) {
        Context c = b.getContext();
        b.setTextColor(error(c));
        b.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(c, R.color.status_error_background)));
        b.setMinHeight(dp(c, 54));
        b.setTextSize(15);
        b.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return b;
    }

    /** Styles an EditText as a dark outlined field. */
    public static EditText styleEditText(EditText e) {
        Context c = e.getContext();
        e.setTextColor(textPrimary(c));
        e.setHintTextColor(ContextCompat.getColor(c, R.color.hint_color));
        e.setBackground(ContextCompat.getDrawable(c, R.drawable.bg_search_edittext));
        if (e.getPaddingLeft() == 0) e.setPadding(dp(c, 14), dp(c, 10), dp(c, 14), dp(c, 10));
        return e;
    }

    /** Builds a themed CardView wrapper programmatically. */
    public static CardView themedCard(Context c) {
        CardView card = new CardView(c);
        card.setCardBackgroundColor(surfaceCard(c));
        card.setRadius(dp(c, 16));
        return card;
    }

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static void tintEditTextLine(EditText e) {
        e.setBackgroundTintList(ColorStateList.valueOf(divider(e.getContext())));
    }
}