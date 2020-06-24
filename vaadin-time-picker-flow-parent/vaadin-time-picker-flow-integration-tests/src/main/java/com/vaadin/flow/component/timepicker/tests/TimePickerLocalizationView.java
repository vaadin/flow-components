/*
 * Copyright 2000-2019 Vaadin Ltd.
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

package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.timepicker.demo.TimePickerView;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Route("time-picker-localization")
public class TimePickerLocalizationView extends Div
        implements HasUrlParameter<String> {

    private final TimePicker timePicker;
    private final TimePickerView.LocalTimeTextBlock browserFormattedTime;

    public TimePickerLocalizationView() {
        NativeSelect localesSelect = new NativeSelect();
        localesSelect.setWidth("230px");
        localesSelect.setOptions(TimePicker.getSupportedAvailableLocales()
                .map(Locale::toLanguageTag).sorted()
                .collect(Collectors.toList()));
        localesSelect.setId("locale-picker");

        timePicker = new TimePicker();

        NativeSelect stepSelector = new NativeSelect();
        stepSelector.setWidth("70px");
        stepSelector.setOptions(Arrays.asList("0.001s", "0.5s", "1s", "10s",
                "1m", "15m", "30m", "1h"));
        stepSelector.setId("step-picker");
        stepSelector.setValue(timePicker.getStep().toString().replace("PT", "")
                .toLowerCase());

        browserFormattedTime = new TimePickerView.LocalTimeTextBlock();
        browserFormattedTime.setId("formatted-time");
        browserFormattedTime.setStep(timePicker.getStep());

        Div valueLabel = new Div();
        valueLabel.setId("value-label");

        localesSelect.addValueChangeListener(event -> {
            if (event.getValue() == null || event.getValue().isEmpty()) {
                return;
            }
            Locale locale = Locale.forLanguageTag(event.getValue());
            timePicker.setLocale(locale);
            browserFormattedTime.setLocale(locale);
        });

        stepSelector.addValueChangeListener(event -> {
            if (event.getValue() != null && !event.getValue().isEmpty()) {
                Duration step = Duration
                        .parse("PT" + event.getValue().toUpperCase());
                timePicker.setStep(step);
                browserFormattedTime.setStep(step);
            }
        });

        timePicker.addValueChangeListener(event -> {
            LocalTime value = event.getValue();
            browserFormattedTime.setLocalTime(value);
            valueLabel.setText(String.format("%s:%s:%s.%s", value.getHour(),
                    value.getMinute(), value.getSecond(),
                    value.get(ChronoField.MILLI_OF_SECOND)));
        });

        add(localesSelect, stepSelector, timePicker, browserFormattedTime,
                valueLabel);
        localesSelect.setValue(UI.getCurrent().getLocale().toLanguageTag());
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent,
            @OptionalParameter String initialValue) {
        if (initialValue != null) {
            // eg. fi-FI-10-30
            String[] split = initialValue.split("\\W");
            Locale locale = new Locale(split[0], split[1]);

            timePicker.setLocale(locale);
            browserFormattedTime.setLocale(locale);
            timePicker.setValue(LocalTime.of(Integer.parseInt(split[2]),
                    Integer.parseInt(split[3])));
        }
    }
}
