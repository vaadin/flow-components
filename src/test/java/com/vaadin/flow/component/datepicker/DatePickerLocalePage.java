package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("date-picker-locale")
public class DatePickerLocalePage extends Div {

    private final LocalDate may30th = LocalDate.of(2018, Month.MAY, 30);
    private final LocalDate april23rd = LocalDate.of(2018, Month.APRIL, 23);

    public DatePickerLocalePage() {
        createPickerWithValueAndLocaleViaDifferentCtor();
        addHungarianLocale();
    }

    private void createPickerWithValueAndLocaleViaDifferentCtor() {
        DatePicker datePicker = new DatePicker(april23rd, Locale.CHINA);
        datePicker.setId("locale-picker-server-with-value");

        NativeButton locale = new NativeButton("Locale: UK");
        locale.setId("uk-locale");

        locale.addClickListener(e -> datePicker.setLocale(Locale.UK));

        DatePicker frenchLocale = new DatePicker();
        frenchLocale.setId("french-locale-date-picker");

        frenchLocale.setLocale(Locale.FRANCE);
        frenchLocale.setValue(may30th);

        DatePicker german = new DatePicker();
        german.setLocale(Locale.GERMANY);
        german.setId("german-locale-date-picker");

        add(datePicker, locale, frenchLocale, german);
    }

    private void addHungarianLocale() {
        DatePicker datePicker = new DatePicker(may30th, new Locale("hu", "HU"));
        datePicker.setId("hungarian-locale-date-picker");
        add(datePicker);
    }
}
