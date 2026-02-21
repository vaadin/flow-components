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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.tests.EnableFeatureFlagRule;

public class FeatureFlagTest {
    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            FeatureFlags.MASTER_DETAIL_LAYOUT_COMPONENT);

    private final UI ui = new UI();

    @Before
    public void setup() {
        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        VaadinService mockService = Mockito.mock(VaadinService.class);

        Mockito.when(mockSession.getService()).thenReturn(mockService);

        ui.getInternals().setSession(mockSession);
    }

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
