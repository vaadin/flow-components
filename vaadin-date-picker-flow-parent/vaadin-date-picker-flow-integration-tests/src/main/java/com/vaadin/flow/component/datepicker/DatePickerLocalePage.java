package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import java.util.Locale;

@Route("vaadin-date-picker/date-picker-locale")
public class DatePickerLocalePage extends Div {
    public DatePickerLocalePage() {
        DatePicker datePicker = new DatePicker();
        datePicker.setId("picker");

        Input localeInput = new Input();
        localeInput.setId("locale-input");
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
        applyLocale.setId("apply-locale");

        add(datePicker, localeInput, applyLocale);
    }
}
