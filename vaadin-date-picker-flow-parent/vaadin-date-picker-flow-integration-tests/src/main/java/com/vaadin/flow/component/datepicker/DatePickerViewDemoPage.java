
/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;

/**
 * View for {@link DatePicker} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-date-picker-test-demo")
public class DatePickerViewDemoPage extends Div {

    public DatePickerViewDemoPage() {
        createSimpleDatePicker();
        createMinAndMaxDatePicker();
        createDisabledDatePicker();
        createFinnishDatePicker();
        createWithClearButton();
        createStartAndEndDatePickers();
        createLocaleChangeDatePicker();
        addCard("Additional code used in the demo",
                new Label("These methods are used in the demo."));
    }

    private void createSimpleDatePicker() {
        Div message = createMessageDiv("simple-picker-message");

        DatePicker datePicker = new DatePicker();

        datePicker.addValueChangeListener(
                event -> updateMessage(message, datePicker));

        datePicker.setId("simple-picker");
        addCard("Simple date picker", datePicker, message);
    }

    private void createMinAndMaxDatePicker() {
        Div message = createMessageDiv("min-and-max-picker-message");

        DatePicker datePicker = new DatePicker();
        datePicker.setLabel("Select a day within this month");
        datePicker.setPlaceholder("Date within this month");

        LocalDate now = LocalDate.now();

        datePicker.setMin(now.withDayOfMonth(1));
        datePicker.setMax(now.withDayOfMonth(now.lengthOfMonth()));

        datePicker.addValueChangeListener(
                event -> updateMessage(message, datePicker));

        datePicker.setId("min-and-max-picker");
        addCard("Date picker with min and max", datePicker, message);
    }

    private void createDisabledDatePicker() {
        Div message = createMessageDiv("disabled-picker-message");

        DatePicker datePicker = new DatePicker();
        datePicker.setEnabled(false);

        datePicker.addValueChangeListener(event -> message
                .setText("This event should not have happened"));

        datePicker.setId("disabled-picker");
        addCard("Disabled date picker", datePicker, message);
    }

    private void createWithClearButton() {
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());

        // Display an icon which can be clicked to clear the value:
        datePicker.setClearButtonVisible(true);

        addCard("Clear button", datePicker);
    }

    private void createFinnishDatePicker() {
        Div message = createMessageDiv("finnish-picker-message");

        DatePicker datePicker = new DatePicker();
        datePicker.setLabel("Finnish date picker");
        datePicker.setPlaceholder("Syntymäpäivä");
        datePicker.setLocale(new Locale("fi"));

        datePicker.setI18n(new DatePicker.DatePickerI18n().setWeek("viikko")
                .setCalendar("kalenteri").setClear("tyhjennä")
                .setToday("tänään").setCancel("peruuta").setFirstDayOfWeek(1)
                .setMonthNames(Arrays.asList("tammiku", "helmikuu", "maaliskuu",
                        "huhtikuu", "toukokuu", "kesäkuu", "heinäkuu", "elokuu",
                        "syyskuu", "lokakuu", "marraskuu", "joulukuu"))
                .setWeekdays(Arrays.asList("sunnuntai", "maanantai", "tiistai",
                        "keskiviikko", "torstai", "perjantai", "lauantai"))
                .setWeekdaysShort(Arrays.asList("su", "ma", "ti", "ke", "to",
                        "pe", "la")));

        datePicker.addValueChangeListener(event -> {
            LocalDate selectedDate = event.getValue();
            if (selectedDate != null) {
                int weekday = selectedDate.getDayOfWeek().getValue() % 7;
                String weekdayName = datePicker.getI18n().getWeekdays()
                        .get(weekday);

                int month = selectedDate.getMonthValue() - 1;
                String monthName = datePicker.getI18n().getMonthNames()
                        .get(month);

                message.setText("Day of week: " + weekdayName + "\nMonth: "
                        + monthName);
            } else {
                message.setText("No date is selected");
            }
        });

        datePicker.setId("finnish-picker");
        addCard("Internationalized date picker", datePicker, message);
    }

    private void createStartAndEndDatePickers() {
        Div message = createMessageDiv("start-and-end-message");

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setLabel("Start");
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setLabel("End");

        startDatePicker.addValueChangeListener(event -> {
            LocalDate selectedDate = event.getValue();
            LocalDate endDate = endDatePicker.getValue();
            if (selectedDate != null) {
                endDatePicker.setMin(selectedDate.plusDays(1));
                if (endDate == null) {
                    endDatePicker.setOpened(true);
                    message.setText("Select the ending date");
                } else {
                    message.setText(
                            "Selected period:\nFrom " + selectedDate.toString()
                                    + " to " + endDate.toString());
                }
            } else {
                endDatePicker.setMin(null);
                message.setText("Select the starting date");
            }
        });

        endDatePicker.addValueChangeListener(event -> {
            LocalDate selectedDate = event.getValue();
            LocalDate startDate = startDatePicker.getValue();
            if (selectedDate != null) {
                startDatePicker.setMax(selectedDate.minusDays(1));
                if (startDate != null) {
                    message.setText(
                            "Selected period:\nFrom " + startDate.toString()
                                    + " to " + selectedDate.toString());
                } else {
                    message.setText("Select the starting date");
                }
            } else {
                startDatePicker.setMax(null);
                if (startDate != null) {
                    message.setText("Select the ending date");
                } else {
                    message.setText("No date is selected");
                }
            }
        });

        startDatePicker.setId("start-picker");
        endDatePicker.setId("end-picker");
        addCard("Two linked date pickers", startDatePicker, endDatePicker,
                message);

    }

    private void createLocaleChangeDatePicker() {
        Div message = createMessageDiv("Customize-locale-picker-message");
        // By default, the datePicker uses the current UI locale
        DatePicker datePicker = new DatePicker();
        NativeButton locale1 = new NativeButton("Locale: US");
        NativeButton locale2 = new NativeButton("Locale: UK");
        NativeButton locale3 = new NativeButton("Locale: CHINA");

        locale1.addClickListener(e -> {
            datePicker.setLocale(Locale.US);
            updateMessage(message, datePicker);
        });
        locale2.addClickListener(e -> {
            datePicker.setLocale(Locale.UK);
            updateMessage(message, datePicker);
        });
        locale3.addClickListener(e -> {
            datePicker.setLocale(Locale.CHINA);
            updateMessage(message, datePicker);
        });

        datePicker.addValueChangeListener(
                event -> updateMessage(message, datePicker));
        locale1.setId("Locale-US");
        locale2.setId("Locale-UK");
        locale3.setId("Locale-CHINA");
        datePicker.setId("locale-change-picker");
        addCard("Date picker with customize locales", datePicker, locale1,
                locale2, locale3, message);
    }

    /**
     * Additional code used in the demo
     */
    private void updateMessage(Div message, DatePicker datePicker) {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            message.setText("Day: " + selectedDate.getDayOfMonth() + "\nMonth: "
                    + selectedDate.getMonthValue() + "\nYear: "
                    + selectedDate.getYear() + "\nLocale: "
                    + datePicker.getLocale());
        } else {
            message.setText("No date is selected");
        }
    }

    private Div createMessageDiv(String id) {
        Div message = new Div();
        message.setId(id);
        message.getStyle().set("whiteSpace", "pre");
        return message;
    }

    private void addCard(String title, Component... components) {
        addCard(title, null, components);
    }

    private void addCard(String title, String description,
            Component... components) {
        if (description != null) {
            title = title + ": " + description;
        }
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
