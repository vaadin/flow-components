package com.vaadin.flow.component.map.configuration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExtentTest {

    private Extent extent;

    @Before
    public void setup() {
        extent = new Extent(10, 10, 100, 200);
    }

    @Test
    public void getWidth_shouldReturnCorrectValue() {
        Assert.assertEquals(90, extent.getWidth(), 0.0001);
    }

    @Test
    public void getHeight_shouldReturnCorrectValue() {
        Assert.assertEquals(190, extent.getHeight(), 0.0001);
    }
}
