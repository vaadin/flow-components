package com.vaadin.flow.component.map.configuration.source;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class XYZSourceTest {

    @Test
    public void initializeWithOptions() {
        XYZSource.Options options = createOptions();
        XYZSource source = new XYZSource(options);

        Assert.assertEquals("https://example.com", source.getUrl());
        Assert.assertEquals("testCrossOrigin", source.getCrossOrigin());
        Assert.assertEquals("testProjection", source.getProjection());
        Assert.assertEquals("testAttributions",
                source.getAttributions().get(0));
        Assert.assertFalse(source.isAttributionsCollapsible());
        Assert.assertTrue(source.isOpaque());
    }

    private XYZSource.Options createOptions() {
        XYZSource.Options options = new XYZSource.Options();
        options.setUrl("https://example.com");
        options.setCrossOrigin("testCrossOrigin");
        options.setProjection("testProjection");
        options.setAttributions(List.of("testAttributions"));
        options.setAttributionsCollapsible(false);
        options.setOpaque(true);

        return options;
    }
}