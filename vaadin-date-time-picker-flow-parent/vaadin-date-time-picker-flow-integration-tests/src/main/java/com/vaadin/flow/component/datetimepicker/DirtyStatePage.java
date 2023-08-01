package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-date-time-picker/dirty-state")
public class DirtyStatePage extends AbstractDirtyStatePage<DateTimePicker> {
    @Override
    protected DateTimePicker createTestField() {
        return new DateTimePicker();
    }
}
