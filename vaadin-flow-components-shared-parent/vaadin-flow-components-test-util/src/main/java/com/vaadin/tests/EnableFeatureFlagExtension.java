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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlags;

/**
 * JUnit extension to enable a specific feature flag for the duration of a test.
 * <p>
 * Usage:
 *
 * <pre>
 * &#64;RegisterExtension
 * EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
 *         MyFeatureFlagProvider.MY_FEATURE);
 * </pre>
 */
public class EnableFeatureFlagExtension
        implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create(EnableFeatureFlagExtension.class);

    private final Feature feature;
    private FeatureFlags mockFeatureFlags;
    private MockedStatic<FeatureFlags> mockFeatureFlagsStatic;

    public EnableFeatureFlagExtension(Feature feature) {
        this.feature = feature;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        // Create the Mockito static mock once per test class and reuse it.
        // Mockito.mockStatic is expensive, and the mock state does not need to
        // be reset between tests in the same class.
        var store = context.getParent().orElse(context).getStore(NAMESPACE);
        String key = context.getRequiredTestClass().getName() + "."
                + feature.getId();
        var holder = store.getOrComputeIfAbsent(key,
                k -> createMocks(), MockHolder.class);
        mockFeatureFlags = holder.featureFlags;
        mockFeatureFlagsStatic = holder.staticMock;
        // Reset to enabled state before each test: a previous test may have
        // called disableFeature(), and that state would otherwise leak.
        enableFeature();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // Do nothing: the mock is kept alive for the whole test class.
        // JUnit 5 closes MockHolder (a CloseableResource) when the store scope
        // ends after all tests in the class have run.
    }

    public void disableFeature() {
        Mockito.when(mockFeatureFlags.isEnabled(feature)).thenReturn(false);
        Mockito.when(mockFeatureFlags.isEnabled(feature.getId()))
                .thenReturn(false);
    }

    public void enableFeature() {
        Mockito.when(mockFeatureFlags.isEnabled(feature)).thenReturn(true);
        Mockito.when(mockFeatureFlags.isEnabled(feature.getId()))
                .thenReturn(true);
    }

    private MockHolder createMocks() {
        FeatureFlags flags = Mockito.mock(FeatureFlags.class);
        Mockito.when(flags.isEnabled(feature)).thenReturn(true);
        Mockito.when(flags.isEnabled(feature.getId())).thenReturn(true);
        MockedStatic<FeatureFlags> staticMock = Mockito
                .mockStatic(FeatureFlags.class);
        staticMock.when(() -> FeatureFlags.get(Mockito.any()))
                .thenReturn(flags);
        return new MockHolder(flags, staticMock);
    }

    private record MockHolder(FeatureFlags featureFlags,
            MockedStatic<FeatureFlags> staticMock)
            implements ExtensionContext.Store.CloseableResource {
        @Override
        public void close() {
            staticMock.close();
        }
    }
}
