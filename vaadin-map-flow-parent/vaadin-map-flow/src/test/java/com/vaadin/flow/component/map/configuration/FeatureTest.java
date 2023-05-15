package com.vaadin.flow.component.map.configuration;

import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.component.map.configuration.style.TextStyle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FeatureTest {

    private Feature feature;

    @Before
    public void setup() {
        feature = new TestFeature();
    }

    @Test
    public void getTextStyle_nullByDefault() {
        Assert.assertNull(feature.getTextStyle());
    }

    @Test
    public void setTextStyle_doesNotCreateStyleIfTextStyleIsNull() {
        feature.setTextStyle(null);

        Assert.assertNull(feature.getStyle());
        Assert.assertNull(feature.getTextStyle());
    }

    @Test
    public void setTextStyle_createsEmptyStyle() {
        TextStyle textStyle = new TextStyle();
        feature.setTextStyle(textStyle);

        Assert.assertNotNull(feature.getStyle());
        Assert.assertEquals(textStyle, feature.getStyle().getTextStyle());
        Assert.assertEquals(textStyle, feature.getTextStyle());
    }

    @Test
    public void setTextStyle_updatesExistingStyle() {
        Style style = new Style();
        feature.setStyle(style);

        TextStyle textStyle = new TextStyle();
        feature.setTextStyle(textStyle);

        Assert.assertEquals(style, feature.getStyle());
        Assert.assertEquals(textStyle, feature.getStyle().getTextStyle());
        Assert.assertEquals(textStyle, feature.getTextStyle());
    }

    private static class TestFeature extends Feature {
    }
}