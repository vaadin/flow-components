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
import com.vaadin.flow.component.slider.RangeSlider;
import com.vaadin.flow.component.slider.RangeSliderValue;
import com.vaadin.flow.component.slider.SliderFeatureFlagProvider;
import com.vaadin.tests.MockUI;

public class RangeSliderWarningsTest {

    private UI ui = new MockUI();
    private Logger mockedLogger;
    private MockedStatic<FeatureFlags> mockFeatureFlagsStatic;
    private MockedStatic<LoggerFactory> mockLoggerFactoryStatic;

    @Before
    public void setUp() {
        mockedLogger = Mockito.mock(Logger.class);
        Mockito.when(mockedLogger.isWarnEnabled()).thenReturn(true);

        mockLoggerFactoryStatic = Mockito.mockStatic(LoggerFactory.class);
        mockLoggerFactoryStatic
                .when(() -> LoggerFactory.getLogger(RangeSlider.class))
                .thenReturn(mockedLogger);

        FeatureFlags mockFeatureFlags = Mockito.mock(FeatureFlags.class);
        Mockito.when(mockFeatureFlags
                .isEnabled(SliderFeatureFlagProvider.SLIDER_COMPONENT))
                .thenReturn(true);

        mockFeatureFlagsStatic = Mockito.mockStatic(FeatureFlags.class);
        mockFeatureFlagsStatic.when(() -> FeatureFlags.get(Mockito.any()))
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
        RangeSlider slider = new RangeSlider();
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setMin(200);
        fakeClientResponse();

        Mockito.verify(mockedLogger).warn(
                Mockito.contains("min"), Mockito.eq(200.0), Mockito.eq(100.0));
    }

    @Test
    public void setMaxLessThanMin_warnsMinGreaterThanMax() {
        RangeSlider slider = new RangeSlider();
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setMax(-10);
        fakeClientResponse();

        Mockito.verify(mockedLogger).warn(
                Mockito.contains("min"), Mockito.eq(0.0), Mockito.eq(-10.0));
    }

    @Test
    public void setValueOutOfRange_warnsValueOutOfRange() {
        RangeSlider slider = new RangeSlider(0, 100);
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setValue(new RangeSliderValue(0, 150));
        fakeClientResponse();

        Mockito.verify(mockedLogger).warn(
                Mockito.contains("outside the configured range"),
                Mockito.eq(new RangeSliderValue(0, 150)), Mockito.eq(0.0),
                Mockito.eq(100.0));
    }

    @Test
    public void setValueNotAlignedWithStep_warnsValueNotAligned() {
        RangeSlider slider = new RangeSlider(0, 100);
        slider.setStep(10);
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setValue(new RangeSliderValue(0, 15));
        fakeClientResponse();

        Mockito.verify(mockedLogger).warn(
                Mockito.contains("not aligned with step"),
                Mockito.eq(new RangeSliderValue(0, 15)), Mockito.eq(0.0),
                Mockito.eq(100.0), Mockito.eq(10.0));
    }

    @Test
    public void setConsistentProperties_noWarnings() {
        RangeSlider slider = new RangeSlider(0, 100);
        slider.setStep(10);
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setValue(new RangeSliderValue(20, 80));
        fakeClientResponse();

        Mockito.verify(mockedLogger, Mockito.never()).warn(Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void setMultipleProperties_onlyOneCheckPerResponseCycle() {
        RangeSlider slider = new RangeSlider(0, 100);
        ui.add(slider);
        fakeClientResponse();
        Mockito.clearInvocations(mockedLogger);

        slider.setMin(10);
        slider.setMax(50);
        slider.setStep(5);
        slider.setValue(new RangeSliderValue(15, 45));
        fakeClientResponse();

        Mockito.verify(mockedLogger, Mockito.never()).warn(Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    private void fakeClientResponse() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
