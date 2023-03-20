/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

/**
 * View for testing custom validation with {@link DatePicker}.
 */
@Route("vaadin-date-picker/required-field-custom-validator")
public class DatePickerWithCustomServerSideValidatorPage extends Div {

    public static class LocalDateWrapper {
        private LocalDate value = LocalDate.of(2019, 1, 2);

        public LocalDate getValue() {
            return value;
        }

        public void setValue(LocalDate value) {
            this.value = value;
        }
    }

    public DatePickerWithCustomServerSideValidatorPage() {
        LocalDateWrapper model = new LocalDateWrapper();
        Binder<LocalDateWrapper> binder = new Binder<>();

        DatePicker datePicker = new DatePicker();

        binder.forField(datePicker)
                .withValidator(s -> s.compareTo(LocalDate.of(2019, 1, 1)) == 0,
                        "Date is not 01/01/2019")
                .asRequired()
                .bind(LocalDateWrapper::getValue, LocalDateWrapper::setValue);
        binder.setBean(model);

        binder.validate();

        add(datePicker);
    }
}
