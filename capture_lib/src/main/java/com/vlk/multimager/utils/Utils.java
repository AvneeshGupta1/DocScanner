package com.vlk.multimager.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.vlk.multimager.R;

/**
 * Created by vansikrishna on 08/06/2016.
 */
public class Utils {

    public static void showShortSnack(View parent, String message) {
        Snackbar snackbar = Snackbar.make(parent, message, Snackbar.LENGTH_SHORT);
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setMaxLines(10);
        snackbar.show();
    }

    public static void changeDrawableColor(Context context, ImageView imageView) {
        imageView.setColorFilter(ContextCompat.getColor(context, R.color.icon_color));
    }

    public static void showLongSnack(View parent, String message) {
        Snackbar snackbar = Snackbar.make(parent, message, Snackbar.LENGTH_LONG);
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setMaxLines(10);
        snackbar.show();
    }

    public static boolean hasCameraFlashHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH)) {
            return true;
        } else {
            return false;
        }
    }

    public static void setViewBackgroundColor(Activity activity, View view, int color) {
        if (color == 0)
            return;
        if (view instanceof ImageButton) {
            AppCompatImageButton imageButton = (AppCompatImageButton) view;
            GradientDrawable drawable = (GradientDrawable) imageButton.getBackground();
            drawable.setColor(color);
            if (Build.VERSION.SDK_INT >= 16)
                imageButton.setBackground(drawable);
            else
                imageButton.setBackgroundDrawable(drawable);
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            GradientDrawable drawable = (GradientDrawable) imageView.getBackground();
            drawable.setColor(color);
            if (Build.VERSION.SDK_INT >= 16)
                imageView.setBackground(drawable);
            else
                imageView.setBackgroundDrawable(drawable);
        } else if (view instanceof Toolbar) {
            Toolbar toolbar = (Toolbar) view;
            toolbar.setBackgroundColor(color);
            if (Build.VERSION.SDK_INT >= 21) {
                Window window = activity.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getDarkColor(color));
            }
        }
    }

    public static void setButtonTextColor(View view, int color) {
        if (color == 0)
            return;
        if (view instanceof Button) {
            ((Button) view).setTextColor(color);
        }
    }


    public static int getDarkColor(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.rgb((int) (red * 0.8), (int) (green * 0.8), (int) (blue * 0.8));
    }

    public static int getLightColor(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb((int) (255 * 0.5), red, green, blue);
    }
}
