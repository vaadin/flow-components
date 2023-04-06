
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

import javax.validation.constraints.NotNull;
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

        @NotNull
        private LocalTime time;

        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }
    }

}
