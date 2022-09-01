package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/date-picker-locale")
public class DatePickerLocalePage extends Div {

    private final LocalDate may3rd = LocalDate.of(2018, Month.MAY, 3);
    private final LocalDate april23rd = LocalDate.of(2018, Month.APRIL, 23);

    public DatePickerLocalePage() {
        createDatePicker();
        createDatePickerWithGermanLocale();
        createDatePickerWithValue();
        createDatePickerWithValueAndFrenchLocale();
        createDatePickerWithValueAndPolishLocale();
        createDatePickerWithValueAndKoreanLocale();
    }

    private void createDatePicker() {
        DatePicker datePicker = new DatePicker();
        datePicker.setId("picker");

        NativeButton ukLocale = new NativeButton("Locale: UK");
        ukLocale.setId("picker-set-uk-locale");
        ukLocale.addClickListener(e -> datePicker.setLocale(Locale.UK));

        addCard("DatePicker", datePicker, ukLocale);
    }

    private void createDatePickerWithGermanLocale() {
        DatePicker datePicker = new DatePicker(null, Locale.GERMAN);
        datePicker.setId("picker-with-german-locale");
        addCard("DatePicker with German locale", datePicker);
    }

    private void createDatePickerWithValue() {
        DatePicker datePicker = new DatePicker(april23rd);
        datePicker.setId("picker-with-value");

        NativeButton ukLocale = new NativeButton("Locale: UK");
        ukLocale.setId("picker-with-value-set-uk-locale");
        ukLocale.addClickListener(e -> datePicker.setLocale(Locale.UK));

        addCard("DatePicker with value", datePicker, ukLocale);
    }

    private void createDatePickerWithValueAndFrenchLocale() {
        DatePicker datePicker = new DatePicker(may3rd, Locale.FRENCH);
        datePicker.setId("picker-with-value-and-french-locale");
        addCard("DatePicker with value and French locale", datePicker);
    }

    private void createDatePickerWithValueAndPolishLocale() {
        DatePicker datePicker = new DatePicker(may3rd, new Locale("pl", "PL"));
        datePicker.setId("picker-with-value-and-polish-locale");
        addCard("DatePicker with value and Polish locale", datePicker);
    }

    private void createDatePickerWithValueAndKoreanLocale() {
        DatePicker datePicker = new DatePicker(may3rd, new Locale("ko", "KR"));
        datePicker.setId("picker-with-value-and-korean-locale");
        addCard("DatePicker with value and Korean locale", datePicker);
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }

}
