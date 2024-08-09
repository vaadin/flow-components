/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
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

        Assert.assertEquals(view.getProjection(), "EPSG:3857");
    }

    @Test
    public void viewProjectionDefaultCanBeChanged() {
        View view = new View("EPSG:4326");

        Assert.assertEquals(view.getProjection(), "EPSG:4326");
    }
}
