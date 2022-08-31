package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/date-picker-locale")
public class DatePickerLocalePage extends Div {

    private final LocalDate may3rd = LocalDate.of(2018, Month.MAY, 3);
    private final LocalDate april23rd = LocalDate.of(2018, Month.APRIL, 23);

    public DatePickerLocalePage() {
        createDatePicker();
        createDatePickerWithValue();

        DatePicker frenchLocale = new DatePicker();
        frenchLocale.setId("french-locale-date-picker");
        frenchLocale.setLocale(Locale.FRANCE);
        frenchLocale.setValue(may3rd);

        DatePicker german = new DatePicker();
        german.setLocale(Locale.GERMAN);
        german.setId("german-locale-date-picker");
        add(frenchLocale, german);

        DatePicker polandDatePicker = new DatePicker(may3rd,
                new Locale("pl", "PL"));
        polandDatePicker.setId("polish-locale-date-picker");
        add(polandDatePicker);

        DatePicker korean = new DatePicker(may3rd, new Locale("ko", "KR"));
        korean.setId("korean-locale-date-picker");
        add(korean);
    }

    private void createDatePicker() {
        DatePicker datePicker = new DatePicker();
        datePicker.setId("picker");

        NativeButton ukLocale = new NativeButton("Locale: UK");
        ukLocale.setId("picker-set-uk-locale");
        ukLocale.addClickListener(e -> datePicker.setLocale(Locale.UK));

        NativeButton plLocale = new NativeButton("Locale: Poland");
        ukLocale.setId("picker-set-pl-locale");
        ukLocale.addClickListener(
                e -> datePicker.setLocale(new Locale("pl", "PL")));

        NativeButton svLocale = new NativeButton("Locale: Sweden");
        ukLocale.setId("picker-set-sv-locale");
        ukLocale.addClickListener(
                e -> datePicker.setLocale(new Locale("sv", "SE")));

        add(datePicker, ukLocale, plLocale, svLocale);
    }

    private void createDatePickerWithValue() {
        DatePicker datePicker = new DatePicker(april23rd);
        datePicker.setId("picker-with-value");

        NativeButton ukLocale = new NativeButton("Locale: UK");
        ukLocale.setId("picker-with-value-set-uk-locale");
        ukLocale.addClickListener(e -> datePicker.setLocale(Locale.UK));

        NativeButton plLocale = new NativeButton("Locale: Poland");
        ukLocale.setId("picker-with-value-set-pl-locale");
        ukLocale.addClickListener(
                e -> datePicker.setLocale(new Locale("pl", "PL")));

        NativeButton svLocale = new NativeButton("Locale: Sweden");
        ukLocale.setId("picker-with-value-set-sv-locale");
        ukLocale.addClickListener(
                e -> datePicker.setLocale(new Locale("sv", "SE")));

        add(datePicker, ukLocale, plLocale, svLocale);
    }

}
