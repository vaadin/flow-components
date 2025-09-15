/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.util;

import java.time.Instant;

public class Util {

    /**
     * Gets the number of miliseconds from the Java epoch of
     * 1970-01-01T00:00:00Z.
     *
     * @param date
     * @return
     */
    public static long toHighchartsTS(Instant date) {
        return date.getEpochSecond() * 1000;
    }

    /**
     * Converts UTC based raw date value from the client side rendering library
     * to an Instant value.
     *
     * @param rawClientSideValue
     *            the raw value from the client side
     * @return an Instant value
     */
    public static Instant toServerInstant(double rawClientSideValue) {
        return Instant.ofEpochMilli((long) rawClientSideValue);
    }

}
