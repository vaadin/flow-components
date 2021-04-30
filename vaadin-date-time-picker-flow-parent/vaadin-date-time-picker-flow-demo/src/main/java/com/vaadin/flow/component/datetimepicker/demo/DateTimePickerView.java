/*
 * Copyright 2000-2020 Vaadin Ltd.
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
package com.vaadin.flow.component.datetimepicker.demo;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.datetimepicker.demo.entity.Appointment;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link DateTimePicker} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-date-time-picker")
public class DateTimePickerView extends DemoView {

    @Override
    public void initView() {
        basicDemo(); // Basic usage
        disabledAndReadonly();
        timePickerStep();
        createMinAndMaxDateTimePicker();
        valueChangeEvent();
        autoOpenDisabled();
        helperTextAndComponent();
        configurationForRequired(); // Validation
        customValidator();
        datePickerWithWeekNumbers(); // Presentation
        finnishDateTimePicker(); // Localization
        themeVariantsTextAlign(); // Theme variants
        themeVariantsSmallSize();
        themeVariantsHelperAbove();
        styling(); // Styling
    }

    private void basicDemo() {
        VerticalLayout layout = new VerticalLayout();
        // begin-source-example
        // source-example-heading: Basic usage
        DateTimePicker labelDateTimePicker = new DateTimePicker();
        labelDateTimePicker.setLabel("Label");

        DateTimePicker placeholderDateTimePicker = new DateTimePicker();
        placeholderDateTimePicker.setDatePlaceholder("Date");
        placeholderDateTimePicker.setTimePlaceholder("Time");

        DateTimePicker valueDateTimePicker = new DateTimePicker();
        LocalDateTime now = LocalDateTime.now();
        valueDateTimePicker.setValue(now);
        // end-source-example

        layout.add(labelDateTimePicker, placeholderDateTimePicker,
                valueDateTimePicker);
        addCard("Basic usage", layout);
    }

    private void disabledAndReadonly() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Disabled and read-only
        DateTimePicker disabledDateTimePicker = new DateTimePicker();
        disabledDateTimePicker.setLabel("Disabled");
        disabledDateTimePicker.setValue(LocalDateTime.now());
        disabledDateTimePicker.setEnabled(false);

        DateTimePicker readonlyDateTimePicker = new DateTimePicker();
        readonlyDateTimePicker.setLabel("Read-only");
        readonlyDateTimePicker.setValue(LocalDateTime.now());
        readonlyDateTimePicker.setReadOnly(true);
        // end-source-example

        disabledDateTimePicker.getStyle().set("margin-right", "1rem")
                .set("width", "22em");
        readonlyDateTimePicker.getStyle().set("width", "22em");
        div.add(disabledDateTimePicker, readonlyDateTimePicker);
        addCard("Disabled and read-only", div);
    }

    private void timePickerStep() {
        Div div = new Div();
        Paragraph note = new Paragraph(
                "Note: Changing the step changes the time format and the time"
                        + " drop down is not shown when step is less than 15 minutes.");
        // begin-source-example
        // source-example-heading: Time picker step
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setLabel("Label");
        dateTimePicker.setStep(Duration.ofMinutes(30));
        // end-source-example

        div.add(note, dateTimePicker);
        addCard("Time picker step", div);
    }

    private void createMinAndMaxDateTimePicker() {
        // begin-source-example
        // source-example-heading: Min and max
        DateTimePicker dateTimePicker = new DateTimePicker();

        LocalDate today = LocalDate.now();
        LocalDateTime min = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime max = LocalDateTime.of(today.plusWeeks(1), LocalTime.MAX);

        dateTimePicker.setMin(min);
        dateTimePicker.setMax(max);
        // end-source-example

        addCard("Min and max", dateTimePicker);
    }

    private void valueChangeEvent() {
        // begin-source-example
        // source-example-heading: Value change event
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setLabel("Label");

        Div value = new Div();
        value.setText("Select a value");
        dateTimePicker.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                value.setText("No date time selected");
            } else {
                value.setText("Selected date time: " + event.getValue());
            }
        });
        // end-source-example

        Paragraph note = new Paragraph(
                "Note: The date time picker has a non-empty value only when"
                        + " both a date and a time have been selected.");
        VerticalLayout verticalLayout = new VerticalLayout(note, dateTimePicker,
                value);
        verticalLayout.setPadding(false);
        addCard("Value change event", verticalLayout);
    }

    private void autoOpenDisabled() {
        // begin-source-example
        // source-example-heading: Auto open disabled
        DateTimePicker dateTimePicker = new DateTimePicker();

        // Dropdown is only opened when clicking the toggle button or pressing Up or Down arrow keys.
        dateTimePicker.setAutoOpen(false);
        // end-source-example

        addCard("Auto open disabled", dateTimePicker);
    }

    private void helperTextAndComponent() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Helper text and helper component
        DateTimePicker dateTimePicker = new DateTimePicker(
              "Pick-up time");
        dateTimePicker
              .setHelperText("Please, select the most suitable time");

        DateTimePicker dateTimePickerHelperComponent = new DateTimePicker(
              "Arrival time");
        dateTimePickerHelperComponent
              .setHelperComponent(new Span("Select your arrival time"));

        add(dateTimePicker, dateTimePickerHelperComponent);
        // end-source-example

        dateTimePicker.getStyle().set("margin-right", "15px");
        div.add(dateTimePicker, dateTimePickerHelperComponent);

        addCard("Helper text and helper component", div);
    }

    private void configurationForRequired() {
        // begin-source-example
        // source-example-heading: Required
        DateTimePicker dateTimePicker = new DateTimePicker();
        Binder<Appointment> binder = new Binder<>();
        dateTimePicker.setLabel("Appointment time");
        binder.forField(dateTimePicker)
                .asRequired("Please choose a date and time")
                .bind(Appointment::getDateTime, Appointment::setDateTime);

        Button button = new Button("Submit", event -> binder.validate());
        // end-source-example

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setPadding(false);
        verticalLayout.add(dateTimePicker, button);
        addCard("Validation", "Required", verticalLayout);
    }

    private void customValidator() {
        // begin-source-example
        // source-example-heading: Custom validator
        DateTimePicker dateTimePicker = new DateTimePicker();
        Binder<Appointment> binder = new Binder<>();
        dateTimePicker.setLabel("Select an appointment date and time");
        binder.forField(dateTimePicker).withValidator(
                value -> !DayOfWeek.SATURDAY.equals(value.getDayOfWeek())
                        && !DayOfWeek.SUNDAY.equals(value.getDayOfWeek())
                        && value.getHour() >= 8 && value.getHour() <= 16,
                "The selected date must be between Monday to Friday 8AM to 4PM")
                .bind(Appointment::getDateTime, Appointment::setDateTime);
        // end-source-example

        addCard("Validation", "Custom validator", dateTimePicker);
    }

    private void datePickerWithWeekNumbers() {
        Div div = new Div();
        Html note = new Html(
                "<p>Note: Displaying week numbers is only supported when"
                        + " first day of week has been configured as Monday via"
                        + " <code>setFirstDayOfWeek(1)</code>.</p>");
        // begin-source-example
        // source-example-heading: Date picker with week numbers
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setLabel("Label");
        dateTimePicker.setWeekNumbersVisible(true);
        dateTimePicker.setDatePickerI18n(new DatePicker.DatePickerI18n()
                .setWeek("Week").setCalendar("Calendar").setClear("Clear")
                .setToday("Today").setCancel("cancel").setFirstDayOfWeek(1)
                .setMonthNames(Arrays.asList("January", "February", "March",
                        "April", "May", "June", "July", "August", "September",
                        "October", "November", "December"))
                .setWeekdays(Arrays.asList("Sunday", "Monday", "Tuesday",
                        "Wednesday", "Thursday", "Friday", "Saturday"))
                .setWeekdaysShort(Arrays.asList("Sun", "Mon", "Tue", "Wed",
                        "Thu", "Fri", "Sat")));
        // end-source-example

        div.add(note, dateTimePicker);
        addCard("Presentation", "Date picker with week numbers", div);
    }

    private void finnishDateTimePicker() {
        Div message = new Div();
        // begin-source-example
        // source-example-heading: Localizing
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setLabel("Finnish date time picker");
        Locale localeFI = new Locale("fi");
        dateTimePicker.setLocale(localeFI);

        dateTimePicker.setDatePickerI18n(new DatePicker.DatePickerI18n()
                .setWeek("viikko").setCalendar("kalenteri").setClear("tyhjennä")
                .setToday("tänään").setCancel("peruuta").setFirstDayOfWeek(1)
                .setMonthNames(Arrays.asList("tammikuu", "helmikuu",
                        "maaliskuu", "huhtikuu", "toukokuu", "kesäkuu",
                        "heinäkuu", "elokuu", "syyskuu", "lokakuu", "marraskuu",
                        "joulukuu"))
                .setWeekdays(Arrays.asList("sunnuntai", "maanantai", "tiistai",
                        "keskiviikko", "torstai", "perjantai", "lauantai"))
                .setWeekdaysShort(Arrays.asList("su", "ma", "ti", "ke", "to",
                        "pe", "la")));

        dateTimePicker.addValueChangeListener(event -> {
            LocalDateTime selectedDateTime = event.getValue();
            if (selectedDateTime != null) {
                String weekdayName = selectedDateTime.getDayOfWeek()
                        .getDisplayName(TextStyle.FULL, localeFI);
                String monthName = selectedDateTime.getMonth()
                        .getDisplayName(TextStyle.FULL, localeFI);

                message.setText("Day of week: " + weekdayName + ", Month: "
                        + monthName);
            } else {
                message.setText("No date is selected");
            }
        });
        // end-source-example

        addCard("Localization", "Localizing", dateTimePicker, message);
    }

    private void themeVariantsTextAlign() {
        VerticalLayout verticalLayout = new VerticalLayout();
        // begin-source-example
        // source-example-heading: Text align
        DateTimePicker leftDateTimePicker = new DateTimePicker();
        leftDateTimePicker.setValue(LocalDateTime.now());
        leftDateTimePicker.addThemeName("align-left");

        DateTimePicker centerDateTimePicker = new DateTimePicker();
        centerDateTimePicker.setValue(LocalDateTime.now());
        centerDateTimePicker.addThemeName("align-center");

        DateTimePicker rightDateTimePicker = new DateTimePicker();
        rightDateTimePicker.setValue(LocalDateTime.now());
        rightDateTimePicker.addThemeName("align-right");
        // end-source-example

        verticalLayout.add(leftDateTimePicker, centerDateTimePicker,
                rightDateTimePicker);
        verticalLayout.setPadding(false);
        addCard("Theme Variants", "Text align", verticalLayout);
    }

    private void themeVariantsSmallSize() {
        // begin-source-example
        // source-example-heading: Small text field
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.addThemeName("small");
        // end-source-example

        addCard("Theme Variants", "Small text field", dateTimePicker);
    }

    private void themeVariantsHelperAbove() {
        // begin-source-example
        // source-example-heading: Helper text above
        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setLabel("Label");
        dateTimePicker.setHelperText("Helper is positioned above the field");
        dateTimePicker.addThemeName("helper-above-field");

        add(dateTimePicker);
        // end-source-example

        addCard("Theme Variants", "Helper text above", dateTimePicker);
    }

    private void styling() {
        Div firstDiv = new Div();
        firstDiv.setText(
                "To read about styling you can read the related tutorial in");
        Anchor firstAnchor = new Anchor(
                "https://vaadin.com/docs/flow/theme/using-component-themes.html",
                "Using Component Themes");

        Div secondDiv = new Div();
        secondDiv.setText("To know about styling in HTML you can read the ");
        Anchor secondAnchor = new Anchor("https://vaadin.com/components/"
                + "vaadin-date-time-picker/html-examples/date-time-picker-styling-demos",
                "HTML Styling Demos");

        HorizontalLayout firstHorizontalLayout = new HorizontalLayout(firstDiv,
                firstAnchor);
        HorizontalLayout secondHorizontalLayout = new HorizontalLayout(
                secondDiv, secondAnchor);
        // begin-source-example
        // source-example-heading: Styling references

        // end-source-example

        addCard("Styling", "Styling references", firstHorizontalLayout,
                secondHorizontalLayout);
    }
}
