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
import com.vaadin.tests.MockUI;

public class SliderWarningsTest {

    private UI ui;
    private Logger mockedLogger;
    private MockedStatic<FeatureFlags> mockFeatureFlagsStatic;
    private MockedStatic<LoggerFactory> mockLoggerFactoryStatic;

    @Before
    public void setUp() {
        mockedLogger = Mockito.mock(Logger.class);
        Mockito.when(mockedLogger.isWarnEnabled()).thenReturn(true);

        mockLoggerFactoryStatic = Mockito.mockStatic(LoggerFactory.class);
        mockLoggerFactoryStatic
                .when(() -> LoggerFactory.getLogger(Slider.class))
                .thenReturn(mockedLogger);

        ui = new MockUI();

        VaadinContext mockContext = Mockito.mock(VaadinContext.class);
        FeatureFlags mockFeatureFlags = Mockito.mock(FeatureFlags.class);

        Mockito.when(ui.getSession().getService().getContext())
                .thenReturn(mockContext);
        Mockito.when(mockFeatureFlags
                .isEnabled(SliderFeatureFlagProvider.SLIDER_COMPONENT))
                .thenReturn(true);

        mockFeatureFlagsStatic = Mockito.mockStatic(FeatureFlags.class);
        mockFeatureFlagsStatic.when(() -> FeatureFlags.get(mockContext))
                .thenReturn(mockFeatureFlags);
    }

    @After
    public void tearDown() {
        mockLoggerFactoryStatic.close();
        mockFeatureFlagsStatic.close();
        UI.setCurrent(null);
    }

    @Test
    public void setMinGreaterThanMax_warnsMinGreaterThanMax() {
        Slider slider = new Slider();
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setMin(200);
        fakeClientResponse();

        Mockito.verify(mockedLogger).warn(Mockito.contains("min"),
                Mockito.eq("Slider"), Mockito.eq(200.0), Mockito.eq(100.0));
    }

    @Test
    public void setMaxLessThanMin_warnsMinGreaterThanMax() {
        Slider slider = new Slider();
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setMax(-10);
        fakeClientResponse();

        Mockito.verify(mockedLogger).warn(Mockito.contains("min"),
                Mockito.eq("Slider"), Mockito.eq(0.0), Mockito.eq(-10.0));
    }

    @Test
    public void setValueOutOfRange_warnsValueOutOfRange() {
        Slider slider = new Slider(0, 100);
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setValue(150.0);
        fakeClientResponse();

        Mockito.verify(mockedLogger).warn(
                Mockito.contains("out of [min, max] range"),
                Mockito.eq("Slider"), Mockito.eq(150.0), Mockito.eq(0.0),
                Mockito.eq(100.0));
    }

    @Test
    public void setValueNotAlignedWithStep_warnsValueNotAligned() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10);
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setValue(15.0);
        fakeClientResponse();

        Mockito.verify(mockedLogger).warn(
                Mockito.contains("not aligned with step"),
                Mockito.eq("Slider"), Mockito.eq(15.0), Mockito.eq(0.0),
                Mockito.eq(100.0), Mockito.eq(10.0));
    }

    @Test
    public void setConsistentProperties_noWarnings() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10);
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setValue(50.0);
        fakeClientResponse();

        Mockito.verify(mockedLogger, Mockito.never()).warn(Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void setMultipleProperties_onlyOneCheckPerResponseCycle() {
        Slider slider = new Slider(0, 100);
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setMin(10);
        slider.setMax(50);
        slider.setStep(5);
        slider.setValue(25.0);
        fakeClientResponse();

        Mockito.verify(mockedLogger, Mockito.never()).warn(Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void setValueFromClient_noWarnings() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10);
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        // Simulate client-side property change
        slider.getElement().setProperty("value", 15.0);
        fakeClientResponse();

        Mockito.verify(mockedLogger, Mockito.never()).warn(Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    private void fakeClientResponse() {
        ui.getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree()
                .collectChanges(ignore -> {
                });
    }
}
