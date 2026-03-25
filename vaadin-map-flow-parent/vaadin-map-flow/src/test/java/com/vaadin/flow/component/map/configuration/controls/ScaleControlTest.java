/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.controls;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScaleControlTest {
    private ScaleControl scaleControl;

    @BeforeEach
    void setup() {
        scaleControl = new ScaleControl();
    }

    @Test
    void defaults() {
        Assertions.assertEquals(64, scaleControl.getMinWidth());
        Assertions.assertNull(scaleControl.getMaxWidth());
        Assertions.assertEquals(ScaleControl.Unit.METRIC,
                scaleControl.getUnits());
        Assertions.assertEquals(ScaleControl.DisplayMode.LINE,
                scaleControl.getDisplayMode());
        Assertions.assertEquals(4, scaleControl.getScaleBarSteps());
        Assertions.assertFalse(scaleControl.isScaleBarRatioVisible());
    }

    @Test
    void setMinWidth() {
        scaleControl.setMinWidth(100);
        Assertions.assertEquals(100, scaleControl.getMinWidth());
    }

    @Test
    void setMaxWidth() {
        scaleControl.setMaxWidth(200);
        Assertions.assertEquals(Integer.valueOf(200),
                scaleControl.getMaxWidth());
    }

    @Test
    void setMaxWidth_null() {
        scaleControl.setMaxWidth(200);
        scaleControl.setMaxWidth(null);
        Assertions.assertNull(scaleControl.getMaxWidth());
    }

    @Test
    void setUnits() {
        scaleControl.setUnits(ScaleControl.Unit.IMPERIAL);
        Assertions.assertEquals(ScaleControl.Unit.IMPERIAL,
                scaleControl.getUnits());
    }

    @Test
    void setUnits_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> scaleControl.setUnits(null));
    }

    @Test
    void setDisplayMode() {
        scaleControl.setDisplayMode(ScaleControl.DisplayMode.BAR);
        Assertions.assertEquals(ScaleControl.DisplayMode.BAR,
                scaleControl.getDisplayMode());
    }

    @Test
    void setDisplayMode_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> scaleControl.setDisplayMode(null));
    }

    @Test
    void setScaleBarSteps() {
        scaleControl.setScaleBarSteps(8);
        Assertions.assertEquals(8, scaleControl.getScaleBarSteps());
    }

    @Test
    void setScaleBarTextVisible() {
        scaleControl.setScaleBarRatioVisible(true);
        Assertions.assertTrue(scaleControl.isScaleBarRatioVisible());
    }
}
