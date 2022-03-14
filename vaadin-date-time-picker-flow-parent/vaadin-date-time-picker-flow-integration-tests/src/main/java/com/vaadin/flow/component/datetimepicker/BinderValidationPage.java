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
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

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

        private LocalDateTime time;

        public LocalDateTime getDateTime() {
            return time;
        }

        public void setDateTime(LocalDateTime time) {
            this.time = time;
        }
    }

}
