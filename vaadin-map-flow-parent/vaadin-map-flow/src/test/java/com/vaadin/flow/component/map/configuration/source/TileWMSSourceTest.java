package com.vaadin.flow.component.map.configuration.source;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TileWMSSourceTest {
    @Test
    public void initialize_validOptions() {
        TileWMSSource.Options options = createValidOptions();

        new TileWMSSource(options);
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
        return options;
    }
}