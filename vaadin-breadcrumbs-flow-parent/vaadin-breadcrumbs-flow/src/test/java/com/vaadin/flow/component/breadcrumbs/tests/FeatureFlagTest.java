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
package com.vaadin.flow.component.breadcrumbs.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.breadcrumbs.Breadcrumbs;
import com.vaadin.flow.component.breadcrumbs.BreadcrumbsFeatureFlagProvider;
import com.vaadin.flow.component.breadcrumbs.ExperimentalFeatureException;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

class FeatureFlagTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            BreadcrumbsFeatureFlagProvider.BREADCRUMBS_COMPONENT);

    @Test
    void featureEnabled_attach_doesNotThrow() {
        var breadcrumbs = new Breadcrumbs();
        Assertions.assertDoesNotThrow(() -> ui.add(breadcrumbs));
    }

    @Test
    void featureDisabled_attach_throws() {
        featureFlagExtension.disableFeature();

        var breadcrumbs = new Breadcrumbs();
        Assertions.assertThrows(ExperimentalFeatureException.class,
                () -> ui.add(breadcrumbs));
    }
}
