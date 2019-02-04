package com.vaadin.flow.component.customfield.vaadincom;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

@Route("vaadin-custom-field")
public class CustomFieldView extends DemoView {

    @Override
    protected void initView() {
        basicDemo();
    }

    private void basicDemo() {
        addCard("Single element wrapping", new SingleElementWrapping());
        addCard("Native input", new NativeInput());
        addCard("Displaying the field value", new DecimalNumberField());
        addCard("Custom DateTime Picker", new CustomDateTimePicker());
    }

    // begin-source-example
    // source-example-heading: Displaying the field value
    public static class DecimalNumberField extends CustomField<BigDecimal> {

        private final TextField integerPart = new TextField("Integer part");
        private final TextField fractionalPart = new TextField(
            "Fractional part");
        private final Div display = new Div();

        DecimalNumberField() {
            super(BigDecimal.ZERO);
            setLabel("Decimal field");
            // Add a value change listener to display the field value.
            addValueChangeListener(e -> display.setText("" + e.getValue()));
            add(integerPart, fractionalPart, display);
        }

        @Override
        protected BigDecimal generateModelValue() {
            try {
                Function<TextField, Integer> toNumber = t ->
                   !t.isEmpty() ? Integer.valueOf(t.getValue()) : 0;
                int i = toNumber.apply(integerPart);
                int f = toNumber.apply(fractionalPart);
                return new BigDecimal(i + "." + f);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }

        @Override
        protected void setPresentationValue(BigDecimal newPresentationValue) {
            if (newPresentationValue == null) {
                integerPart.setValue(null);
                fractionalPart.setValue(null);
            } else {
                String[] parts = newPresentationValue.toPlainString()
                    .split("\\.");
                integerPart.setValue(parts[0]);
                fractionalPart.setValue(parts.length > 1 ? parts[1] : null);
            }
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
