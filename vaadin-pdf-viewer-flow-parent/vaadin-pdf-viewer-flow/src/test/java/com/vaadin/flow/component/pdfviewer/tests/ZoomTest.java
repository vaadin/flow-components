/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.pdfviewer.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.vaadin.flow.component.pdfviewer.Zoom;

class ZoomTest {

    @Test
    void pageWidth() {
        Assertions.assertTrue(Zoom.PAGE_WIDTH.getFactor().isEmpty());
        Assertions.assertEquals("page-width", Zoom.PAGE_WIDTH.toString());
    }

    @Test
    void pageFit() {
        Assertions.assertTrue(Zoom.PAGE_FIT.getFactor().isEmpty());
        Assertions.assertEquals("page-fit", Zoom.PAGE_FIT.toString());
    }

    @Test
    void of_factor() {
        var zoom = Zoom.of(1.5);
        Assertions.assertEquals(1.5, zoom.getFactor().orElseThrow());
        Assertions.assertEquals("1.5", zoom.toString());
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0, -1, Double.NaN, Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY })
    void of_invalidFactor_throws(double factor) {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Zoom.of(factor));
    }

    @Test
    void equalsAndHashCode() {
        Assertions.assertEquals(Zoom.of(1.5), Zoom.of(1.5));
        Assertions.assertEquals(Zoom.of(1.5).hashCode(),
                Zoom.of(1.5).hashCode());
        Assertions.assertNotEquals(Zoom.of(1.5), Zoom.of(2));
        Assertions.assertNotEquals(Zoom.PAGE_WIDTH, Zoom.PAGE_FIT);
        Assertions.assertNotEquals(Zoom.PAGE_WIDTH, Zoom.of(1));
        Assertions.assertNotEquals(Zoom.PAGE_WIDTH, null);
        Assertions.assertNotEquals(Zoom.PAGE_WIDTH, "page-width");
    }
}
