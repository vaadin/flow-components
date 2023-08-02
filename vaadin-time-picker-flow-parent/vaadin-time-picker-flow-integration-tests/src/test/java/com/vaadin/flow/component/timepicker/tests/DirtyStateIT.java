package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-time-picker/dirty-state")
public class DirtyStateIT extends AbstractDirtyStateIT<TimePickerElement> {
    @Override
    protected TimePickerElement getTestField() {
        return $(TimePickerElement.class).first();
    }
}
