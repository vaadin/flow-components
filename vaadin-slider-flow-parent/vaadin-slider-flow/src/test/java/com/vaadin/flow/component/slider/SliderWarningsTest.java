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
package com.vaadin.flow.component.slider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

class SliderWarningsTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            SliderFeatureFlagProvider.SLIDER_COMPONENT);

    private Logger mockedLogger;
    private MockedStatic<LoggerFactory> mockLoggerFactoryStatic;

    @BeforeEach
    void setUp() {
        mockedLogger = Mockito.mock(Logger.class);
        Mockito.when(mockedLogger.isWarnEnabled()).thenReturn(true);

        mockLoggerFactoryStatic = Mockito.mockStatic(LoggerFactory.class);
        mockLoggerFactoryStatic
                .when(() -> LoggerFactory.getLogger(Slider.class))
                .thenReturn(mockedLogger);
    }

    @AfterEach
    void tearDown() {
        mockLoggerFactoryStatic.close();
    }

    @Test
    void setMinGreaterThanMax_warnsMinGreaterThanMax() {
        Slider slider = new Slider();
        ui.add(slider);
        ui.fakeClientCommunication();
        Mockito.clearInvocations(mockedLogger);

        slider.setMin(200.0);
        ui.fakeClientCommunication();

        Mockito.verify(mockedLogger).warn(Mockito.contains("min"),
                Mockito.eq(200.0), Mockito.eq(100.0));
    }

    @Test
    void setMaxLessThanMin_warnsMinGreaterThanMax() {
        Slider slider = new Slider();
        ui.add(slider);
        ui.fakeClientCommunication();
        Mockito.clearInvocations(mockedLogger);

        slider.setMax(-10.0);
        ui.fakeClientCommunication();

        Mockito.verify(mockedLogger).warn(Mockito.contains("min"),
                Mockito.eq(0.0), Mockito.eq(-10.0));
    }

    @Test
    void setValueOutOfRange_warnsValueOutOfRange() {
        Slider slider = new Slider(0, 100);
        ui.add(slider);
        ui.fakeClientCommunication();
        Mockito.clearInvocations(mockedLogger);

        slider.setValue(150.0);
        ui.fakeClientCommunication();

        Mockito.verify(mockedLogger).warn(
                Mockito.contains("outside the configured range"),
                Mockito.eq(150.0), Mockito.eq(0.0), Mockito.eq(100.0));
    }

    @Test
    void setValueNotAlignedWithStep_warnsValueNotAligned() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10.0);
        ui.add(slider);
        ui.fakeClientCommunication();
        Mockito.clearInvocations(mockedLogger);

        slider.setValue(15.0);
        ui.fakeClientCommunication();

        Mockito.verify(mockedLogger).warn(
                Mockito.contains("not aligned with step"), Mockito.eq(15.0),
                Mockito.eq(0.0), Mockito.eq(100.0), Mockito.eq(10.0));
    }

    @Test
    void setConsistentProperties_noWarnings() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10.0);
        ui.add(slider);
        ui.fakeClientCommunication();
        Mockito.clearInvocations(mockedLogger);

        slider.setValue(50.0);
        ui.fakeClientCommunication();

        Mockito.verify(mockedLogger, Mockito.never()).warn(Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void setMultipleProperties_onlyOneCheckPerResponseCycle() {
        Slider slider = new Slider(0, 100);
        ui.add(slider);
        ui.fakeClientCommunication();
        Mockito.clearInvocations(mockedLogger);

        slider.setMin(10.0);
        slider.setMax(50.0);
        slider.setStep(5.0);
        slider.setValue(25.0);
        ui.fakeClientCommunication();

        Mockito.verify(mockedLogger, Mockito.never()).warn(Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }
}
