package com.vaadin.flow.component.spreadsheet.testbench;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Point;

/**
 * An utility class that converts Excel cell addresses to integer coordinates.
 *
 * @author Vaadin Ltd.
 */
public class AddressUtil implements Serializable {

    private static final Pattern ADDRESS_RANGE_PATTERN = Pattern
            .compile("([A-Z]+)(\\d+):([A-Z]+)(\\d+)");
    private static final Pattern ADDRESS_PATTERN = Pattern
            .compile("([A-Z]+)(\\d+)");

    /**
     * Converts a single cell address to its integer coordinates (Point)
     *
     * @param address
     *            The address of the cell, e.g. A3
     * @return the coordinates of the cell
     */
    public static Point addressToPoint(String address) {
        Matcher m = ADDRESS_PATTERN.matcher(address);
        m.find();

        int col = charAddressToInt(m.group(1));
        int row = Integer.parseInt(m.group(2));

        return new Point(col, row);
    }

    /**
     * Converts an address range to a set of coordinates (Points)
     *
     * @param addressRange
     *            The address range, e.g. A1:B3, AA20:AZ98, etc.
     * @return A set of integer coordinates for all cells in the specified
     *         range.
     */
    public static Set<Point> addressRangeToPoints(String addressRange) {
        Matcher m = ADDRESS_RANGE_PATTERN.matcher(addressRange);
        m.find();

        int left = charAddressToInt(m.group(1));
        int top = Integer.parseInt(m.group(2));

        int right = charAddressToInt(m.group(3));
        int bottom = Integer.parseInt(m.group(4));

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
     *            The character address
     * @return The numeric value of the address
     */
    private static int charAddressToInt(String address) {
        int result = 0;
        String reversed = new StringBuffer(address.toUpperCase()).reverse()
                .toString();
        for (int i = 0; i < reversed.length(); i++) {
            result += ((reversed.charAt(i) - 'A') + 1) * Math.pow(26, i);
        }
        return result;
    }
}
