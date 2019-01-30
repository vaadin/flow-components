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
            add(datePicker, timePicker);
        }

        @Override
        protected LocalDateTime generateModelValue() {
            return LocalDateTime.of(datePicker.getValue(),timePicker.getValue());
        }

        @Override
        protected void setPresentationValue(
            LocalDateTime newPresentationValue) {
        }

    }
    // end-source-example
}
