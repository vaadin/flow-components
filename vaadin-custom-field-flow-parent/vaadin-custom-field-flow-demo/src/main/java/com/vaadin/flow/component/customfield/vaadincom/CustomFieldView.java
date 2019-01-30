package com.vaadin.flow.component.customfield.vaadincom;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
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
        addCard("Basic Demo", new DateTimeField());
        addCard("Displaying the field value", new SumField());
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
            return LocalDateTime
                .of(datePicker.getValue(), timePicker.getValue());
        }

        @Override
        protected void setPresentationValue(
            LocalDateTime newPresentationValue) {
        }

    }
    // end-source-example

    // begin-source-example
    // source-example-heading: Displaying the field value.
    public static class SumField extends CustomField<Integer> {

        private final TextField firstNumber = new TextField("First number");
        private final TextField secondNumber = new TextField("Second number");
        private final Div display = new Div();

        SumField() {
            super(0);
            setLabel("Sum");
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
            display.setText("" + newPresentationValue);
        }

    }
    // end-source-example

}
