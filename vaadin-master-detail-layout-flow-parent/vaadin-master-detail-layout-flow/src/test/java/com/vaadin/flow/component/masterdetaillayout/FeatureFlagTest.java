/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class FeatureFlagTest {
    private final UI ui = new UI();
    private final FeatureFlags mockFeatureFlags = Mockito
            .mock(FeatureFlags.class);
    private final MockedStatic<FeatureFlags> mockFeatureFlagsStatic = Mockito
            .mockStatic(FeatureFlags.class);

    @Before
    public void setup() {
        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        VaadinService mockService = Mockito.mock(VaadinService.class);
        VaadinContext mockContext = Mockito.mock(VaadinContext.class);

        Mockito.when(mockSession.getService()).thenReturn(mockService);
        Mockito.when(mockService.getContext()).thenReturn(mockContext);
        mockFeatureFlagsStatic.when(() -> FeatureFlags.get(mockContext))
                .thenReturn(mockFeatureFlags);

        ui.getInternals().setSession(mockSession);
    }

    @After
    public void tearDown() {
        mockFeatureFlagsStatic.close();
    }

    @Test
    public void featureEnabled_attachLayout_doesNotThrow() {
        Mockito.when(mockFeatureFlags
                .isEnabled(FeatureFlags.MASTER_DETAIL_LAYOUT_COMPONENT))
                .thenReturn(true);
        ui.add(new MasterDetailLayout());
    }

    @Test(expected = ExperimentalFeatureException.class)
    public void featureDisabled_attachLayout_throwsExperimentalFeatureException() {
        Mockito.when(mockFeatureFlags
                .isEnabled(FeatureFlags.MASTER_DETAIL_LAYOUT_COMPONENT))
                .thenReturn(false);
        ui.add(new MasterDetailLayout());
    }
}
