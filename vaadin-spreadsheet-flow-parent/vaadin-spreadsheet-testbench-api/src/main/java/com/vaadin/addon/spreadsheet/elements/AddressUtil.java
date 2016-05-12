package com.vaadin.addon.spreadsheet.elements;

/*
 * #%L
 * Vaadin Spreadsheet Testbench API
 * %%
 * Copyright (C) 2013 - 2016 Vaadin Ltd
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
        int row = Integer.valueOf(m.group(2));

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
     *            The character address
     * @return The numeric value of the address
     */
    private static int charAddressToInt(String address) {
        address = address.toUpperCase();
        int result = 0;
        String reversed = new StringBuffer(address).reverse().toString();
        for (int i = 0; i < reversed.length(); i++) {
            result += ((reversed.charAt(i) - 'A') + 1) * Math.pow(26, i);
        }
        return result;
    }
}
