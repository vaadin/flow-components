/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

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
