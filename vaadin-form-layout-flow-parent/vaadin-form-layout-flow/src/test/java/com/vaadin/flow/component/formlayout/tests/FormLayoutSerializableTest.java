package com.vaadin.flow.component.formlayout.tests;

import java.util.stream.Stream;

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

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(Stream.of(
                "com\\.vaadin\\.flow\\.component\\.datepicker\\.osgi\\.DatePickerResource"),
                super.getExcludedPatterns());
    }
}
