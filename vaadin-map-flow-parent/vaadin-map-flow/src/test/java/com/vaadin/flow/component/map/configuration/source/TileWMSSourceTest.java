/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.map.configuration.source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class TileWMSSourceTest {
    @Test
    public void initialize_validOptions() {
        TileWMSSource.Options options = createValidOptions();

        TileWMSSource source = new TileWMSSource(options);
        Assert.assertEquals("https://example.com", source.getUrl());
        Assert.assertEquals("layer1", source.getParams().get("LAYERS"));
        Assert.assertEquals("testServerType", source.getServerType());
        Assert.assertEquals("testCrossOrigin", source.getCrossOrigin());
        Assert.assertEquals("testProjection", source.getProjection());
        Assert.assertEquals("testAttributions",
                source.getAttributions().get(0));
        Assert.assertFalse(source.isAttributionsCollapsible());
        Assert.assertTrue(source.isOpaque());
    }

    @Test
    public void initialize_params_mustNotBeNull() {
        TileWMSSource.Options options = createValidOptions();
        options.setParams(null);

        Assert.assertThrows(NullPointerException.class,
                () -> new TileWMSSource(options));
    }

    @Test
    public void initialize_params_layer_mustNotBeNull() {
        TileWMSSource.Options options = createValidOptions();
        Map<String, Object> params = createValidParams();
        params.remove("LAYERS");
        options.setParams(params);

        Assert.assertThrows(NullPointerException.class,
                () -> new TileWMSSource(options));
    }

    private Map<String, Object> createValidParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("LAYERS", "layer1");

        return params;
    }

    private TileWMSSource.Options createValidOptions() {
        Map<String, Object> params = createValidParams();

        TileWMSSource.Options options = new TileWMSSource.Options();
        options.setUrl("https://example.com");
        options.setParams(params);
        options.setServerType("testServerType");
        options.setCrossOrigin("testCrossOrigin");
        options.setProjection("testProjection");
        options.setAttributions(List.of("testAttributions"));
        options.setAttributionsCollapsible(false);
        options.setOpaque(true);

        return options;
    }
}
