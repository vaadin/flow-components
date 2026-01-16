package com.vaadin.flow.component.slider;

import java.util.List;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlagProvider;

public class SliderFeatureFlagProvider implements FeatureFlagProvider {

    public static final Feature SLIDER_COMPONENT = new Feature(
            "Slider component", "sliderComponent",
            "https://github.com/vaadin/platform/issues/8397", true,
            "com.vaadin.flow.component.slider.Slider");

    @Override
    public List<Feature> getFeatures() {
        return List.of(SLIDER_COMPONENT);
    }
}
