package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.testutil.ClassesSerializableTest;

import java.util.stream.Stream;

public class DatePickerSerializableTest extends ClassesSerializableTest {

    @Override
    protected Stream<String> getExcludedPatterns() {
        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                "com\\.vaadin\\.flow\\.component\\.contextmenu\\.osgi\\..*"));
    }
}
