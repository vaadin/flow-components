package com.vaadin.flow.component.formlayout.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.testutil.ClassesSerializableTest;

public class FormLayoutSerializableTest extends ClassesSerializableTest {

    private static final UI fakeUI = new UI();

    @Override
    protected void resetThreadLocals() {
        super.resetThreadLocals();
        com.vaadin.flow.component.UI.setCurrent(null);
    }

    @Override
    protected void setupThreadLocals() {
        super.setupThreadLocals();
        com.vaadin.flow.component.UI.setCurrent(fakeUI);
    }
}
