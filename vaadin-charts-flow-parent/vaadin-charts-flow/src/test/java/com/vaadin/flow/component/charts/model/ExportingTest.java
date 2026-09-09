/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExportingTest {

    @Test
    void setUrl_safeScheme_urlSet() {
        Exporting exporting = new Exporting();
        exporting.setUrl("https://export.highcharts.com");

        Assertions.assertEquals("https://export.highcharts.com",
                exporting.getUrl());
    }

    @Test
    void setUrl_unsafeScheme_throws() {
        Exporting exporting = new Exporting();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> exporting.setUrl("javascript:alert(1)"));
    }

    @Test
    void setUnsafeUrl_unsafeScheme_urlSet() {
        Exporting exporting = new Exporting();
        exporting.setUnsafeUrl("javascript:alert(1)");

        Assertions.assertEquals("javascript:alert(1)", exporting.getUrl());
    }

    @Test
    void setLibURL_safeScheme_libURLSet() {
        Exporting exporting = new Exporting();
        exporting.setLibURL("https://code.highcharts.com/lib");

        Assertions.assertEquals("https://code.highcharts.com/lib",
                exporting.getLibURL());
    }

    @Test
    void setLibURL_unsafeScheme_throws() {
        Exporting exporting = new Exporting();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> exporting.setLibURL("javascript:alert(1)"));
    }

    @Test
    void setUnsafeLibURL_unsafeScheme_libURLSet() {
        Exporting exporting = new Exporting();
        exporting.setUnsafeLibURL("javascript:alert(1)");

        Assertions.assertEquals("javascript:alert(1)", exporting.getLibURL());
    }
}
