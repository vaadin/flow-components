/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.util;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Util {

    /**
     * @deprecated as of 4.0. Use {@link #toHighchartsTS(Instant)}
     */
    @Deprecated
    public static long toHighchartsTS(Date date) {
        return date.getTime() - date.getTimezoneOffset() * 60000;
    }

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
     * @deprecated as of 4.0. Use {@link #toServerInstant(double)}
     */
    @Deprecated
    public static Date toServerDate(double rawClientSideValue) {
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        instance.setTimeInMillis((long) rawClientSideValue);
        // fix one field to force calendar re-adjust the value
        instance.set(Calendar.MINUTE, instance.get(Calendar.MINUTE));
        instance.setTimeZone(TimeZone.getDefault());
        return instance.getTime();
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
