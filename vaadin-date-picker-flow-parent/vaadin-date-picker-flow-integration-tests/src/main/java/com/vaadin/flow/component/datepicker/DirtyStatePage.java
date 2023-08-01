package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.router.Route;
import com.vaadin.tests.dirtystate.AbstractDirtyStatePage;

@Route("vaadin-date-picker/dirty-state")
public class DirtyStatePage extends AbstractDirtyStatePage<DatePicker> {
    @Override
    protected DatePicker createTestField() {
        return new DatePicker();
    }
}
