
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;

import java.time.LocalTime;
import java.util.Locale;

/**
 * Test view for attaching / detaching {@link TimePicker}.
 */
@Route("vaadin-time-picker/time-picker-detach-attach")
public class TimePickerDetachAttachPage extends Div {

    /**
     * Constructs a basic layout with a time picker.
     */
    public TimePickerDetachAttachPage() {
        TimePicker timePicker = new TimePicker();
        timePicker.setRequiredIndicatorVisible(true);
        timePicker.setId("time-picker");
        add(timePicker);

        NativeButton toggleAttached = new NativeButton("toggle attached", e -> {
            if (timePicker.getParent().isPresent()) {
                remove(timePicker);
            } else {
                add(timePicker);
            }
        });
        toggleAttached.setId("toggle-attached");
        add(toggleAttached);

        NativeButton setValue = new NativeButton("set value", e -> {
            timePicker.setValue(LocalTime.of(2, 0));
        });
        setValue.setId("set-value");
        add(setValue);

        NativeButton setCaliforniaLocale = new NativeButton(
                "set California locale", e -> {
                    timePicker.setLocale(new Locale("en-CA"));
                });
        setCaliforniaLocale.setId("set-california-locale");
        add(setCaliforniaLocale);
    }
}
