package com.vaadin.flow.components.map;

import com.vaadin.flow.component.map.testbench.MapElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/tile-grid")

public class TileGridIT extends AbstractComponentIT {

    private MapElement map;

    @Before
    public void init() {
        open();

    }

    @Test
    public void tileGridSet() {
        map = $(MapElement.class).waitForFirst();

        long width = (long) map.evaluateOLExpression(
                "map.getLayers().item(2).getSource().getTileGrid().getTileSize()[0]");
        long height = (long) map.evaluateOLExpression(
                "map.getLayers().item(2).getSource().getTileGrid().getTileSize()[1]");
        long resolutionsLength = (long) map.evaluateOLExpression(
                "map.getLayers().item(2).getSource().getTileGrid().getResolutions().length");

        Assert.assertEquals(512, width);
        Assert.assertEquals(256, height);
        Assert.assertEquals(22, resolutionsLength);
    }
}
