package com.vaadin.flow.component.map.configuration.source;

import org.junit.Assert;
import org.junit.Test;

public class OSMSourceTest {
    @Test
    public void setAttributionsCollapsible_mayNotBeEnabled() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            OSMSource.Options options = new OSMSource.Options();
            options.setAttributionsCollapsible(true);
        });
    }
}