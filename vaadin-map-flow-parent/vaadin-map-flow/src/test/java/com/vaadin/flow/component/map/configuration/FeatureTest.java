/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.component.map.configuration.style.TextStyle;

class FeatureTest {

    private Feature feature;

    @BeforeEach
    void setup() {
        feature = new TestFeature();
    }

    @Test
    void getTextStyle_nullByDefault() {
        Assertions.assertNull(feature.getTextStyle());
    }

    @Test
    void setTextStyle_doesNotCreateStyleIfTextStyleIsNull() {
        feature.setTextStyle(null);

        Assertions.assertNull(feature.getStyle());
        Assertions.assertNull(feature.getTextStyle());
    }

    @Test
    void setTextStyle_createsEmptyStyle() {
        TextStyle textStyle = new TextStyle();
        feature.setTextStyle(textStyle);

        Assertions.assertNotNull(feature.getStyle());
        Assertions.assertEquals(textStyle, feature.getStyle().getTextStyle());
        Assertions.assertEquals(textStyle, feature.getTextStyle());
    }

    @Test
    void setTextStyle_updatesExistingStyle() {
        Style style = new Style();
        feature.setStyle(style);

        TextStyle textStyle = new TextStyle();
        feature.setTextStyle(textStyle);

        Assertions.assertEquals(style, feature.getStyle());
        Assertions.assertEquals(textStyle, feature.getStyle().getTextStyle());
        Assertions.assertEquals(textStyle, feature.getTextStyle());
    }

    private static class TestFeature extends Feature {
    }
}
