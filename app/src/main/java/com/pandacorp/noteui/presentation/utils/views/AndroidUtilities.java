package com.pandacorp.noteui.presentation.utils.views;

import android.graphics.Point;
import android.graphics.RectF;

public class AndroidUtilities {
    public static Point displaySize = new Point();
    public static float density = 1;

    public static final RectF rectTmp = new RectF();

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static boolean isRTL(CharSequence text) {
        if (text == null || text.length() <= 0) {
            return false;
        }
        char c;
        for (int i = 0; i < text.length(); ++i) {
            c = text.charAt(i);
            if (c >= 0x590 && c <= 0x6ff) {
                return true;
            }
        }
        return false;
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }
}
