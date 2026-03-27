/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OSMSourceTest {

    @Test
    void initializeWithOptions() {
        OSMSource.Options options = createOptions();
        OSMSource source = new OSMSource(options);

        Assertions.assertEquals("https://example.com", source.getUrl());
        Assertions.assertEquals("testCrossOrigin", source.getCrossOrigin());
        Assertions.assertEquals("testProjection", source.getProjection());
        Assertions.assertEquals("testAttributions",
                source.getAttributions().get(0));
        Assertions.assertFalse(source.isAttributionsCollapsible());
        Assertions.assertTrue(source.isOpaque());
    }

    @Test
    void setAttributionsCollapsible_disabledByDefault() {
        OSMSource source = new OSMSource();

        Assertions.assertFalse(source.isAttributionsCollapsible());
    }

    @Test
    void setAttributionsCollapsible_mayNotBeEnabled() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
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
