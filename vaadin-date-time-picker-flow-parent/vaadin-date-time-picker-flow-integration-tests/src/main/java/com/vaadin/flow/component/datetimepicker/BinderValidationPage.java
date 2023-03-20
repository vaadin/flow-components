/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Route("vaadin-date-time-picker/binder-validation")
public class BinderValidationPage extends Div {

    public static final String BINDER_ERROR_MSG = "binder";

    public BinderValidationPage() {
        Binder<AData> binder = new Binder<>(AData.class);
        DateTimePicker dateTimePicker = new DateTimePicker();

        // Set date time field validation constraint
        dateTimePicker.setMin(LocalDateTime.of(2020, 6, 7, 1, 0));

        // Set invalid indicator label
        String invalidString = "invalid";
        Element dateTimeFieldElement = dateTimePicker.getElement();
        dateTimeFieldElement.addPropertyChangeListener(invalidString, event -> {
            String label = dateTimeFieldElement.getProperty(invalidString,
                    false) ? invalidString : "valid";
            dateTimeFieldElement.setProperty("label",
                    label == null ? "" : label);
        });

        binder.forField(dateTimePicker).asRequired().withValidator(
                value -> value != null
                        && value.isAfter(LocalDateTime.of(2020, 6, 7, 2, 0)),
                BINDER_ERROR_MSG).bind(AData::getDateTime, AData::setDateTime);

        add(dateTimePicker);
    }

    public static class AData {

        @NotNull
        private LocalDateTime time;

        public LocalDateTime getDateTime() {
            return time;
        }

        public void setDateTime(LocalDateTime time) {
            this.time = time;
        }
    }

}
