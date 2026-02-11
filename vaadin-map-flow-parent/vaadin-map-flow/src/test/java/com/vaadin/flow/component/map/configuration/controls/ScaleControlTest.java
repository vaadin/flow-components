/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.controls;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ScaleControlTest {
    private ScaleControl scaleControl;

    @Before
    public void setup() {
        scaleControl = new ScaleControl();
    }

    @Test
    public void defaults() {
        Assert.assertEquals(64, scaleControl.getMinWidth());
        Assert.assertNull(scaleControl.getMaxWidth());
        Assert.assertEquals(ScaleControl.Unit.METRIC, scaleControl.getUnits());
        Assert.assertEquals(ScaleControl.DisplayMode.LINE,
                scaleControl.getDisplayMode());
        Assert.assertEquals(4, scaleControl.getScaleBarSteps());
        Assert.assertFalse(scaleControl.isScaleBarTextVisible());
    }

    @Test
    public void setMinWidth() {
        scaleControl.setMinWidth(100);
        Assert.assertEquals(100, scaleControl.getMinWidth());
    }

    @Test
    public void setMaxWidth() {
        scaleControl.setMaxWidth(200);
        Assert.assertEquals(Integer.valueOf(200), scaleControl.getMaxWidth());
    }

    @Test
    public void setMaxWidth_null() {
        scaleControl.setMaxWidth(200);
        scaleControl.setMaxWidth(null);
        Assert.assertNull(scaleControl.getMaxWidth());
    }

    @Test
    public void setUnits() {
        scaleControl.setUnits(ScaleControl.Unit.IMPERIAL);
        Assert.assertEquals(ScaleControl.Unit.IMPERIAL,
                scaleControl.getUnits());
    }

    @Test(expected = NullPointerException.class)
    public void setUnits_null_throws() {
        scaleControl.setUnits(null);
    }

    @Test
    public void setDisplayMode() {
        scaleControl.setDisplayMode(ScaleControl.DisplayMode.BAR);
        Assert.assertEquals(ScaleControl.DisplayMode.BAR,
                scaleControl.getDisplayMode());
    }

    @Test(expected = NullPointerException.class)
    public void setDisplayMode_null_throws() {
        scaleControl.setDisplayMode(null);
    }

    @Test
    public void setScaleBarSteps() {
        scaleControl.setScaleBarSteps(8);
        Assert.assertEquals(8, scaleControl.getScaleBarSteps());
    }

    @Test
    public void setScaleBarTextVisible() {
        scaleControl.setScaleBarTextVisible(true);
        Assert.assertTrue(scaleControl.isScaleBarTextVisible());
    }
}
