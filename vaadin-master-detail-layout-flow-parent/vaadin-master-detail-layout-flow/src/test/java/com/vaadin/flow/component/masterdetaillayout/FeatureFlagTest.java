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
package com.vaadin.flow.component.masterdetaillayout;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

class FeatureFlagTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            FeatureFlags.MASTER_DETAIL_LAYOUT_COMPONENT);

    @Test
    void featureEnabled_attachLayout_doesNotThrow() {
        ui.add(new MasterDetailLayout());
    }

    @Test
    void featureDisabled_attachLayout_throwsExperimentalFeatureException() {
        featureFlagExtension.disableFeature();
        Assertions.assertThrows(ExperimentalFeatureException.class,
                () -> ui.add(new MasterDetailLayout()));
    }
}
