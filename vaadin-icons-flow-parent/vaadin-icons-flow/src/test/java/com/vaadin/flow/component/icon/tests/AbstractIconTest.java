/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.icon.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.dom.ElementConstants;

public class AbstractIconTest {
    private AbstractIcon abstractIcon;

    @Before
    public void init() {
        abstractIcon = new AbstractIcon() {
            @Override
            public void setColor(String color) {
                this.getStyle().setColor(color);
            }

            @Override
            public String getColor() {
                return this.getStyle().get("color");
            }
        };
    }

    @Test
    public void setSize() {
        abstractIcon.setSize("100px");

        Assert.assertEquals("100px",
                abstractIcon.getStyle().get(ElementConstants.STYLE_HEIGHT));
        Assert.assertEquals("100px",
                abstractIcon.getStyle().get(ElementConstants.STYLE_WIDTH));

        abstractIcon.setSize(null);

        Assert.assertNull(
                abstractIcon.getStyle().get(ElementConstants.STYLE_HEIGHT));
        Assert.assertNull(
                abstractIcon.getStyle().get(ElementConstants.STYLE_WIDTH));
    }
}
