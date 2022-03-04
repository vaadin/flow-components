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
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

import java.time.LocalTime;

@Route("vaadin-time-picker/binder-validation")
public class BinderValidationPage extends Div {

    public static final String BINDER_ERROR_MSG = "binder";

    public BinderValidationPage() {
        Binder<AData> binder = new Binder<>(AData.class);
        TimePicker timePicker = new TimePicker("Time");

        // Set time field validation constraint
        timePicker.setMin(LocalTime.of(13, 0, 0));

        // Set invalid indicator label
        String invalidString = "invalid";
        Element timeFieldElement = timePicker.getElement();
        timeFieldElement.addPropertyChangeListener(invalidString, event -> {
            String label = timeFieldElement.getProperty(invalidString, false)
                    ? invalidString
                    : "valid";
            timeFieldElement.setProperty("label", label == null ? "" : label);
        });

        binder.forField(timePicker).asRequired()
                .withValidator(
                        value -> value != null
                                && value.compareTo(LocalTime.of(15, 0, 0)) > -1,
                        BINDER_ERROR_MSG)
                .bind(AData::getTime, AData::setTime);

        add(timePicker);
    }

    public static class AData {

        private LocalTime time;

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }
    }

}
