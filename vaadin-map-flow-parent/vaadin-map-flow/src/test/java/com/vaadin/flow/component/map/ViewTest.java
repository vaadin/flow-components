/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.map.configuration.View;

public class ViewTest {

    @Test
    public void setCenter_doesNotAllowNullValue() {
        View view = new View();

        Assert.assertThrows(NullPointerException.class,
                () -> view.setCenter(null));
    }

    @Test
    public void viewProjectionDefaultIsSet() {
        View view = new View();

        Assert.assertEquals("EPSG:3857", view.getProjection());
    }

    @Test
    public void viewProjectionDefaultCanBeChanged() {
        View view = new View("EPSG:4326");

        Assert.assertEquals("EPSG:4326", view.getProjection());
    }
}
