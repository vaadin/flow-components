package com.vaadin.flow.component.customfield.vaadincom;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Route("vaadin-custom-field")
public class CustomFieldView extends DemoView {

    @Override
    protected void initView() {
        basicDemo();
    }

    private void basicDemo() {
        DateTimeField component = new DateTimeField();

        addCard("Basic Demo", component);
    }

    // begin-source-example
    // source-example-heading: Basic Demo
    public static class DateTimeField extends CustomField<LocalDateTime> {

        private final DatePicker datePicker = new DatePicker();
        private final TimePicker timePicker = new TimePicker();

        DateTimeField() {
            super(null);
            datePicker.addValueChangeListener(e -> setModelValue(
                LocalDateTime.of(e.getValue(), timePicker.getValue()),
                e.isFromClient()));
            timePicker.addValueChangeListener(e -> setModelValue(
                LocalDateTime.of(datePicker.getValue(), e.getValue()),
                e.isFromClient()));
            add(datePicker, timePicker);
        }

        @Override
        protected void setPresentationValue(
            LocalDateTime newPresentationValue) {
            LocalDate date = null;
            LocalTime time = null;
            if (newPresentationValue != null) {
                date = newPresentationValue.toLocalDate();
                time = newPresentationValue.toLocalTime();
            }
            datePicker.setValue(date);
            timePicker.setValue(time);
        }

    }
    // end-source-example
}
