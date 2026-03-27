/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.map.configuration.View;

class ViewTest {

    @Test
    void setCenter_doesNotAllowNullValue() {
        View view = new View();

        Assertions.assertThrows(NullPointerException.class,
                () -> view.setCenter(null));
    }

    @Test
    void viewProjectionDefaultIsSet() {
        View view = new View();

        Assertions.assertEquals("EPSG:3857", view.getProjection());
    }

    @Test
    void viewProjectionDefaultCanBeChanged() {
        View view = new View("EPSG:4326");

        Assertions.assertEquals("EPSG:4326", view.getProjection());
    }
}
