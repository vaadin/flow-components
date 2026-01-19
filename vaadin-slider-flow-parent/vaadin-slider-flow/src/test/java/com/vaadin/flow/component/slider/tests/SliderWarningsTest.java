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
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.component.slider.SliderFeatureFlagProvider;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class SliderWarningsTest {

    private UI ui;
    private Slider slider;
    private Logger logger;
    private FeatureFlags featureFlags = Mockito.mock(FeatureFlags.class);
    private MockedStatic<FeatureFlags> featureFlagsStatic = Mockito
            .mockStatic(FeatureFlags.class);
    private MockedStatic<LoggerFactory> loggerFactoryStatic = Mockito
            .mockStatic(LoggerFactory.class);

    @Before
    public void setup() {
        ui = new UI();
        UI.setCurrent(ui);

        VaadinSession session = Mockito.mock(VaadinSession.class);
        VaadinService service = Mockito.mock(VaadinService.class);
        VaadinContext context = Mockito.mock(VaadinContext.class);

        Mockito.when(session.hasLock()).thenReturn(true);
        Mockito.when(session.getService()).thenReturn(service);
        Mockito.when(service.getContext()).thenReturn(context);

        featureFlagsStatic.when(() -> FeatureFlags.get(context))
                .thenReturn(featureFlags);
        Mockito.when(featureFlags
                .isEnabled(SliderFeatureFlagProvider.SLIDER_COMPONENT))
                .thenReturn(true);

        logger = Mockito.mock(Logger.class);
        loggerFactoryStatic.when(() -> LoggerFactory.getLogger(Slider.class))
                .thenReturn(logger);

        ui.getInternals().setSession(session);

        slider = new Slider(0, 100, 50);
        ui.add(slider);
        fakeClientCommunication();
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
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
