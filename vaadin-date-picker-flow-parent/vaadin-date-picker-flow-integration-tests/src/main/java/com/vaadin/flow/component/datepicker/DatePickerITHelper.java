package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.html.Span;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatePickerITHelper {

    public static Span createOutputSpan(DatePicker datePicker) {
        Span output = new Span();
        datePicker.addValueChangeListener(event -> {
            LocalDate newValue = datePicker.getValue();
            if (newValue != null) {
                output.setText(newValue.format(DateTimeFormatter.ISO_DATE));
            } else {
                output.setText("");
            }
        });
        return output;
    }
}
