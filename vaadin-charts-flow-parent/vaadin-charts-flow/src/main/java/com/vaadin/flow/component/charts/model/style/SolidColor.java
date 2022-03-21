package com.vaadin.flow.component.charts.model.style;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;

import java.util.Locale;

/**
 * Solid (non gradient) colors
 */
@SuppressWarnings("serial")
public class SolidColor extends AbstractConfigurationObject implements Color {

    public final static SolidColor ALICEBLUE = new SolidColor("#F0F8FF");
    public final static SolidColor ANTIQUEWHITE = new SolidColor("#FAEBD7");
    public final static SolidColor AQUA = new SolidColor("#00FFFF");
    public final static SolidColor AQUAMARINE = new SolidColor("#7FFFD4");
    public final static SolidColor AZURE = new SolidColor("#F0FFFF");
    public final static SolidColor BEIGE = new SolidColor("#F5F5DC");
    public final static SolidColor BISQUE = new SolidColor("#FFE4C4");
    public final static SolidColor BLACK = new SolidColor("#000000");
    public final static SolidColor BLANCHEDALMOND = new SolidColor("#FFEBCD");
    public final static SolidColor BLUE = new SolidColor("#0000FF");
    public final static SolidColor BLUEVIOLET = new SolidColor("#8A2BE2");
    public final static SolidColor BROWN = new SolidColor("#A52A2A");
    public final static SolidColor BURLYWOOD = new SolidColor("#DEB887");
    public final static SolidColor CADETBLUE = new SolidColor("#5F9EA0");
    public final static SolidColor CHARTREUSE = new SolidColor("#7FFF00");
    public final static SolidColor CHOCOLATE = new SolidColor("#D2691E");
    public final static SolidColor CORAL = new SolidColor("#FF7F50");
    public final static SolidColor CORNFLOWERBLUE = new SolidColor("#6495ED");
    public final static SolidColor CORNSILK = new SolidColor("#FFF8DC");
    public final static SolidColor CRIMSON = new SolidColor("#DC143C");
    public final static SolidColor CYAN = new SolidColor("#00FFFF");
    public final static SolidColor DARKBLUE = new SolidColor("#00008B");
    public final static SolidColor DARKCYAN = new SolidColor("#008B8B");
    public final static SolidColor DARKGOLDENROD = new SolidColor("#B8860B");
    public final static SolidColor DARKGRAY = new SolidColor("#A9A9A9");
    public final static SolidColor DARKGREY = new SolidColor("#A9A9A9");
    public final static SolidColor DARKGREEN = new SolidColor("#006400");
    public final static SolidColor DARKKHAKI = new SolidColor("#BDB76B");
    public final static SolidColor DARKMAGENTA = new SolidColor("#8B008B");
    public final static SolidColor DARKOLIVEGREEN = new SolidColor("#556B2F");
    public final static SolidColor DARKORANGE = new SolidColor("#FF8C00");
    public final static SolidColor DARKORCHID = new SolidColor("#9932CC");
    public final static SolidColor DARKRED = new SolidColor("#8B0000");
    public final static SolidColor DARKSALMON = new SolidColor("#E9967A");
    public final static SolidColor DARKSEAGREEN = new SolidColor("#8FBC8F");
    public final static SolidColor DARKSLATEBLUE = new SolidColor("#483D8B");
    public final static SolidColor DARKSLATEGRAY = new SolidColor("#2F4F4F");
    public final static SolidColor DARKSLATEGREY = new SolidColor("#2F4F4F");
    public final static SolidColor DARKTURQUOISE = new SolidColor("#00CED1");
    public final static SolidColor DARKVIOLET = new SolidColor("#9400D3");
    public final static SolidColor DEEPPINK = new SolidColor("#FF1493");
    public final static SolidColor DEEPSKYBLUE = new SolidColor("#00BFFF");
    public final static SolidColor DIMGRAY = new SolidColor("#696969");
    public final static SolidColor DIMGREY = new SolidColor("#696969");
    public final static SolidColor DODGERBLUE = new SolidColor("#1E90FF");
    public final static SolidColor FIREBRICK = new SolidColor("#B22222");
    public final static SolidColor FLORALWHITE = new SolidColor("#FFFAF0");
    public final static SolidColor FORESTGREEN = new SolidColor("#228B22");
    public final static SolidColor FUCHSIA = new SolidColor("#FF00FF");
    public final static SolidColor GAINSBORO = new SolidColor("#DCDCDC");
    public final static SolidColor GHOSTWHITE = new SolidColor("#F8F8FF");
    public final static SolidColor GOLD = new SolidColor("#FFD700");
    public final static SolidColor GOLDENROD = new SolidColor("#DAA520");
    public final static SolidColor GRAY = new SolidColor("#808080");
    public final static SolidColor GREY = new SolidColor("#808080");
    public final static SolidColor GREEN = new SolidColor("#008000");
    public final static SolidColor GREENYELLOW = new SolidColor("#ADFF2F");
    public final static SolidColor HONEYDEW = new SolidColor("#F0FFF0");
    public final static SolidColor HOTPINK = new SolidColor("#FF69B4");
    public final static SolidColor INDIANRED = new SolidColor("#CD5C5C");
    public final static SolidColor INDIGO = new SolidColor("#4B0082");
    public final static SolidColor IVORY = new SolidColor("#FFFFF0");
    public final static SolidColor KHAKI = new SolidColor("#F0E68C");
    public final static SolidColor LAVENDER = new SolidColor("#E6E6FA");
    public final static SolidColor LAVENDERBLUSH = new SolidColor("#FFF0F5");
    public final static SolidColor LAWNGREEN = new SolidColor("#7CFC00");
    public final static SolidColor LEMONCHIFFON = new SolidColor("#FFFACD");
    public final static SolidColor LIGHTBLUE = new SolidColor("#ADD8E6");
    public final static SolidColor LIGHTCORAL = new SolidColor("#F08080");
    public final static SolidColor LIGHTCYAN = new SolidColor("#E0FFFF");
    public final static SolidColor LIGHTGOLDENRODYELLOW = new SolidColor(
            "#FAFAD2");
    public final static SolidColor LIGHTGRAY = new SolidColor("#D3D3D3");
    public final static SolidColor LIGHTGREY = new SolidColor("#D3D3D3");
    public final static SolidColor LIGHTGREEN = new SolidColor("#90EE90");
    public final static SolidColor LIGHTPINK = new SolidColor("#FFB6C1");
    public final static SolidColor LIGHTSALMON = new SolidColor("#FFA07A");
    public final static SolidColor LIGHTSEAGREEN = new SolidColor("#20B2AA");
    public final static SolidColor LIGHTSKYBLUE = new SolidColor("#87CEFA");
    public final static SolidColor LIGHTSLATEGRAY = new SolidColor("#778899");
    public final static SolidColor LIGHTSLATEGREY = new SolidColor("#778899");
    public final static SolidColor LIGHTSTEELBLUE = new SolidColor("#B0C4DE");
    public final static SolidColor LIGHTYELLOW = new SolidColor("#FFFFE0");
    public final static SolidColor LIME = new SolidColor("#00FF00");
    public final static SolidColor LIMEGREEN = new SolidColor("#32CD32");
    public final static SolidColor LINEN = new SolidColor("#FAF0E6");
    public final static SolidColor MAGENTA = new SolidColor("#FF00FF");
    public final static SolidColor MAROON = new SolidColor("#800000");
    public final static SolidColor MEDIUMAQUAMARINE = new SolidColor("#66CDAA");
    public final static SolidColor MEDIUMBLUE = new SolidColor("#0000CD");
    public final static SolidColor MEDIUMORCHID = new SolidColor("#BA55D3");
    public final static SolidColor MEDIUMPURPLE = new SolidColor("#9370DB");
    public final static SolidColor MEDIUMSEAGREEN = new SolidColor("#3CB371");
    public final static SolidColor MEDIUMSLATEBLUE = new SolidColor("#7B68EE");
    public final static SolidColor MEDIUMSPRINGGREEN = new SolidColor(
            "#00FA9A");
    public final static SolidColor MEDIUMTURQUOISE = new SolidColor("#48D1CC");
    public final static SolidColor MEDIUMVIOLETRED = new SolidColor("#C71585");
    public final static SolidColor MIDNIGHTBLUE = new SolidColor("#191970");
    public final static SolidColor MINTCREAM = new SolidColor("#F5FFFA");
    public final static SolidColor MISTYROSE = new SolidColor("#FFE4E1");
    public final static SolidColor MOCCASIN = new SolidColor("#FFE4B5");
    public final static SolidColor NAVAJOWHITE = new SolidColor("#FFDEAD");
    public final static SolidColor NAVY = new SolidColor("#000080");
    public final static SolidColor OLDLACE = new SolidColor("#FDF5E6");
    public final static SolidColor OLIVE = new SolidColor("#808000");
    public final static SolidColor OLIVEDRAB = new SolidColor("#6B8E23");
    public final static SolidColor ORANGE = new SolidColor("#FFA500");
    public final static SolidColor ORANGERED = new SolidColor("#FF4500");
    public final static SolidColor ORCHID = new SolidColor("#DA70D6");
    public final static SolidColor PALEGOLDENROD = new SolidColor("#EEE8AA");
    public final static SolidColor PALEGREEN = new SolidColor("#98FB98");
    public final static SolidColor PALETURQUOISE = new SolidColor("#AFEEEE");
    public final static SolidColor PALEVIOLETRED = new SolidColor("#DB7093");
    public final static SolidColor PAPAYAWHIP = new SolidColor("#FFEFD5");
    public final static SolidColor PEACHPUFF = new SolidColor("#FFDAB9");
    public final static SolidColor PERU = new SolidColor("#CD853F");
    public final static SolidColor PINK = new SolidColor("#FFC0CB");
    public final static SolidColor PLUM = new SolidColor("#DDA0DD");
    public final static SolidColor POWDERBLUE = new SolidColor("#B0E0E6");
    public final static SolidColor PURPLE = new SolidColor("#800080");
    public final static SolidColor RED = new SolidColor("#FF0000");
    public final static SolidColor ROSYBROWN = new SolidColor("#BC8F8F");
    public final static SolidColor ROYALBLUE = new SolidColor("#4169E1");
    public final static SolidColor SADDLEBROWN = new SolidColor("#8B4513");
    public final static SolidColor SALMON = new SolidColor("#FA8072");
    public final static SolidColor SANDYBROWN = new SolidColor("#F4A460");
    public final static SolidColor SEAGREEN = new SolidColor("#2E8B57");
    public final static SolidColor SEASHELL = new SolidColor("#FFF5EE");
    public final static SolidColor SIENNA = new SolidColor("#A0522D");
    public final static SolidColor SILVER = new SolidColor("#C0C0C0");
    public final static SolidColor SKYBLUE = new SolidColor("#87CEEB");
    public final static SolidColor SLATEBLUE = new SolidColor("#6A5ACD");
    public final static SolidColor SLATEGRAY = new SolidColor("#708090");
    public final static SolidColor SLATEGREY = new SolidColor("#708090");
    public final static SolidColor SNOW = new SolidColor("#FFFAFA");
    public final static SolidColor SPRINGGREEN = new SolidColor("#00FF7F");
    public final static SolidColor STEELBLUE = new SolidColor("#4682B4");
    public final static SolidColor TAN = new SolidColor("#D2B48C");
    public final static SolidColor TEAL = new SolidColor("#008080");
    public final static SolidColor THISTLE = new SolidColor("#D8BFD8");
    public final static SolidColor TOMATO = new SolidColor("#FF6347");
    public final static SolidColor TURQUOISE = new SolidColor("#40E0D0");
    public final static SolidColor VIOLET = new SolidColor("#EE82EE");
    public final static SolidColor WHEAT = new SolidColor("#F5DEB3");
    public final static SolidColor WHITE = new SolidColor("#FFFFFF");
    public final static SolidColor WHITESMOKE = new SolidColor("#F5F5F5");
    public final static SolidColor YELLOW = new SolidColor("#FFFF00");
    public final static SolidColor YELLOWGREEN = new SolidColor("#9ACD32");

