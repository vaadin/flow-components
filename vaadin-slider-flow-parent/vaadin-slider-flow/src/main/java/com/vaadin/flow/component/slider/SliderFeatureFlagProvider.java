/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
