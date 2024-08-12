/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.style;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.ConfigurationTestUtil;

public class TextStyleTest {
    private TextStyle textStyle;

    @Before
    public void setup() {
        textStyle = new TextStyle();
    }

    @Test
    public void defaults() throws NoSuchFieldException, IllegalAccessException {
        // Verify initial fill and stroke are added as children
        Set<AbstractConfigurationObject> children = ConfigurationTestUtil
                .getChildren(textStyle);
        Assert.assertTrue(children.contains(textStyle.getFill()));
        Assert.assertTrue(children.contains(textStyle.getStroke()));
    }

    @Test
    public void setOffsetFromPixelValues_createsOffset() {
        textStyle.setOffset(123, 456);

        Assert.assertNotNull(textStyle.getOffset());
        Assert.assertEquals(123, textStyle.getOffset().getX(), 0);
        Assert.assertEquals(456, textStyle.getOffset().getY(), 0);
    }

    @Test
    public void setFillFromColor_createsFill() {
        textStyle.setFill("#123");

        Assert.assertNotNull(textStyle.getFill());
        Assert.assertEquals("#123", textStyle.getFill().getColor());
    }

    @Test
    public void setStrokeFromColorAndWidth_createsStroke() {
        textStyle.setStroke("#123", 123);

        Assert.assertNotNull(textStyle.getStroke());
        Assert.assertEquals("#123", textStyle.getStroke().getColor());
        Assert.assertEquals(123, textStyle.getStroke().getWidth(), 0);
    }

    @Test
    public void setBackgroundFillFromColor_createsFill() {
        textStyle.setBackgroundFill("#123");

        Assert.assertNotNull(textStyle.getBackgroundFill());
        Assert.assertEquals("#123", textStyle.getBackgroundFill().getColor());
    }

    @Test
    public void setBackgroundStrokeFromColorAndWidth_createsStroke() {
        textStyle.setBackgroundStroke("#123", 123);

        Assert.assertNotNull(textStyle.getBackgroundStroke());
        Assert.assertEquals("#123", textStyle.getBackgroundStroke().getColor());
        Assert.assertEquals(123, textStyle.getBackgroundStroke().getWidth(), 0);
    }
}
