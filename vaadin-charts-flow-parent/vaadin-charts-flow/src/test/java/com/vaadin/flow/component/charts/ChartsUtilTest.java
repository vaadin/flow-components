/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.time.Instant;
import com.vaadin.flow.component.charts.util.Util;

import org.junit.Assert;
import org.junit.Test;

public class ChartsUtilTest {

    @Test
    public void getInstantFromUnixTimestamp() {
        long unixTimestampMillis = 1546300800000L;
        Instant instant = Util.toServerInstant(unixTimestampMillis);
        Assert.assertEquals(unixTimestampMillis, instant.toEpochMilli());
    }
}
