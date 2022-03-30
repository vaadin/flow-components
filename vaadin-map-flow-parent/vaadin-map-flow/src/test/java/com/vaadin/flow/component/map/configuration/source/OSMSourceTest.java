package com.vaadin.flow.component.map.configuration.source;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class OSMSourceTest {

    @Test
    public void initializeWithOptions() {
        OSMSource.Options options = createOptions();
        OSMSource source = new OSMSource(options);

        Assert.assertEquals("https://example.com", source.getUrl());
        Assert.assertEquals("testCrossOrigin", source.getCrossOrigin());
        Assert.assertEquals("testProjection", source.getProjection());
        Assert.assertEquals("testAttributions",
                source.getAttributions().get(0));
        Assert.assertFalse(source.isAttributionsCollapsible());
        Assert.assertTrue(source.isOpaque());
    }

    @Test
    public void setAttributionsCollapsible_disabledByDefault() {
        OSMSource source = new OSMSource();

        Assert.assertFalse(source.isAttributionsCollapsible());
    }

    @Test
    public void setAttributionsCollapsible_mayNotBeEnabled() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            OSMSource.Options options = new OSMSource.Options();
            options.setAttributionsCollapsible(true);
        });
    }

    private OSMSource.Options createOptions() {
        OSMSource.Options options = new OSMSource.Options();
        options.setUrl("https://example.com");
        options.setCrossOrigin("testCrossOrigin");
        options.setProjection("testProjection");
        options.setAttributions(List.of("testAttributions"));
        options.setAttributionsCollapsible(false);
        options.setOpaque(true);

        return options;
    }
}