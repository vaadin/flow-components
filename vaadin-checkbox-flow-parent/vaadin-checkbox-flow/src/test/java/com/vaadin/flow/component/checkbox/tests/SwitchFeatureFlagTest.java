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
package com.vaadin.flow.component.checkbox.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.checkbox.ExperimentalFeatureException;
import com.vaadin.flow.component.checkbox.Switch;
import com.vaadin.flow.component.checkbox.SwitchFeatureFlagProvider;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

class SwitchFeatureFlagTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            SwitchFeatureFlagProvider.SWITCH_COMPONENT);

    @Test
    void featureEnabled_attach_doesNotThrow() {
        var field = new Switch();
        Assertions.assertDoesNotThrow(() -> ui.add(field));
    }

    @Test
    void featureDisabled_attach_throws() {
        featureFlagExtension.disableFeature();

        var field = new Switch();
        Assertions.assertThrows(ExperimentalFeatureException.class,
                () -> ui.add(field));
    }
}
