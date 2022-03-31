package com.vaadin.flow.component.map.configuration.source;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class VectorSourceTest {

    @Test
    public void initializeWithOptions() {
        VectorSource.Options options = createOptions();
        VectorSource source = new VectorSource(options);

        Assert.assertEquals("testProjection", source.getProjection());
        Assert.assertEquals("testAttributions",
                source.getAttributions().get(0));
        Assert.assertFalse(source.isAttributionsCollapsible());
    }

    private VectorSource.Options createOptions() {
        VectorSource.Options options = new VectorSource.Options();
        options.setProjection("testProjection");
        options.setAttributions(List.of("testAttributions"));
        options.setAttributionsCollapsible(false);

        return options;
    }
}