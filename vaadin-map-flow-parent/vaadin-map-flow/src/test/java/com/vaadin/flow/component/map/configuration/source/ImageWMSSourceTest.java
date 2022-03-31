package com.vaadin.flow.component.map.configuration.source;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageWMSSourceTest {
    @Test
    public void initialize_validOptions() {
        ImageWMSSource.Options options = createValidOptions();

        ImageWMSSource source = new ImageWMSSource(options);
        Assert.assertEquals("https://example.com", source.getUrl());
        Assert.assertEquals("layer1", source.getParams().get("LAYERS"));
        Assert.assertEquals(5f, source.getRatio(), 0.1);
        Assert.assertEquals("testServerType", source.getServerType());
        Assert.assertEquals("testCrossOrigin", source.getCrossOrigin());
        Assert.assertEquals("testProjection", source.getProjection());
        Assert.assertEquals("testAttributions",
                source.getAttributions().get(0));
        Assert.assertFalse(source.isAttributionsCollapsible());
    }

    @Test
    public void initialize_params_mustNotBeNull() {
        ImageWMSSource.Options options = createValidOptions();
        options.setParams(null);

        Assert.assertThrows(NullPointerException.class,
                () -> new ImageWMSSource(options));
    }

    @Test
    public void initialize_params_layer_mustNotBeNull() {
        ImageWMSSource.Options options = createValidOptions();
        Map<String, Object> params = createValidParams();
        params.remove("LAYERS");
        options.setParams(params);

        Assert.assertThrows(NullPointerException.class,
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