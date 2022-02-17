package com.vaadin.flow.component.map.configuration.source;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ImageWMSSourceTest {
    @Test
    public void initialize_validOptions() {
        ImageWMSSource.Options options = createValidOptions();

        new ImageWMSSource(options);
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

        return new ImageWMSSource.Options().setUrl("https://example.com")
                .setParams(params);
    }
}