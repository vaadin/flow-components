package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("date-picker-locale")
public class DatePickerLocalePage extends Div {
    
    public DatePickerLocalePage() {
        createPickerWithValueAndLocaleViaDifferentCtor();
    }
    
    private void createPickerWithValueAndLocaleViaDifferentCtor() {
        DatePicker datePicker = new DatePicker(LocalDate.of(2018, 5, 23),
                Locale.CHINA);
        datePicker.setId("locale-picker-server-with-value");

        NativeButton locale = new NativeButton("Locale: UK");
        locale.setId("uk-locale");

        locale.addClickListener(e -> datePicker.setLocale(Locale.UK));
        add(datePicker, locale);
    }
}
