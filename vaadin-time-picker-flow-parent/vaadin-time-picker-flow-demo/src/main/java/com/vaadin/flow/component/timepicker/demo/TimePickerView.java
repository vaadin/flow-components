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
package com.vaadin.flow.component.timepicker.demo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link TimePicker} demo.
 */
@Route("vaadin-time-picker")
@HtmlImport("frontend://hide-clear-button-theme.html")
@JsModule("frontend://hide-clear-button-theme.js")
public class TimePickerView extends DemoView {

    @Override
    public void initView() {
        createDefaultTimePicker();
        createLocalizedTimePicker();
        createDisabledTimePicker();
        createWithClearButton();
        createTimePickerWithStepSetting();
        createTimePickerWithMinAndMaxSetting();
    }

    private void createLocalizedTimePicker() {
        // begin-source-example
        // source-example-heading: Localization for Time Picker
        Stream<Locale> availableLocales = TimePicker
                .getSupportedAvailableLocales()
                .sorted(Comparator.comparing(Locale::getDisplayName));
        ComboBox<Locale> localesCB = new ComboBox<>("Localization");
        localesCB.addClassName("no-clear-button");
        localesCB.setItemLabelGenerator(Locale::getDisplayName);
        localesCB.setWidth("300px");
        localesCB.setItems(availableLocales);

        TimePicker timePicker = new TimePicker();

        LocalTimeTextBlock localTimeTextBlock = new LocalTimeTextBlock();

        localesCB.addValueChangeListener(event -> {
            Locale value = event.getValue();
            if (value == null) {
                localesCB.setValue(UI.getCurrent().getLocale());
            } else {
                timePicker.setLocale(event.getValue());
                localTimeTextBlock.setLocale(event.getValue());
            }
        });

        timePicker.addValueChangeListener(
                event -> localTimeTextBlock.setLocalTime(event.getValue()));

        localesCB.setValue(UI.getCurrent().getLocale());

        // end-source-example
        Span localizedTimeLabel = new Span("The formatted value:");
        localTimeTextBlock.setStep(timePicker.getStep());

        Div container = new Div(localesCB, timePicker, localizedTimeLabel,
                localTimeTextBlock);
        container.getStyle().set("display", "inline-grid");
        addCard("Localized Time Picker", container);
    }

    private void createDefaultTimePicker() {
        Div message = createMessageDiv("simple-picker-message");
        // begin-source-example
        // source-example-heading: Default Time Picker
        TimePicker timePicker = new TimePicker();

        timePicker.addValueChangeListener(
                event -> updateMessage(message, timePicker));
        // end-source-example

        timePicker.setId("simple-picker");
        addCard("Default Time Picker", timePicker, message);
    }

    private void createTimePickerWithStepSetting() {
        Label label = new Label(
                "Changing the step changes the time format and the drop down "
                        + "is not shown when step is less than 900 seconds. "
                        + "NOTE: the LocalTime value is not updated when the step changes -"
                        + "new granularity is applied after next time selection.");
        // begin-source-example
        // source-example-heading: Time Picker With Step Setting
        TimePicker timePicker = new TimePicker();

        ComboBox<Duration> stepSelector = new ComboBox<>();
        stepSelector.setLabel("TimePicker Step");
        stepSelector.setItems(Duration.ofMillis(500), Duration.ofSeconds(10),
                Duration.ofMinutes(1), Duration.ofMinutes(15),
                Duration.ofMinutes(30), Duration.ofHours(1));
        stepSelector.setValue(timePicker.getStep());
        stepSelector.addValueChangeListener(event -> {
            Duration newStep = event.getValue();
            if (newStep != null) {
                timePicker.setStep(newStep);

            }
        });
        String localTimeValueFormat = "LocalTime value on server side: %sh %smin %sseconds %smilliseconds";

        // end-source-example
        Div localTimeValue = new Div();
        localTimeValue.setText(
                String.format(localTimeValueFormat, "0", "0", "0", "0"));

        stepSelector.setId("step-picker");
        stepSelector.setItemLabelGenerator(duration -> duration.toString()
                .replace("PT", "").toLowerCase());

        timePicker.addValueChangeListener(event -> {
            LocalTime value = event.getValue();
            localTimeValue.setText(String.format(localTimeValueFormat,
                    value.getHour(), value.getMinute(), value.getSecond(),
                    value.get(ChronoField.MILLI_OF_SECOND)));
        });
        timePicker.setId("step-setting-picker");
        label.setFor(timePicker);
        addCard("Time Picker With Step Setting", label, stepSelector,
                timePicker, localTimeValue);
    }

