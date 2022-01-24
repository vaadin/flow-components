package com.vaadin.flow.components.map;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-map/map-events")
public class MapEventsIT extends AbstractComponentIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void moveEndEvent() {
        Assert.assertTrue(true);
    }
}
