/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ImageWMSSourceTest {
    @Test
    void initialize_validOptions() {
        ImageWMSSource.Options options = createValidOptions();

        ImageWMSSource source = new ImageWMSSource(options);
        Assertions.assertEquals("https://example.com", source.getUrl());
        Assertions.assertEquals("layer1", source.getParams().get("LAYERS"));
        Assertions.assertEquals(5f, source.getRatio(), 0.1);
        Assertions.assertEquals("testServerType", source.getServerType());
        Assertions.assertEquals("testCrossOrigin", source.getCrossOrigin());
        Assertions.assertEquals("testProjection", source.getProjection());
        Assertions.assertEquals("testAttributions",
                source.getAttributions().get(0));
        Assertions.assertFalse(source.isAttributionsCollapsible());
    }

    @Test
    void initialize_params_mustNotBeNull() {
        ImageWMSSource.Options options = createValidOptions();
        options.setParams(null);

        Assertions.assertThrows(NullPointerException.class,
                () -> new ImageWMSSource(options));
    }

    @Test
    void initialize_params_layer_mustNotBeNull() {
        ImageWMSSource.Options options = createValidOptions();
        Map<String, Object> params = createValidParams();
        params.remove("LAYERS");
        options.setParams(params);

        Assertions.assertThrows(NullPointerException.class,
                () -> new ImageWMSSource(options));
    }

    private Map<String, Object> createValidParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("LAYERS", "layer1");

        return params;
    }

    private ImageWMSSource.Options createValidOptions() {
        Map<String, Object> params = createValidParams();

        ImageWMSSource.Options options = new ImageWMSSource.Options();
        options.setUrl("https://example.com");
        options.setParams(params);
        options.setRatio(5f);
        options.setServerType("testServerType");
        options.setCrossOrigin("testCrossOrigin");
        options.setProjection("testProjection");
        options.setAttributions(List.of("testAttributions"));
        options.setAttributionsCollapsible(false);

        return options;
    }
}
