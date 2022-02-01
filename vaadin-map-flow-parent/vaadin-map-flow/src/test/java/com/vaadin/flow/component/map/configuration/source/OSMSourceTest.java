package com.vaadin.flow.component.map.configuration.source;

import org.junit.Assert;
import org.junit.Test;

public class OSMSourceTest {
    @Test
    public void setAttributionsCollapsible_mayNotBeEnabled() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            new OSMSource.Options().setAttributionsCollapsible(true);
        });
    }
}