    private final String color;

    /**
     * Constructs a new color from a hex value like "#ff0000" for red.
     *
     * @param color
     */
    public SolidColor(String color) {

        // Sanitize value to avoid cross site attacks
        this.color = color.replaceAll("[^0-9a-z,A-Z#]", "");

    }

    /**
     * Constructs a color from RGB values
     *
     * @param red
     *            Red value (0...255)
     * @param green
     *            Green value (0...255)
     * @param blue
     *            Blue value (0...255)
     */
    public SolidColor(int red, int green, int blue) {
        if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0
                || blue > 255) {
            throw new IllegalArgumentException("Invalid color values given.");
        }

        color = String.format(Locale.ENGLISH, "#%02X%02X%02X", red, green,
                blue);
    }

    /**
     * Constructs a color from RGBA values
     *
     * @param red
     *            Red value (0...255)
     * @param green
     *            Green value (0...255)
     * @param blue
     *            Blue value (0...255)
     * @param opacity
     *            Opacity of color (0.0 ... 1.0)
     */
    public SolidColor(int red, int green, int blue, double opacity) {
        if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0
                || blue > 255 || opacity < 0.0 || opacity > 1.0) {
            throw new IllegalArgumentException(String.format(
                    "Invalid color values given. Red: %s, green: %s blue: %s, opacity: %s",
                    red, green, blue, opacity));
        }

        color = String.format(Locale.ENGLISH, "rgba(%d,%d,%d,%.2f)", red, green,
                blue, opacity);
    }

    @Override
    public String toString() {
        return color;
    }

}
