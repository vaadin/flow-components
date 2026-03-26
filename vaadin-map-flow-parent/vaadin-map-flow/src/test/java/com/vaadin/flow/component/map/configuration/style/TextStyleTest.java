/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.style;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.map.configuration.AbstractConfigurationObject;
import com.vaadin.flow.component.map.configuration.ConfigurationTestUtil;

class TextStyleTest {
    private TextStyle textStyle;

    @BeforeEach
    void setup() {
        textStyle = new TextStyle();
    }

    @Test
    void defaults() throws NoSuchFieldException, IllegalAccessException {
        // Verify initial fill and stroke are added as children
        Set<AbstractConfigurationObject> children = ConfigurationTestUtil
                .getChildren(textStyle);
        Assertions.assertTrue(children.contains(textStyle.getFill()));
        Assertions.assertTrue(children.contains(textStyle.getStroke()));
    }

    @Test
    void setOffsetFromPixelValues_createsOffset() {
        textStyle.setOffset(123, 456);

        Assertions.assertNotNull(textStyle.getOffset());
        Assertions.assertEquals(123, textStyle.getOffset().getX(), 0);
        Assertions.assertEquals(456, textStyle.getOffset().getY(), 0);
    }

    @Test
    void setFillFromColor_createsFill() {
        textStyle.setFill("#123");

        Assertions.assertNotNull(textStyle.getFill());
        Assertions.assertEquals("#123", textStyle.getFill().getColor());
    }

    @Test
    void setStrokeFromColorAndWidth_createsStroke() {
        textStyle.setStroke("#123", 123);

        Assertions.assertNotNull(textStyle.getStroke());
        Assertions.assertEquals("#123", textStyle.getStroke().getColor());
        Assertions.assertEquals(123, textStyle.getStroke().getWidth(), 0);
    }

    @Test
    void setBackgroundFillFromColor_createsFill() {
        textStyle.setBackgroundFill("#123");

        Assertions.assertNotNull(textStyle.getBackgroundFill());
        Assertions.assertEquals("#123",
                textStyle.getBackgroundFill().getColor());
    }

    @Test
    void setBackgroundStrokeFromColorAndWidth_createsStroke() {
        textStyle.setBackgroundStroke("#123", 123);

        Assertions.assertNotNull(textStyle.getBackgroundStroke());
        Assertions.assertEquals("#123",
                textStyle.getBackgroundStroke().getColor());
        Assertions.assertEquals(123, textStyle.getBackgroundStroke().getWidth(),
                0);
    }
}
