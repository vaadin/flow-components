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

import org.junit.Rule;
import org.junit.Test;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.tests.EnableFeatureFlagRule;
import com.vaadin.tests.MockUIRule;

public class FeatureFlagTest {
    @Rule
    public MockUIRule ui = new MockUIRule();
    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            FeatureFlags.MASTER_DETAIL_LAYOUT_COMPONENT);

    @Test
    public void featureEnabled_attachLayout_doesNotThrow() {
        ui.add(new MasterDetailLayout());
    }

    @Test(expected = ExperimentalFeatureException.class)
    public void featureDisabled_attachLayout_throwsExperimentalFeatureException() {
        featureFlagRule.disableFeature();
        ui.add(new MasterDetailLayout());
    }
}
