/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.util.Util;

class ChartsUtilTest {

    @Test
    void getInstantFromUnixTimestamp() {
        long unixTimestampMillis = 1546300800000L;
        Instant instant = Util.toServerInstant(unixTimestampMillis);
        Assertions.assertEquals(unixTimestampMillis, instant.toEpochMilli());
    }
}
