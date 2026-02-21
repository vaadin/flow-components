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
package com.vaadin.tests;

import org.junit.rules.ExternalResource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlags;

/**
 * JUnit rule to enable a specific feature flag for the duration of a test.
 */
public class EnableFeatureFlagRule extends ExternalResource {
    private final Feature feature;
    private FeatureFlags mockFeatureFlags;
    private MockedStatic<FeatureFlags> mockFeatureFlagsStatic;

    public EnableFeatureFlagRule(Feature feature) {
        this.feature = feature;
    }

    @Override
    public void before() {
        mockFeatureFlags = Mockito.mock(FeatureFlags.class);
        Mockito.when(mockFeatureFlags.isEnabled(feature)).thenReturn(true);
        Mockito.when(mockFeatureFlags.isEnabled(feature.getId()))
                .thenReturn(true);

        mockFeatureFlagsStatic = Mockito.mockStatic(FeatureFlags.class);
        mockFeatureFlagsStatic.when(() -> FeatureFlags.get(Mockito.any()))
                .thenReturn(mockFeatureFlags);
    }

    @Override
    public void after() {
        mockFeatureFlagsStatic.close();
    }

    public void disableFeature() {
        Mockito.when(mockFeatureFlags.isEnabled(feature)).thenReturn(false);
        Mockito.when(mockFeatureFlags.isEnabled(feature.getId()))
                .thenReturn(false);
    }
}
