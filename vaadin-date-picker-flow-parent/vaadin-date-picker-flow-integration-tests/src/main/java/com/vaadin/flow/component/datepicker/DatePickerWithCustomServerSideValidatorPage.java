/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
