package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

@Route("vaadin-date-picker/date-picker-locale")
public class DatePickerLocalePage extends Div {

    public static final String CUSTOMIZABLE_LOCALE_DATE_PICKER = "CUSTOMIZABLE_LOCALE_DATE_PICKER";
    public static final String CUSTOMIZABLE_LOCALE_BUTTON = "CUSTOMIZABLE_LOCALE_BUTTON";
    public static final String CUSTOMIZABLE_LOCALE_INPUT = "CUSTOMIZABLE_LOCALE_INPUT";
    public static final String CUSTOM_REFERENCE_DATE_AND_LOCALE_DATE_PICKER = "CUSTOM_REFERENCE_DATE_AND_LOCALE_DATE_PICKER";
    public static final String CUSTOM_REFERENCE_DATE_AND_LOCALE_OUTPUT = "CUSTOM_REFERENCE_DATE_AND_LOCALE_OUTPUT";

    public static final LocalDate may13 = LocalDate.of(2018, Month.MAY, 13);

    public DatePickerLocalePage() {
        setupCustomizableLocale();
        setupCustomReferenceDateAndLocale();
    }

    private void setupCustomizableLocale() {
        DatePicker datePicker = new DatePicker();
        datePicker.setId(CUSTOMIZABLE_LOCALE_DATE_PICKER);

        Input localeInput = new Input();
        localeInput.setId(CUSTOMIZABLE_LOCALE_INPUT);
        localeInput.setPlaceholder("Enter locale string");
        NativeButton applyLocale = new NativeButton("Apply locale", e -> {
            String localeString = localeInput.getValue();
            String[] localeParts = localeString.split("_");
            Locale locale = null;
            if (localeParts.length == 1) {
                locale = new Locale(localeParts[0]);
            }
            if (localeParts.length == 2) {
                locale = new Locale(localeParts[0], localeParts[1]);
            }
            if (localeParts.length == 3) {
                locale = new Locale(localeParts[0], localeParts[1],
                        localeParts[2]);
            }

            datePicker.setLocale(locale);
        });
        applyLocale.setId(CUSTOMIZABLE_LOCALE_BUTTON);

        add(datePicker, localeInput, applyLocale);
    }

    private void setupCustomReferenceDateAndLocale() {
        DatePicker datePicker = new DatePicker(may13);
        datePicker.setLocale(Locale.FRANCE);

        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setReferenceDate(LocalDate.of(1980, 2, 2));
        datePicker.setI18n(i18n);

        Span output = DatePickerITHelper.createOutputSpan(datePicker);
        datePicker.setId(CUSTOM_REFERENCE_DATE_AND_LOCALE_DATE_PICKER);
        output.setId(CUSTOM_REFERENCE_DATE_AND_LOCALE_OUTPUT);
        add(datePicker, output);
    }
}
