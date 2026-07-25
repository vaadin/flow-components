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
package com.vaadin.flow.component.checkbox;

import java.util.List;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlagProvider;

/**
 * Provides the Switch component feature flag, gating the experimental
 * {@link Switch} component.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public class SwitchFeatureFlagProvider implements FeatureFlagProvider {

    /**
     * The Switch component feature flag. When enabled, allows use of the
     * experimental {@link Switch} component.
     */
    public static final Feature SWITCH_COMPONENT = new Feature(
            "Switch component", // title
            "switchComponent", // id
            "https://vaadin.com/docs/latest/components/switch", // moreInfoLink
            true, // requiresServerRestart
            "com.vaadin.flow.component.checkbox.Switch"); // componentClassName

    @Override
    public List<Feature> getFeatures() {
        return List.of(SWITCH_COMPONENT);
    }
}
