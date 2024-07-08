/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
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
