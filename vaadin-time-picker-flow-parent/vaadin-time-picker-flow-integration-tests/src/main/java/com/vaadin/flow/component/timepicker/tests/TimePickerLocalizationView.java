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
import com.vaadin.flow.component.combobox.ComboBox;
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
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Route("time-picker-localization")
public class TimePickerLocalizationView extends Div
        implements HasUrlParameter<String> {

    private final TimePicker timePicker;
    private final TimePickerView.LocalTimeTextBlock browserFormattedTime;

    public TimePickerLocalizationView() {
        Stream<Locale> supportedAvailableLocales = TimePicker
                .getSupportedAvailableLocales();
        ComboBox<Locale> localesCB = new ComboBox<>();
        localesCB.setItemLabelGenerator(
                TimePickerLocalizationView::getLocaleString);
        localesCB.setWidth("300px");
        localesCB.setItems(supportedAvailableLocales);
        localesCB.setId("locale-picker");

        timePicker = new TimePicker();

        ComboBox<Duration> stepSelector = new ComboBox<>();
        stepSelector.setItems(Duration.ofMillis(1), Duration.ofMillis(500),
                Duration.ofSeconds(1), Duration.ofSeconds(10),
                Duration.ofMinutes(1), Duration.ofMinutes(15),
                Duration.ofMinutes(30), Duration.ofHours(1));
        stepSelector.setItemLabelGenerator(duration -> {
            return duration.toString().replace("PT", "").toLowerCase();
        });
        stepSelector.setId("step-picker");
        stepSelector.setValue(timePicker.getStep());

        browserFormattedTime = new TimePickerView.LocalTimeTextBlock();
        browserFormattedTime.setId("formatted-time");
        browserFormattedTime.setStep(timePicker.getStep());

        Div valueLabel = new Div();
        valueLabel.setId("value-label");

        localesCB.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                return;
            }
            timePicker.setLocale(event.getValue());
            browserFormattedTime.setLocale(event.getValue());
        });

        stepSelector.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                timePicker.setStep(event.getValue());
                browserFormattedTime.setStep(event.getValue());
            }
        });

        timePicker.addValueChangeListener(event -> {
            LocalTime value = event.getValue();
            browserFormattedTime.setLocalTime(value);
            valueLabel.setText(String.format("%s:%s:%s.%s", value.getHour(),
                    value.getMinute(), value.getSecond(),
                    value.get(ChronoField.MILLI_OF_SECOND)));
        });

        add(localesCB, stepSelector, timePicker, browserFormattedTime,
                valueLabel);
        localesCB.setValue(UI.getCurrent().getLocale());
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

    static String getLocaleString(Locale locale) {
        String country = locale.getCountry();
        return locale.getLanguage() + (country.isEmpty() ? "" : "-" + country);
    }
}
