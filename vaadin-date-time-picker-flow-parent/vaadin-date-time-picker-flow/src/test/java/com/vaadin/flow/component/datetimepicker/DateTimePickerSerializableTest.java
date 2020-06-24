package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.testutil.ClassesSerializableTest;

public class DateTimePickerSerializableTest extends ClassesSerializableTest {

    private static final UI FAKE_UI = new UI();

    @Override
    protected void resetThreadLocals() {
        super.resetThreadLocals();
        UI.setCurrent(null);
    }

    @Override
    protected void setupThreadLocals() {
        super.setupThreadLocals();
        UI.setCurrent(FAKE_UI);
    }
}
