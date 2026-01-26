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
package com.vaadin.flow.component.slider.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.component.slider.SliderFeatureFlagProvider;
import com.vaadin.tests.MockUI;

public class SliderWarningsTest {

    private Slider slider;

    private MockUI ui = new MockUI();
    private Logger logger = Mockito.mock(Logger.class);
    private FeatureFlags featureFlags = Mockito.mock(FeatureFlags.class);
    private MockedStatic<FeatureFlags> featureFlagsStatic = Mockito
            .mockStatic(FeatureFlags.class);
    private MockedStatic<LoggerFactory> loggerFactoryStatic = Mockito
            .mockStatic(LoggerFactory.class);

    @Before
    public void setup() {
        featureFlagsStatic
                .when(() -> FeatureFlags
                        .get(ui.getSession().getService().getContext()))
                .thenReturn(featureFlags);
        Mockito.when(featureFlags
                .isEnabled(SliderFeatureFlagProvider.SLIDER_COMPONENT))
                .thenReturn(true);
        loggerFactoryStatic.when(() -> LoggerFactory.getLogger(Slider.class))
                .thenReturn(logger);

        slider = new Slider(0, 100, 50);
        ui.add(slider);
        fakeClientCommunication();
    }

    @After
    public void tearDown() {
        featureFlagsStatic.close();
        loggerFactoryStatic.close();
    }

    @Test
    public void setMin_valueBelowMin_warningIsShown() {
        slider.setMin(60);
        fakeClientCommunication();

        Mockito.verify(logger).warn(Mockito.contains("below the minimum"),
                Mockito.any(), Mockito.any());
    }

    @Test
    public void setMin_valueAboveMin_noWarning() {
        slider.setMin(40);
        fakeClientCommunication();

        Mockito.verify(logger, Mockito.never()).warn(Mockito.anyString(),
                Mockito.any(), Mockito.any());
    }

    @Test
    public void setMax_valueAboveMax_warningIsShown() {
        slider.setMax(40);
        fakeClientCommunication();

        Mockito.verify(logger).warn(Mockito.contains("exceeds the maximum"),
                Mockito.any(), Mockito.any());
    }

    @Test
    public void setMax_valueBelowMax_noWarning() {
        slider.setMax(60);
        fakeClientCommunication();

        Mockito.verify(logger, Mockito.never()).warn(Mockito.anyString(),
                Mockito.any(), Mockito.any());
    }

    @Test
    public void setStep_valueNotAligned_warningIsShown() {
        slider.setStep(7);
        fakeClientCommunication();

        Mockito.verify(logger).warn(
                Mockito.contains("not aligned with the step"), Mockito.any(),
                Mockito.any());
    }

    @Test
    public void setStep_valueAligned_noWarning() {
        slider.setStep(10);
        fakeClientCommunication();

        Mockito.verify(logger, Mockito.never()).warn(Mockito.anyString(),
                Mockito.any(), Mockito.any());
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
