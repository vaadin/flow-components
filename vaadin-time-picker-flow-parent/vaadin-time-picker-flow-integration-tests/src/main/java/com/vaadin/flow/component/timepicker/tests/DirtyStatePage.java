package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-time-picker/dirty-state")
public class DirtyStatePage extends AbstractDirtyStatePage<TimePicker> {
    @Override
    protected TimePicker createTestField() {
        return new TimePicker();
    }
}
