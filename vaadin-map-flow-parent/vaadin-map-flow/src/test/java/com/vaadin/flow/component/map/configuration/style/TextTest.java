package com.vaadin.flow.component.map.configuration.style;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TextTest {
    private Text text;

    @Before
    public void setup() {
        text = new Text();
    }

    @Test
    public void setOffsetFromPixelValues_createsOffset() {
        text.setOffset(123, 456);

        Assert.assertNotNull(text.getOffset());
        Assert.assertEquals(123, text.getOffset().getX(), 0);
        Assert.assertEquals(456, text.getOffset().getY(), 0);
    }

    @Test
    public void setFillFromColor_createsFill() {
        text.setFill("#123");

        Assert.assertNotNull(text.getFill());
        Assert.assertEquals("#123", text.getFill().getColor());
    }

    @Test
    public void setStrokeFromColorAndWidth_createsStroke() {
        text.setStroke("#123", 123);

        Assert.assertNotNull(text.getStroke());
        Assert.assertEquals("#123", text.getStroke().getColor());
        Assert.assertEquals(123, text.getStroke().getWidth(), 0);
    }

    @Test
    public void setBackgroundFillFromColor_createsFill() {
        text.setBackgroundFill("#123");

        Assert.assertNotNull(text.getBackgroundFill());
        Assert.assertEquals("#123", text.getBackgroundFill().getColor());
    }

    @Test
    public void setBackgroundStrokeFromColorAndWidth_createsStroke() {
        text.setBackgroundStroke("#123", 123);

        Assert.assertNotNull(text.getBackgroundStroke());
        Assert.assertEquals("#123", text.getBackgroundStroke().getColor());
        Assert.assertEquals(123, text.getBackgroundStroke().getWidth(), 0);
    }
}