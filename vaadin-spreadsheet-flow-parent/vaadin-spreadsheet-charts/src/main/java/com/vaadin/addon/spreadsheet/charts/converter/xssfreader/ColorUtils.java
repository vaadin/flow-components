package com.vaadin.addon.spreadsheet.charts.converter.xssfreader;

import org.openxmlformats.schemas.drawingml.x2006.main.CTColorScheme;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPercentage;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveFixedPercentage;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;

import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.ColorProperties;
import com.vaadin.addon.spreadsheet.charts.converter.chartdata.ChartData.GradientProperties;

/*
 * #%L
 * Vaadin Spreadsheet Charts Integration
 * %%
 * Copyright (C) 2016 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class ColorUtils {

    private static Logger logger = Logger.getLogger(ColorUtils.class.getName());

    // this much would mean 360 degrees
    private static final double ANGLE_FACTOR = 21600000.0;

    private static final float PERCENTAGE_FACTOR = 100000f;

    private static class ColorParameters {
        public byte[] rgb;
        public float lumMod;
        public float lumOff;
        public float alpha = 1;
    }

    public static Map<String, byte[]> createColorMap(CTColorScheme clrScheme) {
        Map<String, byte[]> colorMap = new HashMap<String, byte[]>();

        if (clrScheme.getDk1().getSysClr() != null) {
            colorMap.put("tx1", clrScheme.getDk1().getSysClr().getLastClr());
            colorMap.put("bg1", clrScheme.getLt1().getSysClr().getLastClr());
        }
        colorMap.put("tx2", clrScheme.getDk2().getSrgbClr().getVal());
        colorMap.put("bg2", clrScheme.getLt2().getSrgbClr().getVal());
        colorMap.put("accent1", clrScheme.getAccent1().getSrgbClr().getVal());

        colorMap.put("accent2", clrScheme.getAccent2().getSrgbClr().getVal());
        colorMap.put("accent3", clrScheme.getAccent3().getSrgbClr().getVal());
        colorMap.put("accent4", clrScheme.getAccent4().getSrgbClr().getVal());
        colorMap.put("accent5", clrScheme.getAccent5().getSrgbClr().getVal());
        colorMap.put("accent6", clrScheme.getAccent6().getSrgbClr().getVal());
        // colorMap.put("hlink",
        // clrScheme.getHlink().getSrgbClr().getVal());
        // colorMap.put("folHlink",
        // clrScheme.getFolHlink().getSrgbClr().getVal());

        return colorMap;
    }

    public static GradientProperties createGradientProperties(
            CTGradientFillProperties gradFill, Map<String, byte[]> colorMap) {
        GradientProperties gradientProp = new GradientProperties();

        for (CTGradientStop stop : gradFill.getGsLst().getGsList()) {
            ColorProperties stopClrProp = createColorPropertiesFromGradientStop(
                    stop, colorMap);

            gradientProp.colorStops.put(
                    stop.getPos() / (double) PERCENTAGE_FACTOR, stopClrProp);
        }

        if (gradFill.isSetLin() && gradFill.getLin().isSetAng())
            gradientProp.angle = gradFill.getLin().getAng() / ANGLE_FACTOR;

        return gradientProp;
    }

    public static ColorProperties createColorPropertiesFromFill(
            CTSolidColorFillProperties solidFill,
            Map<String, byte[]> colorMap) {
        if (solidFill == null)
            return null;

        ColorParameters clr = null;

        if (solidFill.isSetSchemeClr()) {
            clr = getParametersFromSchemeClr(solidFill.getSchemeClr(),
                    colorMap);
        } else if (solidFill.isSetSrgbClr()) {
            clr = getColorParametersFromSrgb(solidFill.getSrgbClr());
        } else {
            logger.warning("Unhandled color fill \n" + solidFill);
            return null;
        }

        return ColorUtils.createColorPropertiesFromParameters(clr);
    }

    private static ColorParameters getParametersFromSchemeClr(
            CTSchemeColor schemeClr, Map<String, byte[]> colorMap) {
        ColorParameters clr = new ColorParameters();

        String colorName = schemeClr.getVal().toString();

        if (!colorMap.containsKey(colorName)) {
            logger.warning("Color " + colorName + " is not in the color table");
            return null;
        }

        clr.rgb = colorMap.get(colorName);
        clr.lumMod = getLum(schemeClr.getLumModList());
        clr.lumOff = getLum(schemeClr.getLumOffList());
        clr.alpha = getAlpha(schemeClr.getAlphaList());

        return clr;
    }

    private static ColorProperties createColorPropertiesFromGradientStop(
            CTGradientStop gradientStop, Map<String, byte[]> colorMap) {
        if (gradientStop == null)
            return null;

        ColorParameters clr = null;

        if (gradientStop.isSetSchemeClr()) {
            clr = getParametersFromSchemeClr(gradientStop.getSchemeClr(),
                    colorMap);
        } else if (gradientStop.isSetSrgbClr()) {
            clr = getColorParametersFromSrgb(gradientStop.getSrgbClr());
        } else {
            logger.warning("Unhandled color fill \n" + gradientStop);
            return null;
        }

        return ColorUtils.createColorPropertiesFromParameters(clr);
    }

    private static ColorParameters getColorParametersFromSrgb(
            CTSRgbColor srgbClr) {
        ColorParameters clr = new ColorParameters();

        clr.rgb = srgbClr.getVal();
        clr.lumMod = getLum(srgbClr.getLumModList());
        clr.lumOff = getLum(srgbClr.getLumOffList());
        clr.alpha = getAlpha(srgbClr.getAlphaList());

        return clr;
    }

    private static float getLum(List<CTPercentage> list) {
        if (list.size() > 0)
            return list.get(0).getVal() / PERCENTAGE_FACTOR;
        else
            return 0;
    }

    private static float getAlpha(List<CTPositiveFixedPercentage> alphaList) {
        if (alphaList.size() > 0)
            return alphaList.get(0).getVal() / PERCENTAGE_FACTOR;
        else
            return 1;
    }

    private static ColorProperties createColorPropertiesFromParameters(
            ColorParameters par) {
        if (par == null)
            return null;

        byte[] rgbWithLum = ColorUtils.applyLum(par.rgb, par.lumMod,
                par.lumOff);
        int[] rgbUnsignedWithLum = ColorUtils
                .convertToUnsignedRange(rgbWithLum);

        return new ColorProperties(rgbUnsignedWithLum, par.alpha);
    }

    /**
     * Converts from -127..128 range to 0..255
     */
    private static int[] convertToUnsignedRange(byte[] signedRange) {
        return new int[] { signedRange[0] & 0xFF, signedRange[1] & 0xFF,
                signedRange[2] & 0xFF };
    }

    /**
     * Apply lum modifications according to the forumula from here:
     * https://msdn.microsoft.com/
     * en-us/library/office/dd560821(v=office.12).aspx
     */
    private static byte[] applyLum(byte[] rgb, float lumMod, float lumOff) {
        if (lumMod == 0)
            return rgb;

        float[] hsl = toHSL(rgb);

        // convert to range 0..1
        final float lum = hsl[2] / 100;

        // the formula itself
        final float newLum = lum * lumMod + lumOff;

        hsl[2] = newLum * 100;

        return toRGB(hsl[0], hsl[1], hsl[2]);
    }

    private static float[] toHSL(byte[] rgb) {
        float r = (rgb[0] & 0xFF) / 255.0f;
        float g = (rgb[1] & 0xFF) / 255.0f;
        float b = (rgb[2] & 0xFF) / 255.0f;

        // Minimum and Maximum RGB values are used in the HSL calculations

        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));

        // Calculate the Hue

        float h = 0;

        if (max == min)
            h = 0;
        else if (max == r)
            h = ((60 * (g - b) / (max - min)) + 360) % 360;
        else if (max == g)
            h = (60 * (b - r) / (max - min)) + 120;
        else if (max == b)
            h = (60 * (r - g) / (max - min)) + 240;

        // Calculate the Luminance

        float l = (max + min) / 2;

        // Calculate the Saturation

        float s = 0;

        if (max == min)
            s = 0;
        else if (l <= .5f)
            s = (max - min) / (max + min);
        else
            s = (max - min) / (2 - max - min);

        return new float[] { h, s * 100, l * 100 };
    }

    private static byte[] toRGB(float h, float s, float l) {
        h = h % 360.0f;
        h /= 360f;
        s /= 100f;
        l /= 100f;

        float q = 0;

        if (l < 0.5)
            q = l * (1 + s);
        else
            q = (l + s) - (s * l);

        float p = 2 * l - q;

        float r = Math.max(0, HueToRGB(p, q, h + (1.0f / 3.0f)));
        float g = Math.max(0, HueToRGB(p, q, h));
        float b = Math.max(0, HueToRGB(p, q, h - (1.0f / 3.0f)));

        r = Math.min(r, 1.0f);
        g = Math.min(g, 1.0f);
        b = Math.min(b, 1.0f);

        byte r_byte = (byte) Math.round(r * 255);
        byte g_byte = (byte) Math.round(g * 255);
        byte b_byte = (byte) Math.round(b * 255);

        return new byte[] { r_byte, g_byte, b_byte };
    }

    private static float HueToRGB(float p, float q, float h) {
        if (h < 0)
            h += 1;

        if (h > 1)
            h -= 1;

        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }

        if (2 * h < 1) {
            return q;
        }

        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }

        return p;
    }
}
