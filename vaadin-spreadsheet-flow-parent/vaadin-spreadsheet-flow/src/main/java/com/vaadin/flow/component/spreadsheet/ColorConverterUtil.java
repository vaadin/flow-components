package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;

public class ColorConverterUtil implements Serializable {

    public static String toRGBA(byte[] argb) {
        int rgba[] = new int[3];
        for (int i = 1; i < argb.length; i++) {
            int x = argb[i];
            if (x < 0) {
                x += 256;
            }
            rgba[i - 1] = x;
        }

        float x = argb[0];
        return buildRgba(rgba, x);
    }

    public static String toRGBA(String hexARGB) {
        int rgba[] = new int[3];

        rgba[0] = Integer.parseInt(hexARGB.substring(2, 4), 16);
        rgba[1] = Integer.parseInt(hexARGB.substring(4, 6), 16);
        rgba[2] = Integer.parseInt(hexARGB.substring(6), 16);
        float alpha = Integer.parseInt(hexARGB.substring(0, 2), 16);
        return buildRgba(rgba, alpha);
    }

    public static String buildRgba(int[] rgb, float alpha) {
        StringBuilder sb = new StringBuilder();
        sb.append("rgba(");
        sb.append(rgb[0]);
        sb.append(", ");
        sb.append(rgb[1]);
        sb.append(", ");
        sb.append(rgb[2]);
        sb.append(", ");
        if (alpha == -1.0f) {
            alpha = 1.0f;
        } else if (alpha == 0.0) {
            // This is done because of a bug (???) in POI. Colors from libre
            // office in POI have the alpha-channel as 0.0, so that makes the
            // colors all wrong. The correct value should be -1.0 (no Alpha)
            alpha = 1;
        }
        sb.append(alpha);
        sb.append(");");
        return sb.toString();
    }
}