package com.vaadin.addon.spreadsheet.test.pageobjects;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Point;

/**
 * Can convert from excel type addresses to integer coordinates.
 */
public class AddressUtil {

    public static final Pattern ADDRESS_RANGE_PATTERN = Pattern
            .compile("([A-Z]+)(\\d+):([A-Z]+)(\\d+)");
    public static final Pattern ADDRESS_PATTERN = Pattern
            .compile("([A-Z]+)(\\d+)");

    /**
     * Converts a single cell address to its integer coordinates (Point)
     * 
     * @param address
     *            the address of the cell
     * @return the coordinates of the cell
     */
    public static Point addressToPoint(String address) {
        Matcher m = ADDRESS_PATTERN.matcher(address);
        m.find();

        int col = charAddressToInt(m.group(1));
        int row = Integer.valueOf(m.group(2));

        return new Point(col, row);
    }

    /**
     * Converts an address range to a set of coordinates (Points)
     * 
     * @param addressRange
     *            the address range, e.g. A1:B3, AA20:AZ98, etc.
     * @return a set of integer coordinates for all cells in the specified
     *         range.
     */
    public static Set<Point> addressRangeToPoints(String addressRange) {
        Matcher m = ADDRESS_RANGE_PATTERN.matcher(addressRange);
        m.find();

        int left = charAddressToInt(m.group(1));
        int top = Integer.valueOf(m.group(2));

        int right = charAddressToInt(m.group(3));
        int bottom = Integer.valueOf(m.group(4));

        HashSet<Point> points = new HashSet<Point>();
        for (int col = left; col <= right; col++) {
            for (int row = top; row <= bottom; row++) {
                points.add(new Point(col, row));
            }
        }
        return points;
    }

    /**
     * Converts a character address (like "A", "BA", "AFK") to a numeric one
     * 
     * @param address
     *            the address
     * @return the numeric value of the address
     */
    private static int charAddressToInt(String address) {
        int result = 0;
        String reversed = new StringBuffer(address).reverse().toString();
        for (int i = 0; i < reversed.length(); i++) {
            result += ((reversed.charAt(i) - 'A') + 1) * Math.pow(26, i);
        }
        return result;
    }
}
