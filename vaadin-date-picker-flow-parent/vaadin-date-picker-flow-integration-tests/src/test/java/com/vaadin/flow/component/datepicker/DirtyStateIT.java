package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.dirtystate.AbstractDirtyStateIT;

@TestPath("vaadin-date-picker/dirty-state")
public class DirtyStateIT
        extends AbstractDirtyStateIT<DatePickerElement> {
    @Override
    protected DatePickerElement getTestField() {
        return $(DatePickerElement.class).first();
    }
}
