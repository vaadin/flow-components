package com.vaadin.flow.component.customfield.vaadincom;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.textfield.TextField;
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
        addCard("Single element wrapping", new SingleElementWrapping());
        addCard("Native input", new NativeInput());
        addCard("Displaying the field value", new SumField());
        addCard("Custom DateTime Picker", new CustomDateTimePicker());
    }

    // begin-source-example
    // source-example-heading: Displaying the field value
    public static class SumField extends CustomField<Integer> {

        private final TextField firstNumber = new TextField("First number");
        private final TextField secondNumber = new TextField("Second number");
        private final Div display = new Div();

        SumField() {
            super(0);
            setLabel("Sum");
            // Add a value change listener to display the field value.
            addValueChangeListener(e -> display.setText(""+e.getValue()));
            add(firstNumber, secondNumber, display);
        }

        @Override
        protected Integer generateModelValue() {
            try {
                return Integer.valueOf(firstNumber.getValue()) + Integer
                    .valueOf(secondNumber.getValue());
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        @Override
        protected void setPresentationValue(Integer newPresentationValue) {
            // It is not possible to know the values of each field by their sum.
        }

    }
    // end-source-example

    // begin-source-example
    // source-example-heading: Single element wrapping
    public static class SingleElementWrapping extends CustomField<String> {
        private final TextField wrappedField = new TextField();

        SingleElementWrapping() {
            setLabel("Name");
            add(wrappedField);
        }

        @Override
        protected String generateModelValue() {
            return wrappedField.getValue();
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
            wrappedField.setValue(newPresentationValue);
        }
    }

    // end-source-example

    // begin-source-example
    // source-example-heading: Native input
    public static class NativeInput extends CustomField<String> {
        private final Input wrappedField = new Input();

        NativeInput() {
            setLabel("Price");
            wrappedField.setType("number");
            add(wrappedField);
        }

        @Override
        protected String generateModelValue() {
            return wrappedField.getValue();
        }

        @Override
        protected void setPresentationValue(String newPresentationValue) {
            wrappedField.setValue(newPresentationValue);
        }
    }
    // end-source-example

    // begin-source-example
    // source-example-heading: Custom DateTime Picker
    public static class CustomDateTimePicker
        extends CustomField<LocalDateTime> {

        private final DatePicker datePicker = new DatePicker();
        private final TimePicker timePicker = new TimePicker();

        CustomDateTimePicker() {
            setLabel("Start datetime");
            add(datePicker, timePicker);
        }

        @Override
        protected LocalDateTime generateModelValue() {
            final LocalDate date = datePicker.getValue();
            final LocalTime time = timePicker.getValue();
            return date != null && time != null ?
                LocalDateTime.of(date, time) :
                null;
        }

        @Override
        protected void setPresentationValue(
            LocalDateTime newPresentationValue) {
            datePicker.setValue(newPresentationValue != null ?
                newPresentationValue.toLocalDate() :
                null);
            timePicker.setValue(newPresentationValue != null ?
                newPresentationValue.toLocalTime() :
                null);

        }

    }
    // end-source-example

}
