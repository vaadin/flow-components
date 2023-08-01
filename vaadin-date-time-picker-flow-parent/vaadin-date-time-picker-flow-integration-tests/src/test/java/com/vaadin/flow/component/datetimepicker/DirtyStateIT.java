package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-date-time-picker/dirty-state")
public class DirtyStateIT
        extends AbstractDirtyStateIT<DateTimePickerElement> {
    @Override
    protected DateTimePickerElement getTestField() {
        return $(DateTimePickerElement.class).first();
    }
}
