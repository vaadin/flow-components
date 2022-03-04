package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

@Route("vaadin-date-picker/binder-validation")
public class BinderValidationView extends Div {

    public static final String BINDER_ERROR_MSG = "binder";

    public BinderValidationView() {
        Binder<AData> binder = new Binder<>(AData.class);
        DatePicker dateField = new DatePicker("Date");

        // Set date field validation constraint
        dateField.setMin(LocalDate.of(2019, 1, 1));

        // Set invalid indicator label
        String invalidString = "invalid";
        Element dateFieldElement = dateField.getElement();
        dateFieldElement.addPropertyChangeListener(invalidString, event -> {
            String label = dateFieldElement.getProperty(invalidString, false)
                    ? invalidString
                    : "valid";
            dateFieldElement.setProperty("label", label == null ? "" : label);
        });

        binder.forField(dateField)
                .withValidator(value -> value != null
                        && value.compareTo(LocalDate.of(2019, 2, 1)) > -1,
                        BINDER_ERROR_MSG)
                .bind(AData::getDate, AData::setDate);

        add(dateField);
    }

    public static class AData {

        private LocalDate date;

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }
}