    private void createDisabledTimePicker() {
        Div message = createMessageDiv("disabled-picker-message");

        // begin-source-example
        // source-example-heading: Disabled Time Picker
        TimePicker timePicker = new TimePicker();
        timePicker.setEnabled(false);
        // end-source-example

        timePicker.addValueChangeListener(event -> message
                .setText("This event should not have happened"));

        timePicker.setId("disabled-picker");
        addCard("Disabled Time Picker", timePicker, message);
    }

    private void createWithClearButton() {
        // begin-source-example
        // source-example-heading: Clear button
        TimePicker timePicker = new TimePicker();
        timePicker.setValue(LocalTime.now());

        // Display an icon which can be clicked to clear the value:
        timePicker.setClearButtonVisible(true);
        // end-source-example

        addCard("Clear button", timePicker);
    }

    private void createTimePickerWithMinAndMaxSetting() {
        Div message = createMessageDiv("time-picker-min-max-message");
        // begin-source-example
        // source-example-heading: Time Picker With Min And Max Setting
        TimePicker timePicker = new TimePicker();
        timePicker.setLabel("TimePicker Min & Max");

        timePicker.setMin("05:00");
        timePicker.setMax("18:00");

        // end-source-example
        timePicker.addValueChangeListener(
                event -> updateMessage(message, timePicker));

        timePicker.setId("time-picker-min-max");
        addCard("Time Picker With Min And Max Setting", timePicker, message);
    }

    private Div createMessageDiv(String id) {
        Div message = new Div();
        message.setId(id);
        message.getStyle().set("whiteSpace", "pre");
        return message;
    }

    private void updateMessage(Div message, TimePicker timePicker) {
        LocalTime selectedTime = timePicker.getValue();
        if (selectedTime != null) {
            message.setText("Hour: " + selectedTime.getHour() + "\nMinute: "
                    + selectedTime.getMinute());
        } else {
            message.setText("No time is selected");
        }
    }

    /**
     * Component for showing browser formatted time string in the given locale.
     */
    public static class LocalTimeTextBlock extends Composite<Div> {

        public final static String MILLISECONDS_SPLIT = "MS:";

        private Locale locale;
        private LocalTime localTime;
        private Duration step;

        public void setLocale(Locale locale) {
            this.locale = locale;
            updateValue();
        }

        public void setLocalTime(LocalTime localTime) {
            this.localTime = localTime;
            updateValue();
        }

        public void setStep(Duration step) {
            this.step = step;
        }

        private void updateValue() {
            if (locale == null || localTime == null) {
                return;
            }
            String format = LocalDateTime.of(LocalDate.now(), localTime)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            StringBuilder tag = new StringBuilder(locale.getLanguage());
            if (!locale.getCountry().isEmpty()) {
                tag.append("-").append(locale.getCountry());
            }
            String expression = "$0['innerText'] = new Date('" + format
                    + "').toLocaleTimeString('" + tag.toString()
                    + "', {hour: 'numeric', minute: 'numeric'"
                    + (step.getSeconds() < 60 ? ", second: 'numeric'" : "")
                    + "})";
            // no support for milliseconds in the toLocaleTimeString method
            if (step.getSeconds() < 1) {
                expression += "+' " + MILLISECONDS_SPLIT + "'+ $1;";
                int milliSeconds = localTime.get(ChronoField.MILLI_OF_SECOND);
                getElement().executeJavaScript(expression, getElement(),
                        milliSeconds);
            } else {
                expression += ";";
                getElement().executeJavaScript(expression, getElement());
            }
        }
    }
}
